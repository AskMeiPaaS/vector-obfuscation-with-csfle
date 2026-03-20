package com.ayedata.vectorobfuscation.dto;

import java.time.Instant;

public record TransactionRequest(String merchant, Double amount, String description) {}
