package com.ayedata.vectorobfuscation.controller;

import com.ayedata.vectorobfuscation.dto.ObfuscationRequest;
import com.ayedata.vectorobfuscation.dto.ObfuscationResponse;
import com.ayedata.vectorobfuscation.service.VectorObfuscationService;
import com.ayedata.vectorobfuscation.websocket.LogWebSocketHandler;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class VectorObfuscationController {

    // 1. Manually declare the logger (Bypassing @Slf4j)
    private static final Logger log = LoggerFactory.getLogger(VectorObfuscationController.class);

    private final VectorObfuscationService obfuscationService;
    private final LogWebSocketHandler logStreamer;
    private final com.ayedata.vectorobfuscation.repository.AuditLogRepository auditLogRepository;
    private final com.ayedata.vectorobfuscation.service.TransactionService transactionService;

    public VectorObfuscationController(VectorObfuscationService obfuscationService, 
                                      LogWebSocketHandler logStreamer,
                                      com.ayedata.vectorobfuscation.repository.AuditLogRepository auditLogRepository,
                                      com.ayedata.vectorobfuscation.service.TransactionService transactionService) {
        this.obfuscationService = obfuscationService;
        this.logStreamer = logStreamer;
        this.auditLogRepository = auditLogRepository;
        this.transactionService = transactionService;
    }

    private void persistAndBroadcast(String eventType, String method, long latency, String message) {
        logStreamer.broadcastLog(eventType, latency, message);
        auditLogRepository.save(com.ayedata.vectorobfuscation.domain.AuditLog.builder()
                .eventType(eventType)
                .method(method)
                .latencyMs(latency)
                .message(message)
                .timestamp(java.time.Instant.now())
                .build());
    }

    @PostMapping("/obfuscate")
    public ResponseEntity<ObfuscationResponse> obfuscateVector(@Valid @RequestBody ObfuscationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            persistAndBroadcast("HTTP_POST", "obfuscate", 0, "Received raw vector for obfuscation.");

            double[] obfuscated = obfuscationService.obfuscate(request.vector());

            long latency = System.currentTimeMillis() - startTime;
            persistAndBroadcast("TRANSFORMATION_SUCCESS", "obfuscate", latency,
                    "Successfully multiplied by Orthogonal Matrix. Distance preserved.");

            return ResponseEntity.ok(new ObfuscationResponse(obfuscated, latency));

        } catch (IllegalArgumentException e) {
            persistAndBroadcast("ERROR", "obfuscate", 0, "Validation failed: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            persistAndBroadcast("ERROR", "obfuscate", 0, "Internal Server Error during obfuscation.");
            throw e;
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<com.ayedata.vectorobfuscation.domain.Transaction> createTransaction(
            @RequestBody com.ayedata.vectorobfuscation.dto.TransactionRequest request) {
        long startTime = System.currentTimeMillis();
        persistAndBroadcast("UPI_PAYMENT", "createTransaction", 0, 
            "Processing UPI Payment for merchant: " + request.merchant());
        
        com.ayedata.vectorobfuscation.domain.Transaction transaction = 
            transactionService.saveTransaction(request.merchant(), request.amount(), request.description());
            
        persistAndBroadcast("DATABASE", "createTransaction", System.currentTimeMillis() - startTime, 
            "Transaction saved with obfuscated vector.");
            
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/search")
    public ResponseEntity<java.util.List<com.ayedata.vectorobfuscation.domain.Transaction>> searchTransactions(
            @RequestParam String query) {
        long startTime = System.currentTimeMillis();
        persistAndBroadcast("SEMANTIC_SEARCH", "searchTransactions", 0, 
            "Performing semantic search for: " + query);
            
        java.util.List<com.ayedata.vectorobfuscation.domain.Transaction> results = 
            transactionService.searchTransactions(query);

        long latency = System.currentTimeMillis() - startTime;

        // Broadcast each match with its relevancy score
        for (com.ayedata.vectorobfuscation.domain.Transaction t : results) {
            persistAndBroadcast("RELEVANCY_SCORE", "searchTransactions", latency, 
                String.format("Match: %s | Score: %.4f | Description: %s", 
                    t.getMerchant(), t.getSimilarityScore(), t.getDescription()));
        }

        persistAndBroadcast("SEARCH_SUCCESS", "searchTransactions", latency, 
            "Found " + results.size() + " relevant transactions using obfuscated vector similarity.");
            
        return ResponseEntity.ok(results);
    }
}