package com.example.aihub;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MongoDbTest {

    @Autowired
    private MongoTestRepository mongoTestRepository;

    private MongoTestEntity testEntity;

    @BeforeEach
    void setUp() {
        // 清理旧数据，以防上次测试失败残留
        mongoTestRepository.deleteAll();
        // 创建一个测试实体
        testEntity = new MongoTestEntity("testUser");
    }

    @Test
    void testMongoDbConnectionAndCrud() {
        // 1. 保存
        MongoTestEntity savedEntity = mongoTestRepository.save(testEntity);
        assertNotNull(savedEntity.getId(), "实体保存后应该获得ID");

        // 2. 查询
        MongoTestEntity foundEntity = mongoTestRepository.findById(savedEntity.getId()).orElse(null);
        assertNotNull(foundEntity, "应该能根据ID找到保存的实体");

        // 3. 断言
        assertEquals("testUser", foundEntity.getName(), "查询到的实体名称应该与保存时一致");

        System.out.println("MongoDB-ID: " + foundEntity.getId());
        System.out.println("MongoDB-Name: " + foundEntity.getName());
    }

    @AfterEach
    void tearDown() {
        // 清理本次测试创建的数据
        mongoTestRepository.deleteAll();
    }
} 