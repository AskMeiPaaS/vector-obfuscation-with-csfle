package com.ayedata.vectorobfuscation.repository;

import com.ayedata.vectorobfuscation.domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    // Standard CRUD operations provided by MongoRepository
}
