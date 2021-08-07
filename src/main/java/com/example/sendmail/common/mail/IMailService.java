package com.example.sendmail.common.mail;

import com.example.sendmail.common.mail.bo.AttrInfo;

import java.util.List;

public interface IMailService {
	
	boolean sendSimpleMail(String subject, String text, String[] toUsers, String[] ccUsers) throws Exception;

	boolean sendComplexMail(String subject, String text, List<AttrInfo> attrs, String[] toUsers, String[] ccUsers) throws Exception;
}
