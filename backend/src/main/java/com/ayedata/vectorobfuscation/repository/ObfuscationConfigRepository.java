package com.ayedata.vectorobfuscation.repository;

import com.ayedata.vectorobfuscation.domain.ObfuscationConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObfuscationConfigRepository extends MongoRepository<ObfuscationConfig, String> {
}