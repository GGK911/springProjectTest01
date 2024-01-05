package com.ggk911.springtest01.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 发送邮件工具类
 *
 * @author TangHaoKai
 * @version 1.0 2023/8/10 15:04
 */
@Slf4j
@Service
public class SendEmailUtil {
    private JavaMailSender mailSender;

    private void iniSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.163.com");
        mailSender.setProtocol("smtp");
        mailSender.setPort(587);
        mailSender.setUsername("");
        mailSender.setPassword("");
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        this.mailSender = mailSender;
    }

    /**
     * 发送邮件（带附件）
     *
     * @param subject             标题
     * @param content             内容
     * @param isHtml              是否html
     * @param receiveAddressArray 收件地址
     * @param inputStream         输入流
     * @param fileOriName         文件全名
     */
    @Async("SendEmailThreadPool")
    public void sendMailWithAttachment(String subject, String content, boolean isHtml, String receiveAddressArray, InputStream inputStream, String fileOriName) {
        iniSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("");
            helper.setSubject(subject);
            helper.setTo(receiveAddressArray);
            helper.setText(content, isHtml);
            helper.addAttachment(fileOriName, new ByteArrayResource(IOUtils.toByteArray(inputStream)));
            mailSender.send(mimeMessage);
        } catch (MessagingException | IOException e) {
            log.error("发送邮件错误,地址：{}；异常：{}", receiveAddressArray, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送邮件（带附件）
     *
     * @param subject             标题
     * @param content             内容
     * @param isHtml              是否html
     * @param receiveAddressArray 收件地址
     * @param bytes               文件byte[]
     * @param fileOriName         文件全名
     */
    @Async("SendEmailThreadPool")
    public void sendMailWithAttachment(String subject, String content, boolean isHtml, String receiveAddressArray, byte[] bytes, String fileOriName) {
        iniSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("");
            helper.setSubject(subject);
            helper.setTo(receiveAddressArray);
            helper.setText(content, isHtml);
            helper.addAttachment(fileOriName, new ByteArrayResource(bytes));
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("发送邮件错误,地址：{}；异常：{}", receiveAddressArray, e.getMessage());
            e.printStackTrace();
        }
    }
}
