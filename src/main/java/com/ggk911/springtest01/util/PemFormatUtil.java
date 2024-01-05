package com.ggk911.springtest01.util;

import lombok.SneakyThrows;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * PEM格式化工具
 *
 * @author TangHaoKai
 * @version V1.0 2023-12-01 16:00
 **/
public class PemFormatUtil {
    /**
     * PEM格式化私钥
     *
     * @param priKey 私钥
     * @return 私钥PEM格式
     */
    @SneakyThrows
    public static String priKeyToPem(PrivateKey priKey) {
        System.out.println("----------打印私钥");
        return pemFormat("PRIVATE KEY", priKey.getEncoded());
    }

    /**
     * PEM格式化公钥
     *
     * @param pubKey 公钥
     * @return 公钥PEM格式
     */
    @SneakyThrows
    public static String pubKeyToPem(PublicKey pubKey) {
        System.out.println("----------打印公钥");
        return pemFormat("PUBLIC KEY", pubKey.getEncoded());
    }

    /**
     * PEM格式化
     *
     * @param pemPrefix pem前缀
     * @param encoded   数据
     * @return PEM格式
     */
    @SneakyThrows
    public static String pemFormat(String pemPrefix, byte[] encoded) {
        PemObject privateKeyPem = new PemObject(pemPrefix, encoded);
        StringWriter priStrWriter = new StringWriter();
        PemWriter priPemWriter = new PemWriter(priStrWriter);
        priPemWriter.writeObject(privateKeyPem);
        priPemWriter.close();
        priStrWriter.close();
        System.out.println(priStrWriter);
        return priStrWriter.toString();
    }

    /**
     * PEM格式化P10
     *
     * @param scr P10请求
     * @return PEM格式
     */
    @SneakyThrows
    public static String csrToPem(PKCS10CertificationRequest scr) {
        System.out.println("----------打印P10");
        return pemFormat("CERTIFICATE REQUEST", scr.getEncoded());
    }
}
