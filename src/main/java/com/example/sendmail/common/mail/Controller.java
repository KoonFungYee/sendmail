package com.example.sendmail.common.mail;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class Controller {

    @Autowired
    private IMailService iMailService;
    
    @GetMapping("hi")
    public String name() throws Exception {
        String MAIL_SUBJECT = "Hello World";
        String content = "How are you?";
        String[] toUsers = new String[] { "kelvin2274@hotmail.com" };
        boolean isSuc = iMailService.sendSimpleMail(MAIL_SUBJECT, content, toUsers, null);
        System.out.println(isSuc);
        return "hi";
    }
}
