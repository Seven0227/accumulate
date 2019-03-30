package com.seven.accumulate.email;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @classDesc: (发送邮邮件 建造者模式校验发件人、收件人邮箱等)
 * @Author:
 * @createTime: Created in 13:38 2018/8/17
 */
public class EmailInfo {

    //抄送方式默认是抄送
    public enum RecipientType {
        CC, BCC
    }

    //smtp服务器地址
    private String host;
    //发件邮箱
    private String from;
    //发件邮箱密码
    private String passowrd;
    //收件邮箱
    private List<String> toList;
    //主题
    private String subject;
    //邮件内容
    private String content;
    //邮件附件
    private List<String> attachFileList;
    //抄送方式
    private RecipientType recipientType;
    //抄送人列表
    private List<String> recipientList;


    private EmailInfo(Builder builder) {
        this.host = builder.host;
        this.from = builder.from;
        this.passowrd = builder.passowrd;
        this.toList = builder.toList;
        this.subject = builder.subject;
        this.content = builder.content;
        this.attachFileList = builder.attachFileList;
        this.recipientType = builder.recipientType;
        this.recipientList = builder.recipientList;
    }

    public static class Builder {
        //smtp服务器地址
        private String host;
        //发件邮箱
        private String from;
        //发件邮箱密码
        private String passowrd;
        //收件邮箱
        private List<String> toList;
        //主题
        private String subject;
        //邮件内容
        private String content;
        //邮件附件
        private List<String> attachFileList;
        //抄送方式
        private RecipientType recipientType;
        //抄送人列表
        private List<String> recipientList;

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setPassowrd(String passowrd) {
            this.passowrd = passowrd;
            return this;
        }

        public Builder addTo(String to) {
            if (this.toList == null) {
                this.toList = new ArrayList<>();
            }
            this.toList.add(to);
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder addAttachFile(String attachFile) {
            if (this.attachFileList == null) {
                this.attachFileList = new ArrayList<>();
            }
            this.attachFileList.add(attachFile);
            return this;
        }

        public Builder setRecipientType(RecipientType recipientType) {
            this.recipientType = recipientType;
            return this;
        }

        public Builder addRecipient(String recipient) {
            if (this.recipientList == null) {
                this.recipientList = new ArrayList<>();
            }
            this.recipientList.add(recipient);
            return this;
        }

        public EmailInfo build() throws Exception {
            if (Strings.isNullOrEmpty(host)) {
                throw new RuntimeException("未设置smtp服务器地址");
            }
            if (Strings.isNullOrEmpty(from)) {
                throw new RuntimeException("未设置发件邮箱");
            }
            if (Strings.isNullOrEmpty(passowrd)) {
                throw new RuntimeException("未设置发件邮箱密码");
            }
            if (toList == null || toList.isEmpty()) {
                throw new RuntimeException("未设置收件邮箱");
            }
            if (subject == null) {
                subject = "";
            }
            if (Strings.isNullOrEmpty(content)) {
                throw new RuntimeException("未设置邮件内容");
            }
            if (attachFileList == null) {
                attachFileList = new ArrayList<>();
            }
            if (recipientType == null) {
                this.recipientType = RecipientType.CC;
            }
            if (recipientList == null) {
                recipientList = new ArrayList<>();
            }
            return new EmailInfo(this);
        }

    }


    public void sendEmail() throws Exception {

        //获取连接 1.获得连接 确定连接位置
        Properties properties = new Properties();
        //获取邮箱的smtp服务器的地址
        properties.setProperty("mail.host", host);
        //是否进行权限验证
        properties.setProperty("mail.smtp.auth", "true");
        properties.put("mail.transport.protocol", "smtp");
        //修改端口号
        //SSLSocketFactory类的端口
        properties.put("mail.smtp.socketFactory.port", "465");
        //SSLSocketFactory类
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        //网易提供的ssl加密端口,QQ邮箱也是该端口
        properties.put("mail.smtp.port", "465");

        //获取连接 2.确定权限(账号和密码)
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, passowrd);
            }
        };

        //1.获取连接(连接地址(smtp服务器的地址 + 权限验证) + 确定权限)
        Session session = Session.getDefaultInstance(properties,authenticator);

        //2.创建消息(发件人 + 收件人 + 主题 + 内容)
        Message message = new MimeMessage(session);
        //2.1发件人
        message.setFrom(new InternetAddress(from));
        /**
         * 第一个参数:
         *      Message.RecipientType.TO 代表收件人
         *      RecipientType.CC 抄送
         *      RecipientType.BCC 暗送
         *      关于抄送暗送
         *          A给B发送邮件, A觉得有必要让C也看看其内容,就在给B发送邮件时候将邮件内容抄送给C,
         *          这时候B知道A给C抄送过该邮件; 如果是暗送(密送),那B就不知道A也给C发送过该邮件
         * 第二个参数:
         *      也可以是数组, 群发
         */
        //2.2收件人
        message.setRecipients(Message.RecipientType.TO, new InternetAddress().parse(Joiner.on(",").skipNulls().join(toList)));
        //2.3抄送人
        if (recipientType == RecipientType.CC) {
            message.addRecipients(Message.RecipientType.CC, new InternetAddress().parse(Joiner.on(",").skipNulls().join(recipientList)));
        }else {
            message.addRecipients(Message.RecipientType.BCC, new InternetAddress().parse(Joiner.on(",").skipNulls().join(recipientList)));
        }
        //2.4主题
        message.setSubject(subject);
        //2.5内容
        Multipart multipart = new MimeMultipart();
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(content, "text/html;charset=UTF-8");
        multipart.addBodyPart(bodyPart);
        //2.6附件
        for (String attach : attachFileList) {
            BodyPart body = new MimeBodyPart();
            FileDataSource fileDataSource = new FileDataSource(attach);
            body.setDataHandler(new DataHandler(fileDataSource));
            body.setFileName(MimeUtility.encodeText(fileDataSource.getName()));
            multipart.addBodyPart(body);
        }
        message.setContent(multipart);
        //3.发送消息
        Transport.send(message);
    }

}
