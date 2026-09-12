package com.ecomart.service;

import java.util.List;

/**
 * Chat intents ordered from most specific to most generic; keywords are
 * stored in normalized (accent-stripped, lowercase) form so detection is
 * robust to inputs typed without diacritics.
 */
public enum ChatIntent {

    SHIPPING_FEE(List.of("phi giao hang", "phi ship", "ship bao nhieu", "phi van chuyen", "cuoc phi",
            "phi giao", "phi ship la bao nhieu", "mien phi ship", "sieu thi co giao khong",
            "giao hang phi bao nhieu", "phi giao hang bao nhieu")),
    OUT_OF_STOCK(List.of("het hang", "tam het", "hang het", "san pham het", "het stock",
            "out of stock", "con hang khong", "san pham nao het hang")),
    NEW_PRODUCTS(List.of("san pham moi", "hang moi", "san pham moi nhat", "san pham noi bat",
            "moi ve", "best seller", "ban chay")),
    CATEGORY_LIST(List.of("danh muc", "nhom hang", "nganh hang", "ban nhung gi", "co nhung gi",
            "co hang gi", "san pham gi", "co nhung san pham nao", "categories")),
    PAYMENT_METHODS(List.of("thanh toan", "cach thanh toan", "phuong thuc thanh toan",
            "cod", "tien mat", "tra tien", "payos", "qr", "thanh toan the nao")),
    ORDER_HELP(List.of("don hang", "huy don", "tra cuu don", "theo doi don", "don cua toi",
            "kiem tra don", "huy dat", "trang thai don")),
    ACCOUNT(List.of("tai khoan", "dang ky", "dang nhap", "tao tai khoan", "dang ki")),
    RESET_PASSWORD(List.of("quen mat khau", "doi mat khau", "mat khau")),
    HELP(List.of("giup", "tro giup", "huong dan", "ho tro", "giup do", "bot giup gi")),
    THANKS(List.of("cam on", "camon", "thanks", "thank you", "thank")),
    FAREWELL(List.of("tam biet", "bye", "hen gap lai", "goodbye")),
    GREETING(List.of("chao", "xin chao", "hi", "hello", "alo", "helo", "e oi"));

    private final List<String> keywords;

    ChatIntent(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> keywords() {
        return keywords;
    }

    public static List<ChatIntent> ordered() {
        return List.of(SHIPPING_FEE, OUT_OF_STOCK, NEW_PRODUCTS, CATEGORY_LIST, PAYMENT_METHODS,
                ORDER_HELP, ACCOUNT, RESET_PASSWORD, HELP, THANKS, FAREWELL, GREETING);
    }
}