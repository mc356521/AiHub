package com.example.aihub.repository;

import com.example.aihub.entity.MongoTestEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoTestRepository extends MongoRepository<MongoTestEntity, String> {
} 