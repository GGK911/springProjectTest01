package com.ggk911.springtest01.util;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Java 代码生成 PKCS#10 规范的证书签名请求 CSR
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-22 16:31
 **/
public class CsrUtil {

    private static final Provider BC = new BouncyCastleProvider();

    /**
     * 生成PKCS#10格式的CSR
     *
     * @param isRsaNotEcc {@code true}：使用 RSA 加密算法；{@code false}：使用 ECC（SM2）加密算法
     * @return P10证书签名请求 Base64 字符串
     */
    public static String generateCsr(boolean isRsaNotEcc, String sn) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, IOException {
        // 使用 RSA/ECC 算法，生成密钥对（公钥、私钥）
        KeyPairGenerator generator = KeyPairGenerator.getInstance(isRsaNotEcc ? "RSA" : "EC", BC);
        if (isRsaNotEcc) {
            // RSA
            generator.initialize(2048);
        } else {
            // ECC
            generator.initialize(new ECGenParameterSpec("sm2p256v1"));
        }
        KeyPair keyPair = generator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // 打印私钥，注意：请务必保存您的私钥"EC PRIVATE KEY" "EC PUBLIC KEY"
        System.out.println("----------打印私钥");
        String privateKeyStr = isRsaNotEcc ? PemFormatUtil.priKeyToPem(privateKey) : PemFormatUtil.pemFormat("EC PRIVATE KEY", privateKey.getEncoded());
        System.out.println(privateKeyStr);
        System.out.println("----------打印公钥");
        String publicKeyStr = isRsaNotEcc ? PemFormatUtil.pubKeyToPem(publicKey) : PemFormatUtil.pemFormat("EC PUBLIC KEY", publicKey.getEncoded());
        System.out.println(publicKeyStr);

        // 按需添加证书主题项，
        // 有些 CSR 不需要我们在主题项中添加各字段,
        // 如 `C=CN, CN=吴仙杰, E=wuxianjiezh@gmail.com, OU=3303..., L=杭州, S=浙江`，
        // 而是通过额外参数提交，故这里我只简单地指定了国家码
        /*
          CN common name (域名)
          OU Organizational unit (部门)
          O  Organization Name （组织）
          L  Location
          ST  State
          C  Country
          SN device serial number name
         */
        String subjectParam = "CN=*.dlyd.com,OU=IT,O=dlyd,L=Chongqing,ST=Chongqing," +
                "C=CN," +
                "SERIALNUMBER=" + sn;
        X500Principal subject = new X500Principal(subjectParam);

        // 使用私钥和 SHA256WithRSA/SM3withSM2 算法创建签名者对象
        ContentSigner signer = new JcaContentSignerBuilder(isRsaNotEcc ? "SHA256WithRSA" : "SM3withSM2")
                .setProvider(BC)
                .build(privateKey);

        // 创建 CSR
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);
        PKCS10CertificationRequest csr = builder.build(signer);

        // 打印 OpenSSL PEM 格式文件字符串
        System.out.println("----------打印PEM 格式CSR");
        String csrStr = PemFormatUtil.csrToPem(csr);
        System.out.println(csrStr);

        // 以 Base64 字符串形式返回 CSR
        String baseStr = Base64.getEncoder().encodeToString(csr.getEncoded());
        System.out.println("----------打印Base64格式CSR");
        System.out.println(baseStr);
        return baseStr;
    }

    /**
     * CSR字符串转证书签名请求对象
     *
     * @param csrStr PKCS#10 PEM CSR完整字符串
     * @author Linwei
     */
    public static PKCS10CertificationRequest convertPemToPKCS10CertificationRequest(String csrStr) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        PKCS10CertificationRequest csr = null;
        ByteArrayInputStream pemStream = null;
        pemStream = new ByteArrayInputStream(csrStr.getBytes(StandardCharsets.UTF_8));
        Reader pemReader = new BufferedReader(new InputStreamReader(pemStream));
        PEMParser pemParser = new PEMParser(pemReader);
        Object parsedObj = pemParser.readObject();
        if (parsedObj instanceof PKCS10CertificationRequest) {
            csr = (PKCS10CertificationRequest) parsedObj;
        }
        return csr;
    }

    /**
     * 读取CSR中的主题信息
     *
     * @param asn1ObjectIdentifier 主题OID
     * @param x500Name             主题
     */
    public static String getX500Field(String asn1ObjectIdentifier, X500Name x500Name) {
        RDN[] rdnArray = x500Name.getRDNs(new ASN1ObjectIdentifier(asn1ObjectIdentifier));
        String retVal = null;
        for (RDN item : rdnArray) {
            retVal = item.getFirst().getValue().toString();
        }
        return retVal;
    }

    /**
     * 读取CSR中的主题信息
     *
     * @param asn1ObjectIdentifier 主题OID
     * @param x500Name 主题
     */
    public static String getX500Field(ASN1ObjectIdentifier asn1ObjectIdentifier, X500Name x500Name) {
        RDN[] rdnArray = x500Name.getRDNs(asn1ObjectIdentifier);
        String retVal = null;
        for (RDN item : rdnArray) {
            retVal = item.getFirst().getValue().toString();
        }
        return retVal;
    }


    /**
     * 从字符串中加载公钥
     */
    public static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        try {
//			byte[] buffer = StrUtil.bytes(publicKeyStr, CharsetUtil.CHARSET_UTF_8);
            // 注意：先用BASE64解密字符串, 否则会报错误：invalid key format ssl invalid key format
            byte[] buffer = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从字符串中加载私钥
     */
    public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加密
     */
    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解密
     */
    public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encrypted);
    }

}

