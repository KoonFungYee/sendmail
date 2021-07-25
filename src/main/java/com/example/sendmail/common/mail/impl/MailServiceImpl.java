package com.example.sendmail.common.mail.impl;

import com.example.sendmail.common.mail.IMailService;
import com.example.sendmail.common.mail.bo.AttrInfo;
import com.sun.mail.util.MailSSLSocketFactory;
// import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

// @Slf4j
@Service
public class MailServiceImpl implements IMailService {
    @Value("${email.account}")
    private String account;
    @Value("${email.pass}")
    private String pass;
    @Value("${email.from}")
    private String from;
    @Value("${email.host}")
    private String host;
    @Value("${email.port}")
    private String port;
    @Value("${email.protocol}")
    private String protocol;

    private static Properties prop;
    private static final String UTF8 = "UTF-8";
    private static final Charset CHARSET_UTF8 = Charset.forName(UTF8);
    private static final String CONTENT_TYPE="text/html;charset=utf-8";

    @Override
    public boolean sendSimpleMail(String subject, String text, String[] toUsers, String[] ccUsers) throws Exception {
        // log.info("Ready to send email from system.");
        if (prop == null) {
            try {
                initProp();
            } catch (GeneralSecurityException e) {
                prop = null;
                // log.error("init properties of sending emall failed:",e);
                return false;
            }
        }

        Session session = Session.getDefaultInstance(prop, new MyAuthenricator(account, pass));
        session.setDebug(false);
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        this.addRecipient(mimeMessage,toUsers,ccUsers);
        mimeMessage.setSubject(subject);
        mimeMessage.setSentDate(new Date());
        mimeMessage.setText(text);
        mimeMessage.saveChanges();
        Transport.send(mimeMessage);
        // log.info("Sending email from system complete.");
        return true;
    }

    /**
     * 添加收件地址。
     * 
     * @param mimeMessage
     * @throws AddressException
     * @throws MessagingException
     */
    private void addRecipient(MimeMessage mimeMessage,String[] toUsers,String[] ccUsers) throws AddressException, MessagingException {

        if (toUsers!=null && toUsers.length>0) {
            for (int i = 0; i < toUsers.length; i++) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toUsers[i]));
            }
        } else {
            throw new AddressException("toUsers can not be null or empty");
        }
        if (ccUsers!=null && ccUsers.length>0) {
            for (int i = 0; i < ccUsers.length; i++) {
                mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(ccUsers[i]));
            }
        }
    }

    /**用户名密码验证，需要实现抽象类Authenticator的抽象方法PasswordAuthentication*/
    private static class MyAuthenricator extends Authenticator {
        String u = null;
        String p = null;

        public MyAuthenricator(String u, String p) {
            this.u = u;
            this.p = p;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            // log.info(String.format("u:%s,p:%s", u, p));
            return new PasswordAuthentication(u, p);
        }
    }

    private void initProp() throws GeneralSecurityException {
        if (prop != null) {
            return;
        }
        prop = new Properties();
        prop.setProperty("mail.debug", "true");
        // 协议
        prop.setProperty("mail.transport.protocol", protocol);
        // 服务器
        prop.setProperty("mail.smtp.host", host);
        // 端口
        prop.setProperty("mail.smtp.port", port);
        // 使用smtp身份验证
        prop.setProperty("mail.smtp.auth", "true");
        // 使用SSL，qq邮箱必需, 开启安全协议
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        prop.put("mail.smtp.connectiontimeout", "10000");
        prop.put("mail.smtp.timeout", "10000");
    }

    private static String base64ToString(String base64) {
        return new String(Base64.decodeBase64(base64.getBytes(CHARSET_UTF8)), CHARSET_UTF8);
    }

    @Override
    public boolean sendComplexMail(String subject, String text,List<AttrInfo> attrs, String[] toUsers, String[] ccUsers) throws Exception {
        // log.info("Ready to send complex email");

        if (prop == null) {
            try {
                initProp();
            } catch (GeneralSecurityException e) {
                prop = null;
                // log.error("Send complex email fail when init,", e);
                return false;
            }
        }
        try{
            Session session = Session.getDefaultInstance(prop, new MyAuthenricator(account, pass));
            session.setDebug(false);
            Transport.send(complexEmail(session,toUsers,ccUsers,subject,text,attrs));
        }catch (Exception e){
            // log.error("Send complex email fail,", e);
            return false;
        }
        // log.info("Sending complex email completed.");
        return true;
    }

    /**带附件和正文的邮件*/
    public MimeMessage complexEmail(Session session, String[] toUsers, String[] ccUsers, String subject, String content, List<AttrInfo> attrs) throws MessagingException, UnsupportedEncodingException {
        //消息的固定信息
        MimeMessage mimeMessage = new MimeMessage(session);
        //发件人
        mimeMessage.setFrom(new InternetAddress(from));
        //收件人
        if (!StringUtils.isEmpty(toUsers)) {
            for (String to:toUsers) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
        } else {
            // log.error("toUsers can not be null or empty");
            throw new MessagingException("toUsers can not be null or empty");
        }
        //邮件标题
        mimeMessage.setSubject(subject);

        //邮件主体内容
        MimeMultipart mainBody = new MimeMultipart();
        //准备文本
        MimeBodyPart mainText = new MimeBodyPart();
        mainText.setContent(content,CONTENT_TYPE);
        mainBody.addBodyPart(mainText);

        //附件
        if(attrs!=null && attrs.size()>0){
            for(AttrInfo e:attrs){
                MimeBodyPart appendix = new MimeBodyPart();
                try {
                    appendix.setDataHandler(new DataHandler(new ByteArrayDataSource(e.getIs(),e.getType())));
                } catch (IOException ex) {
                    // log.error("Attachment error,file name is:"+e.getFileName(), ex);
                    continue;
                }
                appendix.setFileName(e.getFileName());
                mainBody.addBodyPart(appendix);
            }
        }
        //正文和附件都存在邮件中，所有类型设置为mixed
        mainBody.setSubType("mixed");

        //放到Message消息中
        mimeMessage.setContent(mainBody);
        mimeMessage.saveChanges();
        return mimeMessage;
    }
}
