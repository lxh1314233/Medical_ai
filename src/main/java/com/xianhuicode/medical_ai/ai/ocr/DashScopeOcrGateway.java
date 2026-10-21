package com.xianhuicode.medical_ai.ai.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DashScope Qwen-VL 视觉识别（OpenAI 兼容端点）：jpg/png 直接 base64 走 /chat/completions；
 * pdf 用 PDFBox 逐页渲染成 PNG 再识别。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ocr", name = "provider", havingValue = "dashscope")
public class DashScopeOcrGateway implements OcrGateway {

    private static final String PROMPT = """
            你是检验报告识别助手。请识别图片中出现的所有检验指标项，严格只输出一个 JSON 数组，不要输出任何其他文字，不要用 markdown 代码块。
            数组每个元素形如 {"item":"指标名","value":"数值","unit":"单位"}。
            若无法识别到任何指标，输出 []。
            """;
    private static final int MAX_PDF_PAGES = 3;

    private final OcrProperties props;
    private final ObjectMapper objectMapper;

    private volatile RestClient restClient;

    @Override
    public List<OcrIndicator> recognize(byte[] bytes, String fileType) {
        List<String> images = toDataUrls(bytes, fileType);
        if (images.isEmpty()) {
            return List.of();
        }
        String json = postJson(images);
        return parseIndicators(json);
    }

    // ==================== 图片构造 ====================

    private List<String> toDataUrls(byte[] bytes, String fileType) {
        List<String> urls = new ArrayList<>();
        try {
            switch (fileType == null ? "" : fileType) {
                case "jpg", "jpeg" -> urls.add(dataUrl("image/jpeg", bytes));
                case "png" -> urls.add(dataUrl("image/png", bytes));
                case "pdf" -> urls.addAll(pdfPageDataUrls(bytes));
                default -> log.warn("不支持的 OCR 文件类型: {}", fileType);
            }
        } catch (Exception e) {
            log.error("OCR 图片转换失败 type={}: {}", fileType, e.getMessage());
        }
        return urls;
    }

    private List<String> pdfPageDataUrls(byte[] bytes) throws Exception {
        List<String> urls = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pages = Math.min(doc.getNumberOfPages(), MAX_PDF_PAGES);
            for (int i = 0; i < pages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 150);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image, "png", out);
                urls.add(dataUrl("image/png", out.toByteArray()));
            }
        }
        return urls;
    }

    private String dataUrl(String mime, byte[] bytes) {
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    // ==================== 请求 ====================

    private String postJson(List<String> images) {
        try {
            List<Object> content = new ArrayList<>();
            content.add(Map.of("type", "text", "text", PROMPT));
            for (String url : images) {
                content.add(Map.of("type", "image_url", "image_url", Map.of("url", url)));
            }
            Map<String, Object> body = Map.of(
                    "model", props.getModel(),
                    "messages", List.of(Map.of("role", "user", "content", content)));
            String json = objectMapper.writeValueAsString(body);
            return client().post().uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("DashScope OCR 调用失败: {}", e.getMessage());
            throw new IllegalStateException("OCR 识别失败", e);
        }
    }

    private RestClient client() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    restClient = RestClient.builder()
                            .baseUrl(props.getBaseUrl())
                            .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                            .build();
                }
            }
        }
        return restClient;
    }

    // ==================== 解析 ====================

    private List<OcrIndicator> parseIndicators(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            String content = extractContent(root);
            String arrayJson = extractArray(content);
            if (arrayJson == null) {
                return List.of();
            }
            JsonNode arr = objectMapper.readTree(arrayJson);
            if (arr == null || !arr.isArray()) {
                return List.of();
            }
            List<OcrIndicator> result = new ArrayList<>();
            Iterator<JsonNode> it = arr.iterator();
            while (it.hasNext()) {
                JsonNode n = it.next();
                String item = n.path("item").asText(null);
                if (item == null || item.isBlank()) {
                    continue;
                }
                String value = n.path("value").asText("");
                String unit = n.path("unit").asText(null);
                result.add(new OcrIndicator(item.trim(), value.trim(), unit == null ? null : unit.trim()));
            }
            return result;
        } catch (Exception e) {
            log.warn("解析 OCR 结果失败: {}", e.getMessage());
            return List.of();
        }
    }

    /** 提取 choices[0].message.content，兼容字符串与分段数组两种返回。 */
    private String extractContent(JsonNode root) {
        JsonNode choices = root == null ? null : root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return null;
        }
        JsonNode content = choices.get(0).path("message").get("content");
        if (content == null) {
            return null;
        }
        if (content.isTextual()) {
            return content.asText();
        }
        if (content.isArray()) {
            StringBuilder sb = new StringBuilder();
            content.forEach(n -> {
                if (n.isTextual()) {
                    sb.append(n.asText());
                } else if (n.get("text") != null) {
                    sb.append(n.get("text").asText());
                }
            });
            return sb.toString();
        }
        return content.toString();
    }

    private String extractArray(String s) {
        if (s == null) {
            return null;
        }
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return null;
        }
        return s.substring(start, end + 1);
    }
}
