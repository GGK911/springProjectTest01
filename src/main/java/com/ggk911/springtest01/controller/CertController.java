package com.ggk911.springtest01.controller;

import com.ggk911.springtest01.service.impl.CertServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 证书
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-22 17:58
 **/
@RestController
@Slf4j
@RequiredArgsConstructor
public class CertController {
    private final CertServiceImpl certService;

    /**
     * 构建P10（csr请求）
     *
     * @param isRsaNotEcc RSA OR SM2 ？
     * @param sn          序列号
     * @return P10
     */
    @PostMapping("/cert/createP10")
    public Object createP10(boolean isRsaNotEcc, String sn) {
        return certService.createP10(isRsaNotEcc, sn);
    }

    /**
     * HEX解码
     *
     * @param hex HEX字符串
     * @return 解码字节流
     */
    @PostMapping("/cert/hexDecode")
    public Object hexDecode(String hex) {
        return certService.hexDecode(hex);
    }

    /**
     * HEX编码
     *
     * @param bytes 字节流
     * @return HEX字符串
     */
    @PostMapping("/cert/hexEncode")
    public Object hexEncode(byte[] bytes) {
        return certService.hexEncode(bytes);
    }

    /**
     * BASE64解码
     *
     * @param base64 BASE64字符串
     * @return 解码字节流
     */
    @PostMapping("/cert/base64Decode")
    public Object base64Decode(String base64) {
        return certService.base64Decode(base64);
    }

    /**
     * BASE64编码
     *
     * @param bytes 字节流
     * @return BASE64字符串
     */
    @PostMapping("/cert/base64Encode")
    public Object base64Encode(byte[] bytes) {
        return certService.base64Encode(bytes);
    }

    /**
     * HEX编码转BASE64编码
     *
     * @param hex HEX字符串
     * @return BASE64字符串
     */
    @PostMapping("/cert/hexToBase64")
    public Object hexToBase64(String hex) {
        return certService.hexToBase64(hex);
    }

    /**
     * BASE64编码转HEX编码
     *
     * @param base64 BASE64字符串
     * @return HEX字符串
     */
    @PostMapping("/cert/base64ToHex")
    public Object base64ToHex(String base64) {
        return certService.base64ToHex(base64);
    }

    /**
     * 解析PFX信息
     *
     * @param pfxFile PFX文件
     * @param pwd     密码
     * @return PFX信息
     */
    @PostMapping("/cert/analysisPfx")
    public Object analysisPfx(MultipartFile pfxFile, String pwd) {
        return certService.analysisPfx(pfxFile, pwd);
    }
}
