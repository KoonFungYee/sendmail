package com.example.sendmail.common.mail.impl;

import com.example.sendmail.common.mail.IMailService;
import com.example.sendmail.common.mail.bo.AttrInfo;
import com.sun.mail.util.MailSSLSocketFactory;
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
        if (prop == null) {
            try {
                initProp();
            } catch (GeneralSecurityException e) {
                prop = null;
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
        return true;
    }

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

    private static class MyAuthenricator extends Authenticator {
        String u = null;
        String p = null;

        public MyAuthenricator(String u, String p) {
            this.u = u;
            this.p = p;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(u, p);
        }
    }

    private void initProp() throws GeneralSecurityException {
        if (prop != null) {
            return;
        }
        prop = new Properties();
        prop.setProperty("mail.debug", "true");
        prop.setProperty("mail.transport.protocol", protocol);
        prop.setProperty("mail.smtp.host", host);
        prop.setProperty("mail.smtp.port", port);
        prop.setProperty("mail.smtp.auth", "true");
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
        if (prop == null) {
            try {
                initProp();
            } catch (GeneralSecurityException e) {
                prop = null;
                return false;
            }
        }
        try{
            Session session = Session.getDefaultInstance(prop, new MyAuthenricator(account, pass));
            session.setDebug(false);
            Transport.send(complexEmail(session,toUsers,ccUsers,subject,text,attrs));
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public MimeMessage complexEmail(Session session, String[] toUsers, String[] ccUsers, String subject, String content, List<AttrInfo> attrs) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        if (!StringUtils.isEmpty(toUsers)) {
            for (String to:toUsers) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
        } else {
            throw new MessagingException("toUsers can not be null or empty");
        }
        mimeMessage.setSubject(subject);

        MimeMultipart mainBody = new MimeMultipart();
        MimeBodyPart mainText = new MimeBodyPart();
        mainText.setContent(content,CONTENT_TYPE);
        mainBody.addBodyPart(mainText);

        if(attrs!=null && attrs.size()>0){
            for(AttrInfo e:attrs){
                MimeBodyPart appendix = new MimeBodyPart();
                try {
                    appendix.setDataHandler(new DataHandler(new ByteArrayDataSource(e.getIs(),e.getType())));
                } catch (IOException ex) {
                    continue;
                }
                appendix.setFileName(e.getFileName());
                mainBody.addBodyPart(appendix);
            }
        }
        mainBody.setSubType("mixed");
        mimeMessage.setContent(mainBody);
        mimeMessage.saveChanges();
        return mimeMessage;
    }
}
