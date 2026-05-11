package com.chupchia.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class OcrUtilsTest {

    @Test
    public void testExtractAmount_WithKeywords() {
        String input = "Cửa hàng tiện lợi\nTOTAL: 150.000\nCảm ơn quý khách";
        assertEquals("150000", OcrUtils.extractAmount(input));
        
        String input2 = "Hóa đơn thanh toán\nTổng cộng: 2.500.000 VND";
        assertEquals("2500000", OcrUtils.extractAmount(input2));
    }

    @Test
    public void testExtractAmount_WithoutKeywords() {
        String input = "Phở Bò\n35.000\nCoca\n15.000";
        // It should pick the largest one within heuristic range
        assertEquals("35000", OcrUtils.extractAmount(input));
    }

    @Test
    public void testExtractAmount_MixedFormats() {
        String input = "Giá: 50,000đ\nSố lượng: 1";
        assertEquals("50000", OcrUtils.extractAmount(input));
    }

    @Test
    public void testExtractAmount_Invalid() {
        String input = "Không có số nào ở đây";
        assertNull(OcrUtils.extractAmount(input));
        
        String input2 = "Ngày 05/04/2026";
        assertNull(OcrUtils.extractAmount(input2));
    }
}
