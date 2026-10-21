package com.xianhuicode.medical_ai.knowledge.service;

import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 按扩展名提取文本：txt/md 直接读（UTF-8，失败回退 GBK），pdf 走 PDFBox，docx 走 POI。
 */
@Slf4j
@Component
public class DocumentTextExtractor {

    public String extract(byte[] bytes, String fileType) {
        try {
            return switch (fileType) {
                case "txt", "md" -> readText(bytes);
                case "pdf" -> extractPdf(bytes);
                case "docx" -> extractDocx(bytes);
                default -> throw new BizException(ResultCode.KNOWLEDGE_FILE_TYPE_UNSUPPORTED);
            };
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件解析失败 type={}: {}", fileType, e.getMessage());
            throw new BizException(ResultCode.KNOWLEDGE_PARSE_FAILED);
        }
    }

    private String readText(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        if (s.indexOf('�') >= 0) {
            s = new String(bytes, Charset.forName("GBK"));
        }
        return s;
    }

    private String extractPdf(byte[] bytes) throws Exception {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractDocx(byte[] bytes) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
