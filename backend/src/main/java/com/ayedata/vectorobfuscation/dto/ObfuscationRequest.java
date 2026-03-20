package com.ayedata.vectorobfuscation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ObfuscationRequest(
        @NotNull(message = "Vector cannot be null") @NotEmpty(message = "Vector cannot be empty") double[] vector) {
}