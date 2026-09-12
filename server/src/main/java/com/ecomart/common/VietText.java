package com.ecomart.common;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VietText {

    private static final Map<String, String> SHORTHAND = Map.ofEntries(
            Map.entry("ko", "khong"),
            Map.entry("k", "khong"),
            Map.entry("hem", "khong"),
            Map.entry("hong", "khong"),
            Map.entry("hok", "khong"),
            Map.entry("dc", "duoc"),
            Map.entry("bt", "binh thuong"),
            Map.entry("j", "gi"),
            Map.entry("ge", "gi"),
            Map.entry("ok", "ok"),
            Map.entry("oke", "ok"),
            Map.entry("vs", "voi"),
            Map.entry("dn", "dang nhap"));

    private VietText() {
    }

    /**
     * Lowercases, strips Vietnamese diacritics (đ → d, á → a), maps common
     * shorthand and collapses whitespace, so "Phí giao hàng" and "phi giao hag"
     * both become comparable token sequences.
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String s = input.toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return String.join(" ", tokenize(s));
    }

    /**
     * Splits on non-letters/digits and rewrites whole tokens through the
     * shorthand map. Untokenized text is also fine for exact term matching.
     */
    public static List<String> tokenize(String normalized) {
        return java.util.Arrays.stream(normalized.split("[^\\p{L}\\p{N}]+"))
                .filter(t -> !t.isBlank())
                .map(t -> SHORTHAND.getOrDefault(t, t))
                .toList();
    }

    /**
     * Levenshtein distance between two strings. O(n*m); intended for short
     * chat tokens.
     */
    public static int editDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(
                        prev[j] + 1,
                        curr[j - 1] + 1),
                        prev[j - 1] + cost);
            }
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }
        return prev[m];
    }

    /**
     * True when {@code candidate} equals {@code target} or differs only by a
     * plausible typo: at most one edit for short words, two for long ones.
     */
    public static boolean fuzzyEquals(String target, String candidate) {
        if (target.equals(candidate)) {
            return true;
        }
        int maxDistance = target.length() >= 7 ? 2 : (target.length() >= 4 ? 1 : 0);
        return maxDistance > 0 && editDistance(target, candidate) <= maxDistance;
    }
}