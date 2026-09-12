package com.ecomart.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bm25Test {

    private List<Bm25.Document> docs() {
        return List.of(
                new Bm25.Document(0, VietText.tokenize("rau sach")),
                new Bm25.Document(1, VietText.tokenize("rau")));
    }

    @Test
    void ranksFullMatchAbovePartialMatch() {
        Bm25 bm25 = new Bm25(docs());

        List<Bm25.ScoredDocument> hits = bm25.search("rau sach", 2, 0.0);

        assertEquals(2, hits.size());
        assertEquals(0, hits.get(0).id());
    }

    @Test
    void emptyQueryReturnsNothing() {
        Bm25 bm25 = new Bm25(docs());

        assertTrue(bm25.search("!!!", 5, 0.0).isEmpty());
    }

    @Test
    void minScoreFiltersWeakMatches() {
        Bm25 bm25 = new Bm25(docs());
        List<Bm25.ScoredDocument> hits = bm25.search("rau sach", 5, 10.0);

        assertTrue(hits.isEmpty());
    }

    @Test
    void unmatchedQueryReturnsNothing() {
        Bm25 bm25 = new Bm25(docs());
        List<Bm25.ScoredDocument> hits = bm25.search("xe may", 5, 0.0);

        assertTrue(hits.isEmpty());
    }

    @Test
    void typoTolerantExpansionFindsMatches() {
        Bm25 bm25 = new Bm25(docs());

        List<Bm25.ScoredDocument> hits = bm25.search("saxh", 1, 0.0);

        assertEquals(1, hits.size());
        assertEquals(0, hits.get(0).id());
        assertTrue(hits.get(0).score() > 0.0);
    }
}