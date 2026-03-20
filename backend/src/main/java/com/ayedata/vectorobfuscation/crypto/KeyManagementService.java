package com.ayedata.vectorobfuscation.crypto;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.BsonBinary;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeyManagementService {

    private static final Logger log = LoggerFactory.getLogger(KeyManagementService.class);

    private final MongoClient mongoClient;
    private final MongoClientSettings mongoClientSettings;
    private final com.ayedata.vectorobfuscation.websocket.LogWebSocketHandler logHandler;

    @Value("${csfle.local-master-key-path}")
    private String keyFilePath;

    @Value("${csfle.vault-namespace}")
    private String keyVaultNamespace;

    public KeyManagementService(MongoClient mongoClient, 
                               MongoClientSettings mongoClientSettings,
                               com.ayedata.vectorobfuscation.websocket.LogWebSocketHandler logHandler) {
        this.mongoClient = mongoClient;
        this.mongoClientSettings = mongoClientSettings;
        this.logHandler = logHandler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDataEncryptionKeyExists() {
        log.info("Checking for existing Data Encryption Key (DEK) in vault: {}", keyVaultNamespace);

        String[] namespaceParts = keyVaultNamespace.split("\\.");
        MongoCollection<Document> keyVaultCollection = mongoClient.getDatabase(namespaceParts[0])
                .getCollection(namespaceParts[1]);

        if (keyVaultCollection.countDocuments() > 0) {
            log.info("DEK already exists in the key vault. Cryptography layer is ready.");
            return;
        }

        log.warn("No DEK found! Generating a new Data Encryption Key...");
        logHandler.broadcastLog("CRYPTO", 0, "No Data Encryption Key found. Initializing key generation...");
        generateAndStoreNewDek();
    }

    private void generateAndStoreNewDek() {
        try {
            Path path = Paths.get(keyFilePath);
            // Optimization: Avoid intermediate immutable String for sensitive key data
            byte[] base64Bytes = Files.readAllBytes(path);
            byte[] localMasterKeyBytes = Base64.getDecoder().decode(base64Bytes);
            // Clear the base64 bytes immediately
            java.util.Arrays.fill(base64Bytes, (byte) 0);

            Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
            kmsProviders.put("local", Map.of("key", localMasterKeyBytes));

            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                    .keyVaultMongoClientSettings(mongoClientSettings)
                    .keyVaultNamespace(keyVaultNamespace)
                    .kmsProviders(kmsProviders)
                    .build();

            try (ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings)) {
                logHandler.broadcastLog("CRYPTO", 0, "Generating new DEK via CSFLE vault...");
                BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());
                log.info("Successfully generated and stored new DEK. Key ID (Base64): {}",
                        Base64.getEncoder().encodeToString(dataKeyId.getData()));
                logHandler.broadcastLog("CRYPTO", 0, "New Data Encryption Key generated and stored in vault.");
            }

            java.util.Arrays.fill(localMasterKeyBytes, (byte) 0);

        } catch (Exception e) {
            log.error("CRITICAL: Failed to generate the Data Encryption Key. CSFLE will not work.", e);
            logHandler.broadcastLog("ERROR", 0, "CRITICAL: DEK generation failed. Check server logs.");
            throw new RuntimeException("DEK Generation Failed", e);
        }
    }
}