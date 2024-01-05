package com.ggk911.springtest01.service.impl;

import cn.com.mcsca.extend.SecuEngine;
import cn.com.mcsca.pki.core.util.SignatureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ggk911.springtest01.util.CreateSealUtil;
import com.ggk911.springtest01.util.pdf.PdfParameterEntity;
import com.ggk911.springtest01.util.pdf.PdfUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用公共服务
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-23 10:50
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonServiceImpl {

    /**
     * 生成签名值
     *
     * @param priKey  加签私钥
     * @param reqHead 请求头
     * @param reqBody 请求体
     * @return 签名json
     */
    public Object createSignValue(String priKey, String reqHead, String reqBody) {
        log.info("开始生产签名值，priKey={},reqHead={},reqBody={}", priKey, reqHead, reqBody);
        Map<String, String> reqMap = new HashMap<>(2);
        reqMap.put("reqHead", reqHead);
        reqMap.put("reqBody", reqBody);
        String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(reqMap, SerializerFeature.MapSortField, SerializerFeature.SortField);
        String sign = "";
        try {
            sign = SignatureUtil.doSign(priKey, jsonString);
        } catch (Exception e) {
            log.error("签名私钥...{}", priKey);
            e.printStackTrace();
        }
        Map<String, Object> resBody = new HashMap<>(1);
        resBody.put("signValue", sign);
        log.info("签名值...{}", sign);
        return JSONUtil.parse(resBody);
    }

    /**
     * 验签
     *
     * @param pubKey    公钥
     * @param reqHead   请求头
     * @param reqBody   请求体
     * @param signValue 签名
     * @return 验签结果
     */
    public Object verifySignValue(String pubKey, String reqHead, String reqBody, String signValue) {
        log.info("开始验证签名值");
        Map<String, String> reqMap = new HashMap<>(2);
        reqMap.put("reqHead", reqHead);
        reqMap.put("reqBody", reqBody);
        String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(reqMap, SerializerFeature.MapSortField, SerializerFeature.SortField);
        boolean verify = false;
        try {
            SecuEngine secuEngine = new SecuEngine();
            verify = secuEngine.VerifySignDataWithSM2ByPublicKey(pubKey, jsonString.getBytes(), signValue);
        } catch (Exception e) {
            log.error("验签公钥...{}", pubKey);
            e.printStackTrace();
        }
        Map<String, Object> resBody = new HashMap<>(1);
        resBody.put("verify", verify);
        log.info("验签结果...{}", verify);
        return JSONUtil.parse(resBody);
    }

    /**
     * 生成个人方章
     *
     * @param name     名称
     * @param response 响应
     */
    @SneakyThrows
    public void createPersonSeal(String name, HttpServletResponse response) {
        log.info("开始生成图章，name={}", name);
        byte[] sealBytes = CreateSealUtil.createSquareSeal(name);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(sealBytes);
        } catch (Exception e) {
            log.error("文件响应失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 生成企业圆章
     *
     * @param name     企业名称
     * @param response 响应
     */
    @SneakyThrows
    public void createEnterpriseSeal(String name, HttpServletResponse response) {
        log.info("开始生成图章，name={}", name);
        byte[] sealBytes = CreateSealUtil.createCircleSeal(name);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(sealBytes);
        } catch (Exception e) {
            log.error("文件响应失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 填充PDF
     *
     * @param picFile          图片File
     * @param pdfFile          pdfFile
     * @param textDomainParams 文本域参数
     * @param picDomainParams  图片域参数
     * @param response         响应
     */
    @SneakyThrows
    public void pdfFill(MultipartFile picFile, MultipartFile pdfFile, String textDomainParams, String picDomainParams, HttpServletResponse response) {
        JSONObject textParamsJson = JSONUtil.parseObj(textDomainParams);
        JSONObject picParamsJson = JSONUtil.parseObj(picDomainParams);
        Map<String, Object> params = new HashMap<>();
        params.putAll(textParamsJson.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue()))));
        params.putAll(picParamsJson.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> JSONUtil.toBean(String.valueOf(entry.getValue()), PdfUtil.FillImageParam.class))));
        byte[] pdfFileBytes = pdfFile.getBytes();
        byte[] picFileBytes = picFile.getBytes();
        byte[] pdfFill = PdfUtil.pdfFill(pdfFileBytes, params, picFileBytes);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(pdfFill);
        }
    }

    /**
     * 读取PDF域参数
     *
     * @param pdfFile pdf文件
     * @return 参数信息
     */
    @SneakyThrows
    public JSONObject pdfParams(MultipartFile pdfFile) {
        byte[] pdfFileBytes = pdfFile.getBytes();
        List<PdfParameterEntity> params = new ArrayList<>();
        PdfUtil.getPdfDomain(pdfFileBytes, params, null);
        JSONObject res = new JSONObject();
        res.set("params", params);
        return res;
    }
}
