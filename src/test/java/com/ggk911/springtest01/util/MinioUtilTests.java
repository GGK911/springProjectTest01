package com.ggk911.springtest01.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.ggk911.springtest01.config.properties.MinioConfig;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MinioUtil测试
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-23 17:02
 **/
@SpringBootTest
public class MinioUtilTests {
    @Autowired
    MinioUtil minioUtil;
    @Autowired
    MinioConfig minioConfig;

    /**
     * 文件桶测试
     */
    @Test
    public void bucketExists() {
        assertTrue(minioUtil.bucketExists(minioConfig.getBucketName()));
    }

    /**
     * 上传文件测试
     */
    @Test
    public void uploadFile() {
        byte[] bytes = ResourceUtil.readBytes("C:\\Users\\ggk911\\IdeaProjects\\springTest01\\src\\test\\resources\\图片1.png");
        String upload = minioUtil.upload(bytes, "测试图片1.png");
        System.out.println(upload);
        assertNotNull(upload);
    }

    /**
     * 预览图片
     */
    @Test
    public void previewImage() {
        String preview = minioUtil.preview("2023-11/23/873b3e172044432297645394f0136f03.png");
        System.out.println(preview);
        assertNotNull(preview);
    }

    /**
     * 获取全部bucket
     */
    @Test
    public void listBuckets() {
        List<Bucket> buckets = minioUtil.getAllBuckets();
        System.out.println(JSONUtil.parse(buckets));
        assertNotNull(buckets);
    }

    /**
     * 文件下载
     */
    @Test
    public void downloadFile() {
        byte[] download = minioUtil.download("2023-11/23/4bf5174afdc943dea1004bd012b35d77.png");
        assertNotNull(download);
    }

    /**
     * 查看文件对象
     */
    @Test
    public void allObject() {
        List<Item> itemList = minioUtil.listObjects(true, "");
        List<String> collect = itemList.stream().map(Item::objectName).collect(Collectors.toList());
        System.out.println(JSONUtil.parse(collect).toStringPretty());
        assertNotNull(itemList);
    }

    /**
     * 删除
     */
    @Test
    public void deleteFile() {
        boolean remove = minioUtil.remove("2023-11/23/4bf5174afdc943dea1004bd012b35d77.png");
        System.out.println(remove);
        assertTrue(remove);
    }
}
