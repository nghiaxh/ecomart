package com.ecomart.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Best Match 25 over normalized Vietnamese tokens with typo-tolerant query
 * expansion. Standard Okapi BM25 (k1 = 1.2, b = 0.75, Robertson +0.5 IDF);
 * each query token is expanded with near-miss vocabulary terms so inputs
 * without diacritics or with small typos still match.
 */
public final class Bm25 {

    public record Document(int id, List<String> tokens) {
    }

    public record ScoredDocument(int id, double score) {
    }

    private final List<Document> documents;
    private final List<List<String>> tokenized;
    private final List<Double> lengths;
    private final Map<String, List<Map.Entry<Integer, Integer>>> inverted;
    private final Map<String, Integer> docFreq;
    private final double avgdl;
    private final int n;
    private final double k1;
    private final double b;

    public Bm25(List<Document> documents, double k1, double b) {
        this.documents = documents;
        this.k1 = k1;
        this.b = b;
        this.tokenized = documents.stream().map(d -> d.tokens()).toList();
        this.lengths = tokenized.stream().map(l -> (double) l.size()).toList();
        this.n = tokenized.size();
        this.avgdl = lengths.stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
        this.inverted = new HashMap<>();
        this.docFreq = new HashMap<>();
        for (int docId = 0; docId < n; docId++) {
            Map<String, Integer> tf = termFrequency(tokenized.get(docId));
            for (Map.Entry<String, Integer> e : tf.entrySet()) {
                inverted.computeIfAbsent(e.getKey(), k -> new ArrayList<>())
                        .add(Map.entry(docId, e.getValue()));
                docFreq.merge(e.getKey(), 1, Integer::sum);
            }
        }
    }

    public Bm25(List<Document> documents) {
        this(documents, 1.2, 0.75);
    }

    public List<ScoredDocument> search(String query, int topK, double minScore) {
        List<String> queryTokens = VietText.tokenize(VietText.normalize(query));
        if (queryTokens.isEmpty()) {
            return List.of();
        }
        var expanded = expand(queryTokens);
        Map<Integer, Double> scores = new LinkedHashMap<>();
        for (int docId = 0; docId < n; docId++) {
            scores.put(docId, score(docId, expanded));
        }
        return scores.entrySet().stream()
                .filter(e -> e.getValue() > 0 && e.getValue() >= minScore)
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(topK)
                .map(e -> new ScoredDocument(e.getKey(), e.getValue()))
                .toList();
    }

    private List<String> expand(List<String> queryTokens) {
        List<String> expanded = new ArrayList<>(queryTokens);
        if (inverted.keySet().isEmpty()) {
            return expanded;
        }
        for (String token : queryTokens) {
            if (inverted.containsKey(token)) {
                continue;
            }
            for (String vocab : inverted.keySet()) {
                if (vocab.length() >= 4 && VietText.fuzzyEquals(vocab, token)) {
                    expanded.add(vocab);
                }
            }
        }
        return expanded;
    }

    private double score(int docId, List<String> queryTokens) {
        double docLength = lengths.get(docId);
        double lengthNorm = 1 - b + b * (docLength / avgdl);
        double score = 0;
        for (String term : queryTokens) {
            List<Map.Entry<Integer, Integer>> postings = inverted.get(term);
            if (postings == null) {
                continue;
            }
            int tf = 0;
            for (Map.Entry<Integer, Integer> posting : postings) {
                if (posting.getKey() == docId) {
                    tf = posting.getValue();
                }
            }
            if (tf == 0) {
                continue;
            }
            double idf = Math.log((n - docFreq.get(term) + 0.5) / (docFreq.get(term) + 0.5) + 1);
            score += idf * (tf * (k1 + 1)) / (tf + k1 * lengthNorm);
        }
        return score;
    }

    private static Map<String, Integer> termFrequency(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String token : tokens) {
            tf.merge(token, 1, Integer::sum);
        }
        return tf;
    }
}