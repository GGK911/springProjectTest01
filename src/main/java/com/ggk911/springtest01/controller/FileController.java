package com.ggk911.springtest01.controller;

import com.ggk911.springtest01.service.impl.FileServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author TangHaoKai
 * @version V1.0 2023-11-27 15:42
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileServiceImpl fileService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件路径
     */
    @PostMapping(value = "/file/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    /**
     * 预览图片(获取图片链接)
     *
     * @param filePath 文件路径
     * @return 图片链接
     */
    @PostMapping(value = "/file/preview")
    public String previewImage(@RequestParam("filePath") String filePath) {
        return fileService.previewImage(filePath);
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @param response 响应
     */
    @PostMapping(value = "/file/download")
    public void downloadFile(@RequestParam("filePath") String filePath,
                             HttpServletResponse response) {
        fileService.downloadFile(filePath, response);
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除
     */
    @PostMapping(value = "/file/remove")
    public boolean removeFile(@RequestParam("filePath") String filePath) {
        return fileService.removeFile(filePath);
    }

    /**
     * 查询文件
     *
     * @param filePath 文件路径
     * @return 文件名List
     */
    @PostMapping(value = "/file/query")
    public Object queryFile(@RequestParam("filePath") String filePath) {
        return fileService.queryFile(filePath);
    }
}
