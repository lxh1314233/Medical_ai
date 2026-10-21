package com.xianhuicode.medical_ai.ai.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock OCR：无外部依赖即可跑通「上传 → 识别 → 解读」链路，用于开发联调。
 * 固定返回一组指标，覆盖 正常/轻度异常/显著异常/危急值 四种分级。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "ocr", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockOcrGateway implements OcrGateway {

    @Override
    public List<OcrIndicator> recognize(byte[] bytes, String fileType) {
        // 血钾 2.5 触发危急值；ALT 120 显著偏高；白细胞 12.5 轻度偏高；其余正常
        return List.of(
                new OcrIndicator("白细胞计数", "12.5", "10^9/L"),
                new OcrIndicator("血红蛋白", "138", "g/L"),
                new OcrIndicator("血小板", "180", "10^9/L"),
                new OcrIndicator("ALT", "120", "U/L"),
                new OcrIndicator("肌酐", "70", "µmol/L"),
                new OcrIndicator("空腹血糖", "5.4", "mmol/L"),
                new OcrIndicator("血钾", "2.5", "mmol/L"),
                new OcrIndicator("血钠", "142", "mmol/L"));
    }
}
