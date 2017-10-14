package com.jverson.springboot.demos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;



@Component
public class MailSenderService {

	private final Logger logger =  LoggerFactory.getLogger(getClass());
	
	@Autowired
	private JavaMailSender mailSender; //spring boot auto configure
	
	@Autowired  
	private Configuration configuration; //freeMarker configuration 
	
	@Value("${spring.mail.username}")
    private String senderEmailAddr; //发件人需要和配置文件中配的保持一致
	
	/**
	 * 发送简单邮件
	 * @param to
	 * @param subject
	 * @param content
	 */
	public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmailAddr);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
            logger.info("sendSimpleMail done!");
        } catch (Exception e) {
            logger.error("sendSimpleMail exception！", e);
        }
    }
	
	/**
	 * 发送html格式邮件，邮件正文会以html格式呈现
	 * 适合发送静态html页的邮件
	 * @param to
	 * @param subject
	 * @param content
	 */
	public void sendHtmlMail(String to, String subject, String content) {
	    MimeMessage message = mailSender.createMimeMessage();
	    try {
	        //true表示需要创建一个multipart message
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	        helper.setFrom(senderEmailAddr);
	        helper.setTo(to);
	        helper.setSubject(subject);
	        helper.setText(content, true);

	        mailSender.send(message);
	        logger.info("sendHtmlMail done!");
	    } catch (MessagingException e) {
	    	logger.error("sendHtmlMail exception！", e);
	    }
	}
	
	/**
	 * 发送带附件的邮件
	 * @param to
	 * @param subject
	 * @param content
	 * @param filename
	 */
	public void sendAttachmentsMail(String to, String subject, String content, String filename){
	    MimeMessage message = mailSender.createMimeMessage();
	    try {
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	        helper.setFrom(senderEmailAddr);
	        helper.setTo(to);
	        helper.setSubject(subject);
	        helper.setText(content, true);

	        //从classpath下加载附件文件
			ClassLoader classLoader = getClass().getClassLoader();
			URL url = classLoader.getResource(filename);
			FileSystemResource file = new FileSystemResource(new File(url.getFile()));
			helper.addAttachment(filename, file);

	        mailSender.send(message);
	        logger.info("sendAttachmentsMail done!");
	    } catch (MessagingException e) {
	    	logger.error("sendAttachmentsMail exception！", e);
	    }
	}
	
	/**
	 * 发送带静态资源的邮件,即邮件正文嵌入图片
	 * @param to
	 * @param subject
	 * @param content
	 * @param rscPath
	 * @param rscId
	 */
	public void sendInlineResourceMail(String to, String subject, String content, String rscPath, String rscId){
	    MimeMessage message = mailSender.createMimeMessage();
	    try {
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	        helper.setFrom(senderEmailAddr);
	        helper.setTo(to);
	        helper.setSubject(subject);
	        helper.setText(content, true);

	        FileSystemResource res = new FileSystemResource(new File(rscPath));
	        helper.addInline(rscId, res);

	        mailSender.send(message);
	        logger.info("sendInlineResourceMail done!");
	    } catch (MessagingException e) {
	    	logger.error("sendInlineResourceMail exception！", e);
	    }
	}
	
	/**
	 * 发送FreeMaker模板邮件
	 * @param to
	 * @param subject
	 * @param templateName
	 * @param model
	 */
	public void sendFreeMarkerTemplateMail(String to, String subject, String templateName, @SuppressWarnings("rawtypes") Map model){
	    Template t;
		try {
			t = configuration.getTemplate(templateName); // freeMarker template  
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model); //渲染模板
			this.sendHtmlMail(to, subject, content);
			logger.info("sendFreeMarkerTemplateMail done!");
		} catch (IOException | TemplateException e) {
			logger.info("sendFreeMarkerTemplateMail exception!");
			e.printStackTrace();
		} 
	}
	
	/**
	 * 发送thymeleaf模板邮件
	 * 注意需要添加pom依赖
	 * @param to
	 * @param subject
	 * @param templateName
	 * @param model
	 */
	public void sendThymeleafTemplateMail(String to, String subject, String templateName, @SuppressWarnings("rawtypes") Map model){
		try {
//			Context context = new Context();
//			context.setVariable("id", "006");
//			String emailContent = templateEngine.process("emailTemplate", context);
//			this.sendHtmlMail(to, subject, emailContent);
			logger.info("sendThymeleafTemplateMail done!");
		} catch (Exception e) {
			logger.info("sendThymeleafTemplateMail exception!");
			e.printStackTrace();
		} 
	}
	
	
}
