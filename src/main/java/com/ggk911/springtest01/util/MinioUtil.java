package com.ggk911.springtest01.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.ggk911.springtest01.config.properties.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TangHaoKai
 * @version V1.0 2023-11-23 16:41
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {
    private final MinioConfig minioConfig;
    private final MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     *
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     *
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     *
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取全部bucket
     */
    public List<Bucket> getAllBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            return buckets;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件上传
     *
     * @param bytes    文件
     * @param fileName 文件名称
     * @return 路径
     */
    public String upload(byte[] bytes, String fileName) {
        return upload(bytes, fileName, "application/octet-stream");
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @return Boolean
     */
    @SneakyThrows
    public String upload(MultipartFile file) {
        return upload(file.getBytes(), file.getOriginalFilename(), file.getContentType());
    }

    /**
     * 文件上传
     *
     * @param bytes       文件
     * @param fileName    文件名称
     * @param contentType 文件类型
     * @return 路径
     */
    public String upload(byte[] bytes, String fileName, String contentType) {
        String objectName = DateUtil.format(DateUtil.date(), "yyyy-MM/dd") + "/" + UUID.fastUUID().toString(true) + "." + FileUtil.extName(fileName);
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType).build();
            // 文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectName;
    }

    /**
     * 预览图片
     *
     * @param filePath 文件路径
     * @return 预览URL
     */
    public String preview(String filePath) {
        // 查看文件地址
        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder()
                .bucket(minioConfig.getBucketName())
                .object(filePath)
                .method(Method.GET).build();
        try {
            return minioClient.getPresignedObjectUrl(build);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public byte[] download(String filePath) {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(filePath).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            return IOUtils.toByteArray(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件下载到http响应
     *
     * @param filePath 文件路径
     * @param res      response
     */
    public void download(String filePath, HttpServletResponse res) {
        download(filePath, filePath.substring(filePath.lastIndexOf("/") + 1), res);
    }

    /**
     * 文件下载到http响应(文件重命名)
     *
     * @param filePath 文件路径
     * @param fileName 重命名文件名称
     * @param res      response
     */
    @SneakyThrows
    public void download(String filePath, String fileName, HttpServletResponse res) {
        byte[] bytes = download(filePath);
        ServletOutputStream outputStream = res.getOutputStream();
        res.setCharacterEncoding("utf-8");
        // 设置强制下载不打开
        // res.setContentType("application/force-download");
        res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.write(bytes, outputStream);
    }

    /**
     * 查看文件对象
     *
     * @param isRecursive 是否递归
     * @return 存储bucket内文件对象信息
     */
    public List<Item> listObjects(boolean isRecursive, String prefix) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .recursive(isRecursive)
                        .prefix(prefix)
                        .build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }

    /**
     * 删除
     *
     * @param filePath 文件路径
     * @return 删除成功
     */
    public boolean remove(String filePath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucketName()).object(filePath).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
