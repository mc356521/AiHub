package com.example.aihub.service;

import com.example.aihub.entity.Resources;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * <p>
 * 存储上传的教学资源，如课件、视频、文档等 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-21
 */
public interface ResourcesService extends IService<Resources> {
    /**
     * 上传资源文件
     *
     * @param file     上传的文件
     * @param title    资源标题
     * @param courseId 关联的课程ID (可选)
     * @return 包含资源信息的实体
     * @throws IOException 文件读写异常
     */
    Resources uploadFile(MultipartFile file, String title, Integer courseId) throws IOException;

    /**
     * 根据文件路径加载文件为Spring资源对象
     *
     * @param filePath 文件在服务器上的存储路径
     * @return Spring的Resource对象，如果找不到则抛出异常
     * @throws MalformedURLException 路径格式错误时抛出
     */
    Resource loadFileAsResource(String filePath) throws MalformedURLException;
}
