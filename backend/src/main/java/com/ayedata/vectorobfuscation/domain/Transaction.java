package com.ayedata.vectorobfuscation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    private String merchant;

    private Double amount;

    private String description;

    private Instant timestamp;

    @Field("obfuscated_vector")
    private double[] obfuscatedVector;

    @org.springframework.data.annotation.Transient
    private double similarityScore;

    public Transaction() {}

    public Transaction(String id, String merchant, Double amount, String description, Instant timestamp, double[] obfuscatedVector) {
        this.id = id;
        this.merchant = merchant;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.obfuscatedVector = obfuscatedVector;
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public double[] getObfuscatedVector() { return obfuscatedVector; }
    public void setObfuscatedVector(double[] obfuscatedVector) { this.obfuscatedVector = obfuscatedVector; }
    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }

    public static class TransactionBuilder {
        private String id;
        private String merchant;
        private Double amount;
        private String description;
        private Instant timestamp;
        private double[] obfuscatedVector;

        public TransactionBuilder id(String id) { this.id = id; return this; }
        public TransactionBuilder merchant(String merchant) { this.merchant = merchant; return this; }
        public TransactionBuilder amount(Double amount) { this.amount = amount; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public TransactionBuilder obfuscatedVector(double[] obfuscatedVector) { this.obfuscatedVector = obfuscatedVector; return this; }

        public Transaction build() {
            return new Transaction(id, merchant, amount, description, timestamp, obfuscatedVector);
        }
    }
}
