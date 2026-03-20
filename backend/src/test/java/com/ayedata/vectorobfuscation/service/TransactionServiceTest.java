package com.ayedata.vectorobfuscation.service;

import com.ayedata.vectorobfuscation.domain.Transaction;
import com.ayedata.vectorobfuscation.repository.AuditLogRepository;
import com.ayedata.vectorobfuscation.repository.TransactionRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionService transactionService;
    
    @Mock
    private EmbeddingService embeddingService;
    
    @Mock
    private TransactionRepository repository;
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private VectorObfuscationService obfuscationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(repository, auditLogRepository, embeddingService, obfuscationService);
        ReflectionTestUtils.setField(transactionService, "similarityThreshold", 0.3);
        
        // Mock obfuscation to return the vector itself (Identity for testing similarity)
        when(obfuscationService.obfuscate(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock embedding results for unit testing semantic comparisons
        when(embeddingService.embed(contains("coffee"))).thenReturn(new double[]{1.0, 0.0, 0.0});
        when(embeddingService.embed(contains("shopping"))).thenReturn(new double[]{0.0, 1.0, 0.0});
    }

    @Test
    void testSearchTransactions_SemanticSimilarity() {
        // Arrange
        Transaction t1 = Transaction.builder()
                .merchant("Starbucks")
                .description("Morning Coffee")
                .obfuscatedVector(new double[]{1.0, 0.0, 0.0}) // Close to "Coffee"
                .build();
        
        Transaction t2 = Transaction.builder()
                .merchant("Amazon")
                .description("Kindle Book")
                .obfuscatedVector(new double[]{0.0, 1.0, 0.0}) // Close to "Shopping"
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(t1, t2));

        // Act - Search for "coffee"
        List<Transaction> coffeeResults = transactionService.searchTransactions("latte coffee");
        
        // Assert
        assertFalse(coffeeResults.isEmpty());
        assertEquals("Starbucks", coffeeResults.get(0).getMerchant());
        
        // Act - Search for "shopping"
        List<Transaction> shoppingResults = transactionService.searchTransactions("online shopping");
        
        // Assert
        assertFalse(shoppingResults.isEmpty());
        assertEquals("Amazon", shoppingResults.get(0).getMerchant());
    }
}
