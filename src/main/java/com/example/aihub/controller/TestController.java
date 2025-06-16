package com.example.aihub.controller;

import com.example.aihub.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器，提供用于系统健康检查和功能验证的接口。
 */
@RestController
@RequestMapping("/test")
@Tag(name = "系统测试", description = "用于验证服务可用性的测试接口")
@Slf4j
public class TestController extends BaseController {

    /**
     * 服务可用性测试接口。
     * 访问此接口将返回一个固定的成功消息，用于验证服务是否已成功启动并能够响应请求。
     *
     * @return 包含 "Hello World" 信息的Result响应
     */
    @GetMapping("/hello")
    @Operation(summary = "服务可用性测试", description = "返回一个'Hello World'字符串，用于检测服务是否正常运行")
    public Result<String> hello() {
        log.info("执行服务可用性测试接口 /test/hello");
        return Result.success("Hello World, AIHub is running!");
    }
} 