package com.ecomart.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VietTextTest {

    @Test
    void stripsDiacritics() {
        assertEquals("phi giao hang", VietText.normalize("Phí giao hàng"));
        assertEquals("an com chua", VietText.normalize("Ăn cơm chưa?"));
        assertEquals("dang nhap", VietText.normalize("Đăng nhập"));
    }

    @Test
    void rewritesCommonShorthand() {
        assertEquals("khong co dau", VietText.normalize("ko có đâu"));
        assertEquals("toi dang nhap", VietText.normalize("tôi đn"));
    }

    @Test
    void collapsesWhitespace() {
        assertEquals("rau cu sach", VietText.normalize("  Rau   củ \tsạch "));
    }

    @Test
    void fuzzyEqualsAllowsSingleTypoOnMediumWords() {
        assertTrue(VietText.fuzzyEquals("thanh", "thanh"));
        assertTrue(VietText.fuzzyEquals("thanh", "thann"));
        assertTrue(VietText.fuzzyEquals("thanh", "than"));
        assertFalse(VietText.fuzzyEquals("thanh", "thngk"));
    }

    @Test
    void fuzzyEqualsAllowsTwoTyposOnLongWords() {
        assertTrue(VietText.fuzzyEquals("thanh toan", "thanh toan"));
        assertTrue(VietText.fuzzyEquals("phuong thuc", "phuong thux"));
    }

    @Test
    void fuzzyEqualsNeverMatchesShortWordsWithTypos() {
        assertTrue(VietText.fuzzyEquals("phi", "phi"));
        assertFalse(VietText.fuzzyEquals("phi", "pho"));
    }
}