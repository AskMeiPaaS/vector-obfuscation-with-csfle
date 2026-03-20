package com.ayedata.vectorobfuscation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "obfuscation_config")
public class ObfuscationConfig {

    @Id
    private String id;

    private int dimension;

    @Field("orthogonal_matrix")
    private double[][] orthogonalMatrix;

    // Standard Java Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public double[][] getOrthogonalMatrix() {
        return orthogonalMatrix;
    }

    public void setOrthogonalMatrix(double[][] orthogonalMatrix) {
        this.orthogonalMatrix = orthogonalMatrix;
    }
}