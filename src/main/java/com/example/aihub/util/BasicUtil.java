package com.example.aihub.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

/**
 *
 * 工具类
 *
 * @created: ii_kun
 * @createTime: 2025/6/17 11:02
 * @email: weijikun1@icloud.com
 */
public class BasicUtil {

    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    /**
     * 随机生成6-8位数的班级口令
     *
     * @return 随机班级口令字符串
     */
    public static String getRandomCommand() {
        int length = 6 + random.nextInt(3); // 6~8位
        StringBuilder command = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            command.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }
        return command.toString();
    }
}
