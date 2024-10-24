package com.antares.user.utils;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.HttpCodeEnum;

@Component
public class MailUtil {
    @Value("${spring.mail.username}")
    private String MAIL_SENDER; // 邮件发送者
    @Resource
    private JavaMailSender javaMailSender;// 注入QQ发送邮件的bean

    @Async
    public void sendMail(String mail, String code) {
        MailBean mailBean = new MailBean();
        mailBean.setRecipient(mail);// 接收者
        mailBean.setSubject("Antares Blog验证码");// 标题
        // 内容主体
        mailBean.setContent("您的验证码为：" + code + ", 有效期10分钟。");
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(MAIL_SENDER);// 发送者
            mailMessage.setTo(mailBean.getRecipient());// 接收者
            mailMessage.setSubject(mailBean.getSubject());// 邮件标题
            mailMessage.setText(mailBean.getContent());// 邮件内容
            javaMailSender.send(mailMessage);// 发送邮箱
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.INTERNAL_SERVER_ERROR, "邮件发送失败");
        }
    }
}
