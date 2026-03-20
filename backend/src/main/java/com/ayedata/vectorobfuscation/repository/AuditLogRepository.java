package com.ayedata.vectorobfuscation.repository;

import com.ayedata.vectorobfuscation.domain.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
}
