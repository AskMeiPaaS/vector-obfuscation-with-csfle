package com.ayedata.vectorobfuscation.dto;

public record ObfuscationResponse(
        double[] obfuscatedVector,
        long latencyMs) {
}