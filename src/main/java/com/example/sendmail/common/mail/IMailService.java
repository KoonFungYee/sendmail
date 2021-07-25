package com.example.sendmail.common.mail;

import com.example.sendmail.common.mail.bo.AttrInfo;

import java.util.List;

/**
 * Send email service
 *
 * @author sh00975
 */
public interface IMailService {
	
	/**
	 * @param subject 主题
	 * @param text  邮件正文
	 * @param toUsers 收件人列表
	 * @param ccUsers 抄送人列表
	 * @return 是否发送成功
	 * @throws Exception
	 */
	boolean sendSimpleMail(String subject, String text, String[] toUsers, String[] ccUsers) throws Exception;

	/**
	 * 发送带附件的邮件
	 * @param subject 邮件标题
	 * @param text  邮件正文
	 * @param attrs 附件列表
	 * @param toUsers 收件人列表
	 * @param ccUsers 抄送人列表
	 * @return 是否发送成功
	 * @throws Exception
	 */
	boolean sendComplexMail(String subject, String text, List<AttrInfo> attrs, String[] toUsers, String[] ccUsers) throws Exception;
}
