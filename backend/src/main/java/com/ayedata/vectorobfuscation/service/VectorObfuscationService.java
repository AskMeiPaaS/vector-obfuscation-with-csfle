package com.ayedata.vectorobfuscation.service;

import com.ayedata.vectorobfuscation.domain.ObfuscationConfig;
import com.ayedata.vectorobfuscation.math.OrthogonalTransformationService;
import com.ayedata.vectorobfuscation.repository.ObfuscationConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class VectorObfuscationService {

    private static final Logger log = LoggerFactory.getLogger(VectorObfuscationService.class);

    private final OrthogonalTransformationService mathService;
    private final ObfuscationConfigRepository repository;
    private final com.ayedata.vectorobfuscation.websocket.LogWebSocketHandler logHandler;
    private final AtomicReference<double[][]> cachedMatrix = new AtomicReference<>();

    private static final String CONFIG_ID = "singleton-matrix-config";

    @Value("${VECTOR_DIMENSION:1024}")
    private int vectorDimension;

    public VectorObfuscationService(OrthogonalTransformationService mathService,
            ObfuscationConfigRepository repository,
            com.ayedata.vectorobfuscation.websocket.LogWebSocketHandler logHandler) {
        this.mathService = mathService;
        this.repository = repository;
        this.logHandler = logHandler;
    }

    public double[][] getOrGenerateMatrix() {
        double[][] current = cachedMatrix.get();
        if (current != null)
            return current;

        synchronized (this) {
            current = cachedMatrix.get();
            if (current != null)
                return current;

            log.info("Checking database for existing Orthogonal Matrix...");
            ObfuscationConfig config = repository.findById(CONFIG_ID).orElse(null);

            // Check if existing config matches current dimension requirements
            if (config != null && config.getDimension() == vectorDimension) {
                log.info("Found compatible matrix in database. CSFLE decrypted it transparently.");
                logHandler.broadcastLog("DATABASE", 0, "Retrieved existing " + vectorDimension + "d Orthogonal Matrix");
                cachedMatrix.set(config.getOrthogonalMatrix());
            } else {
                if (config != null) {
                    log.warn("Existing matrix dimension ({}) doesn't match required dimension ({}). Regenerating...", config.getDimension(), vectorDimension);
                }
                
                log.info("Generating new {}x{} orthogonal matrix (this may take a few seconds)...", vectorDimension, vectorDimension);
                logHandler.broadcastLog("MATH", 0, "Generating new " + vectorDimension + "x" + vectorDimension + " Orthogonal Matrix...");
                
                long startMath = System.currentTimeMillis();
                double[][] newMatrix = mathService.generateOrthogonalMatrix(vectorDimension);
                long mathLatency = System.currentTimeMillis() - startMath;

                config = new ObfuscationConfig();
                config.setId(CONFIG_ID);
                config.setDimension(vectorDimension);
                config.setOrthogonalMatrix(newMatrix);

                repository.save(config);
                log.info("New matrix saved to database. CSFLE encrypted it securely.");
                logHandler.broadcastLog("DATABASE", mathLatency, "New " + vectorDimension + "d Matrix generated and persisted");

                cachedMatrix.set(newMatrix);
            }
            return cachedMatrix.get();
        }
    }

    public double[] obfuscate(double[] rawVector) {
        long startTime = System.currentTimeMillis();
        double[][] matrix = getOrGenerateMatrix();
        double[] result = mathService.obfuscateVector(rawVector, matrix);
        long duration = System.currentTimeMillis() - startTime;

        logHandler.broadcastLog("OBFUSCATION", duration, "Vector obfuscated successfully. Dimensions: " + rawVector.length);
        return result;
    }
}