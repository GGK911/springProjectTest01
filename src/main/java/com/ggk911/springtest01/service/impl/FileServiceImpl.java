package com.ggk911.springtest01.service.impl;

import com.ggk911.springtest01.util.MinioUtil;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TangHaoKai
 * @version V1.0 2023-11-27 15:42
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl {
    private final MinioUtil minioUtil;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件路径
     */
    public String uploadFile(MultipartFile file) {
        return minioUtil.upload(file);
    }

    /**
     * 预览图片(获取图片链接)
     *
     * @param filePath 文件路径
     * @return 图片链接
     */
    public String previewImage(String filePath) {
        return minioUtil.preview(filePath);
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @param response 响应
     */
    public void downloadFile(String filePath, HttpServletResponse response) {
        minioUtil.download(filePath, response);
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除
     */
    public boolean removeFile(String filePath) {
        return minioUtil.remove(filePath);
    }

    /**
     * 查询文件
     *
     * @param filePath 文件路径
     * @return 文件名List
     */
    public List<String> queryFile(String filePath) {
        return minioUtil.listObjects(true, filePath).stream().map(Item::objectName).collect(Collectors.toList());
    }
}
