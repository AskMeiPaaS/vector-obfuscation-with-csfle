package com.ayedata.vectorobfuscation.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrthogonalTransformationService {
    private static final Logger log = LoggerFactory.getLogger(OrthogonalTransformationService.class);

    /**
     * Generates a random Orthogonal Matrix of size N x N using QR Decomposition.
     * * @param dimension The size of the embedding vector (e.g., 384, 768, 1536)
     * 
     * @return A 2D array representing the orthogonal matrix Q
     */
    public double[][] generateOrthogonalMatrix(int dimension) {
        log.info("Generating a {}x{} random Orthogonal Matrix via QR Decomposition...", dimension, dimension);

        long startTime = System.currentTimeMillis();
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        RealMatrix randomMatrix = new Array2DRowRealMatrix(dimension, dimension);

        // 1. Populate the matrix with random Gaussian values
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                randomMatrix.setEntry(i, j, randomDataGenerator.nextGaussian(0.0, 1.0));
            }
        }

        // 2. Perform QR Decomposition to extract the orthogonal matrix (Q)
        QRDecomposition qrDecomposition = new QRDecomposition(randomMatrix);
        RealMatrix qMatrix = qrDecomposition.getQ();

        long latency = System.currentTimeMillis() - startTime;
        log.info("Orthogonal Matrix generated successfully in {} ms.", latency);

        return qMatrix.getData();
    }

    /**
     * Obfuscates a raw embedding vector by multiplying it with the Orthogonal
     * Matrix.
     * * @param rawVector The original vector $v$
     * 
     * @param orthogonalMatrix The orthogonal matrix $Q$
     * @return The obfuscated vector $v'$
     */
    public double[] obfuscateVector(double[] rawVector, double[][] orthogonalMatrix) {
        if (rawVector.length != orthogonalMatrix.length) {
            throw new IllegalArgumentException(
                    "Vector dimension (" + rawVector.length + ") does not match Matrix dimension ("
                            + orthogonalMatrix.length + ")");
        }

        // Convert raw arrays to Commons Math objects
        RealMatrix q = MatrixUtils.createRealMatrix(orthogonalMatrix);
        RealVector v = MatrixUtils.createRealVector(rawVector);

        // Multiply: Q * v
        RealVector obfuscatedVector = q.operate(v);

        return obfuscatedVector.toArray();
    }
}