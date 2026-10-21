package com.xianhuicode.medical_ai.ai.ocr;

import java.util.List;

/**
 * OCR 网关抽象：图片/PDF → 结构化指标项（item/value/unit），与具体实现解耦，便于 mock 与真实模型切换。
 */
public interface OcrGateway {

    /**
     * 识别报告文件中的检验指标。
     *
     * @param bytes    文件字节
     * @param fileType 文件类型（jpg/jpeg/png/pdf）
     * @return 识别的指标项列表（可能为空）
     */
    List<OcrIndicator> recognize(byte[] bytes, String fileType);

    /** OCR 识别出的单个指标项。 */
    record OcrIndicator(String item, String value, String unit) {
    }
}
