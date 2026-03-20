package com.ayedata.vectorobfuscation.service;

import com.ayedata.vectorobfuscation.domain.AuditLog;
import com.ayedata.vectorobfuscation.domain.Transaction;
import com.ayedata.vectorobfuscation.repository.AuditLogRepository;
import com.ayedata.vectorobfuscation.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;
    private final AuditLogRepository auditLogRepository;
    private final EmbeddingService embeddingService;
    private final VectorObfuscationService obfuscationService;
    
    @Value("${SIMILARITY_THRESHOLD:0.3}")
    private double similarityThreshold;

    public TransactionService(TransactionRepository repository,
                              AuditLogRepository auditLogRepository,
                              EmbeddingService embeddingService, 
                              VectorObfuscationService obfuscationService) {
        this.repository = repository;
        this.auditLogRepository = auditLogRepository;
        this.embeddingService = embeddingService;
        this.obfuscationService = obfuscationService;
    }

    public Transaction saveTransaction(String merchant, Double amount, String description) {
        // 1. Embed description
        double[] rawVector = embeddingService.embed(description);

        // 2. Obfuscate vector
        double[] obfuscatedVector = obfuscationService.obfuscate(rawVector);

        // 3. Save to MongoDB
        Transaction transaction = Transaction.builder()
                .merchant(merchant)
                .amount(amount)
                .description(description)
                .timestamp(Instant.now())
                .obfuscatedVector(obfuscatedVector)
                .build();

        return repository.save(transaction);
    }


    public List<Transaction> searchTransactions(String query) {
        long startTime = System.currentTimeMillis();
        
        // 1. Embed query (Timing this as part of latency)
        double[] rawQueryVector = embeddingService.embed(query);
        
        // 2. Obfuscate query vector
        double[] obfuscatedQueryVector = obfuscationService.obfuscate(rawQueryVector);
        
        List<Transaction> all = repository.findAll();
        log.info("Starting Semantic Search: Query='{}', Candidates={}, Threshold={}", query, all.size(), similarityThreshold);
        
        long duration = System.currentTimeMillis() - startTime;
        List<Transaction> results = all.stream()
                .filter(t -> {
                    boolean match = t.getObfuscatedVector() != null && t.getObfuscatedVector().length == obfuscatedQueryVector.length;
                    if (!match && t.getObfuscatedVector() != null) {
                        log.warn("Vector Dimension Mismatch: DB={} != Query={}", t.getObfuscatedVector().length, obfuscatedQueryVector.length);
                    }
                    return match;
                })
                .map(t -> {
                    double score = cosineSimilarity(obfuscatedQueryVector, t.getObfuscatedVector());
                    t.setSimilarityScore(score);
                    log.info("Candidate Score: Merchant={}, Score={}", t.getMerchant(), String.format("%.4f", score));
                    return t;
                })
                .filter(t -> t.getSimilarityScore() >= similarityThreshold)
                .sorted((t1, t2) -> Double.compare(t2.getSimilarityScore(), t1.getSimilarityScore()))
                .limit(5)
                .peek(t -> {
                    log.info("Match Found: Merchant={}, Description={}, Score={}", 
                        t.getMerchant(), t.getDescription(), String.format("%.4f", t.getSimilarityScore()));
                    
                    auditLogRepository.save(AuditLog.builder()
                        .eventType("VECTOR_SEARCH_MATCH")
                        .method("GET")
                        .latencyMs(duration)
                        .message("Semantic match for query: " + query + " | Result: " + t.getMerchant())
                        .relevancyScore(t.getSimilarityScore())
                        .timestamp(Instant.now())
                        .build());
                })
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            log.warn("No semantic matches found above threshold {} for query: {}", similarityThreshold, query);
        }
        
        return results;
    }

    private double cosineSimilarity(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) return 0.0;
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        
        if (normA == 0.0 || normB == 0.0) return 0.0;
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
