package com.xianhuicode.medical_ai.knowledge.service;

import java.util.ArrayList;
import java.util.List;

/**
 * 纯文本切块：按 maxChars + overlap 滑动切分，去空白、跳过空块。
 */
public final class TextChunker {

    private static final int MAX_CHARS = 800;
    private static final int OVERLAP = 100;

    private TextChunker() {
    }

    public static List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null) {
            return chunks;
        }
        String clean = text.trim();
        if (clean.isEmpty()) {
            return chunks;
        }
        int len = clean.length();
        if (len <= MAX_CHARS) {
            chunks.add(clean);
            return chunks;
        }
        int step = MAX_CHARS - OVERLAP;
        int start = 0;
        while (start < len) {
            int end = Math.min(start + MAX_CHARS, len);
            String piece = clean.substring(start, end).trim();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
            if (end >= len) {
                break;
            }
            start += step;
        }
        return chunks;
    }
}
