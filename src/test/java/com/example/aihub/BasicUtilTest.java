package com.example.aihub;

import com.example.aihub.util.BasicUtil;
import org.junit.jupiter.api.Test;

/**
 * @created: ii_kun
 * @createTime: 2025/6/17 11:05
 * @email: weijikun1@icloud.com
 */
public class BasicUtilTest {

    @Test
    void testBasicUtil() {
        BasicUtil basicUtil = new BasicUtil();
        System.out.println("生成的随机口令: " + basicUtil.getRandomCommand());
    }

}
