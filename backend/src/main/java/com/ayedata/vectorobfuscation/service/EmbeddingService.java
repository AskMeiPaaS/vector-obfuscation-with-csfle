package com.ayedata.vectorobfuscation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private final RestTemplate restTemplate;

    @Value("${OLLAMA_URL:http://ollama-engine:11434/api/embeddings}")
    private String ollamaUrl;

    @Value("${OLLAMA_MODEL:nub235/voyage-4-nano}")
    private String modelName;

    @Value("${VECTOR_DIMENSION:1024}")
    private int vectorDimension;

    public EmbeddingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public double[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new double[vectorDimension];
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", modelName);
            request.put("prompt", text); // Standard for /api/embeddings

            log.info("Requesting embedding from Ollama model: {} for text: '{}'", modelName, text);
            Map<String, Object> response = restTemplate.postForObject(ollamaUrl, request, Map.class);

            if (response != null) {
                // Handle /api/embeddings (returns "embedding": [...])
                if (response.containsKey("embedding")) {
                    List<Double> embeddingList = (List<Double>) response.get("embedding");
                    return embeddingList.stream().mapToDouble(Double::doubleValue).toArray();
                } 
                // Handle /api/embed (returns "embeddings": [[...]])
                else if (response.containsKey("embeddings")) {
                    List<List<Double>> listGroup = (List<List<Double>>) response.get("embeddings");
                    if (!listGroup.isEmpty()) {
                        return listGroup.get(0).stream().mapToDouble(Double::doubleValue).toArray();
                    }
                }
            }
            
            log.warn("Ollama returned empty or unrecognized response format: {}. Falling back to zeros.", response);
            return new double[vectorDimension];
        } catch (Exception e) {
            log.error("Error calling Ollama embedding API: {}. Make sure OLLAMA_MODEL is pulled and container is ready.", e.getMessage());
            return new double[vectorDimension]; 
        }
    }
}
