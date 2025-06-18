package com.example.aihub.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "test_collection")
public class MongoTestEntity {
    @Id
    private String id;
    private String name;

    public MongoTestEntity(String name) {
        this.name = name;
    }
} 