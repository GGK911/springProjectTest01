package com.ggk911.springtest01.service.impl;

import cn.com.mcsca.extend.SecuEngine;
import cn.com.mcsca.util.CertUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.ggk911.springtest01.util.CsrUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * 证书服务
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-22 17:59
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class CertServiceImpl {

    /**
     * 构造PKCS10请求文件CSR
     *
     * @param isRsaNotEcc RSA OR SM2
     * @param sn          序列号
     * @return P10
     */
    @SneakyThrows
    public Object createP10(boolean isRsaNotEcc,
                            String sn) {
        JSONObject json = new JSONObject();
        json.set("p10", StrUtil.removeAllLineBreaks(CsrUtil.generateCsr(isRsaNotEcc, sn)));
        return json;
    }

    /**
     * HEX解码
     *
     * @param hex HEX字符串
     * @return 解码字节流
     */
    public String hexDecode(String hex) {
        return new String(Hex.decode(hex));
    }

    /**
     * HEX编码
     *
     * @param bytes 字节流
     * @return HEX字符串
     */
    public String hexEncode(byte[] bytes) {
        return Hex.toHexString(bytes);
    }

    /**
     * BASE64解码
     *
     * @param base64 BASE64字符串
     * @return 解码字节流
     */
    public String base64Decode(String base64) {
        return new String(Base64.decode(base64));
    }

    /**
     * BASE64编码
     *
     * @param bytes 字节流
     * @return BASE64字符串
     */
    public String base64Encode(byte[] bytes) {
        return Base64.toBase64String(bytes);
    }

    /**
     * HEX编码转BASE64编码
     *
     * @param hex HEX字符串
     * @return BASE64字符串
     */
    public String hexToBase64(String hex) {
        return Base64.toBase64String(Hex.decode(hex));
    }

    /**
     * BASE64编码转HEX编码
     *
     * @param base64 BASE64字符串
     * @return HEX字符串
     */
    public String base64ToHex(String base64) {
        return Hex.toHexString(Base64.decode(base64));
    }

    /**
     * 解析PFX信息
     *
     * @param pfxFile PFX文件
     * @param pwd     密码
     * @return PFX信息
     */
    @SneakyThrows
    public Map<String, String> analysisPfx(MultipartFile pfxFile, String pwd) {
        byte[] pfxBytes = pfxFile.getBytes();
        SecuEngine secuEngine = new SecuEngine();
        //公钥
        String pubKeyCert = secuEngine.ParsingPfx(new ByteArrayInputStream(pfxBytes), pwd, 1);
        //私钥证书
        String priKey = secuEngine.ParsingPfx(new ByteArrayInputStream(pfxBytes), pwd, 2);

        String startDate = CertUtil.parseCert(pubKeyCert, String.valueOf(4));
        String endDate = CertUtil.parseCert(pubKeyCert, String.valueOf(5));
        String subject = CertUtil.parseCert(pubKeyCert, String.valueOf(1));
        String serialNumber = CertUtil.parseCert(pubKeyCert, String.valueOf(2));
        String isuseStr = CertUtil.parseCert(pubKeyCert, String.valueOf(3));
        String isuse = isuseStr.substring(isuseStr.indexOf("CN=") + 3);
        // 不解算法 暂时没用 测试环境报错
        String alg = CertUtil.parseCert(pubKeyCert, String.valueOf(9));
        // 目前只能解析MCSCA颁发的证书
        String idNum = subject.substring(subject.indexOf("SERIALNUMBER=") + 13, subject.indexOf(",CN="));
        String[] splitStr = subject.split("@");
        String award = "";
        if (splitStr.length > 1) {
            award = splitStr[1];
        }
        boolean isRsa = alg.startsWith("1.2.840.113549.1.1.1");
        Map<String, String> map = new HashMap<>(10);
        map.put("pubKeyCert", pubKeyCert);
        map.put("priKey", priKey);
        map.put("certEffectiveDate", startDate);
        map.put("certExpirationDate", endDate);
        map.put("serialNumber", serialNumber);
        map.put("idNum", idNum);
        map.put("award", award);
        map.put("isRsa", Boolean.toString(isRsa));
        map.put("isuse", isuse);
        map.put("certSubject", subject);
        return map;
    }
}
