package com.ayedata.vectorobfuscation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MongoEncryptionConfig {
  private static final Logger log = LoggerFactory.getLogger(MongoEncryptionConfig.class);

  @Value("${csfle.local-master-key-path}")
  private String keyFilePath;

  @Value("${csfle.vault-namespace}")
  private String keyVaultNamespace;

  @Value("${spring.data.mongodb.database}")
  private String targetDatabase;

  // We manually inject the URI to apply it directly to the core driver
  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  public MongoEncryptionConfig() {
    System.out.println("DEBUG: MongoEncryptionConfig initialized!");
  }

  @Bean
  public org.springframework.data.mongodb.MongoDatabaseFactory mongoDatabaseFactory(com.mongodb.client.MongoClient mongoClient) {
    System.out.println("DEBUG: Creating MongoDatabaseFactory for database: " + targetDatabase);
    return new org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory(mongoClient, targetDatabase);
  }

  @Bean
  public com.mongodb.client.MongoClient mongoClient(MongoClientSettings settings) {
    System.out.println("DEBUG: Creating MongoClient with CSFLE settings...");
    return com.mongodb.client.MongoClients.create(settings);
  }

  /**
   * Creates a core MongoClientSettings bean directly, bypassing Spring Boot's
   * autoconfigure packages.
   * Spring Boot will automatically detect this bean and use it for all MongoDB
   * connections.
   */
  @Bean
  public MongoClientSettings mongoClientSettings() {
    // 1. Initialize the builder with our target connection string
    MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(mongoUri));

    Path path = Paths.get(keyFilePath);
    if (!Files.exists(path)) {
      log.warn("CSFLE Key file not found at '{}'! MongoDB will operate WITHOUT encryption.", keyFilePath);
      return settingsBuilder.build();
    }

    log.info("Reading MongoDB CSFLE Master Key from file: {}", keyFilePath);
    byte[] localMasterKeyBytes;

    try {
      byte[] base64Bytes = Files.readAllBytes(path);
      // We still need a temporary string to trim, but we'll clear the source bytes
      String base64Key = new String(base64Bytes).trim();
      localMasterKeyBytes = Base64.getDecoder().decode(base64Key);
      
      // Zero out the temporary byte array
      java.util.Arrays.fill(base64Bytes, (byte) 0);

      if (localMasterKeyBytes.length != 96) {
        log.error("SEVERE: CSFLE Master Key must be exactly 96 bytes. Found {} bytes. Halting encryption setup.",
            localMasterKeyBytes.length);
        return settingsBuilder.build();
      }
    } catch (Exception e) {
      log.error("Failed to read or decode the CSFLE key file.", e);
      return settingsBuilder.build();
    }

    Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
    kmsProviders.put("local", Map.of("key", localMasterKeyBytes));

    // 2. Define the Encryption Schema
    String collectionName = "obfuscation_config";
    String namespace = targetDatabase + "." + collectionName;

    BsonDocument schema = BsonDocument.parse("""
        {
          "bsonType": "object",
          "properties": {
            "orthogonal_matrix": {
              "encrypt": {
                "bsonType": "array",
                "algorithm": "AEAD_AES_256_CBC_HMAC_SHA_512-Random"
              }
            }
          }
        }
        """);

    Map<String, BsonDocument> schemaMap = new HashMap<>();
    schemaMap.put(namespace, schema);

    // 3. Configure the offline crypt_shared library (Optional)
    Map<String, Object> extraOptions = new HashMap<>();
    String cryptSharedPath = "/usr/lib/mongo_crypt_v1.so";
    if (Files.exists(Paths.get(cryptSharedPath))) {
        log.info("Found crypt_shared library at {}. Enabling high-performance offline encryption.", cryptSharedPath);
        extraOptions.put("cryptSharedLibPath", cryptSharedPath);
        extraOptions.put("cryptSharedLibRequired", true);
    } else {
        log.warn("crypt_shared library NOT found at {}. mongocryptd is also unavailable in this environment.", cryptSharedPath);
        log.warn("SYSTEM WILL START WITHOUT CSFLE. Dynamic encryption/decryption will fail.");
        Arrays.fill(localMasterKeyBytes, (byte) 0);
        return settingsBuilder.build();
    }

    AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
        .keyVaultNamespace(keyVaultNamespace)
        .kmsProviders(kmsProviders)
        .schemaMap(schemaMap)
        .extraOptions(extraOptions)
        .build();

    // 4. Attach CSFLE settings to the core MongoDB configuration
    settingsBuilder.autoEncryptionSettings(autoEncryptionSettings);

    Arrays.fill(localMasterKeyBytes, (byte) 0);
    log.info("CSFLE successfully injected into MongoClient Settings. Target Namespace: {}", namespace);

    return settingsBuilder.build();
  }
}