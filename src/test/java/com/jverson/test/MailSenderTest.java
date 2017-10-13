package com.jverson.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.jverson.springboot.HelloSpringBoot;
import com.jverson.springboot.demos.MailSenderService;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class MailSenderTest {

	@Autowired 
	private MailSenderService mailSenderService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${spring.mail.username}")
    private String username;
	
//	@Test
	public void send(){
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo("jverson@126.com");//收件人邮箱地址
		mail.setFrom(username);//收件人
		mail.setSubject("spring自带javamail发送的邮件");//主题
        mail.setText("hello this mail is from spring javaMail");//正文
        mailSenderService.send(mail);
	}
	
	@Test
	public void sendTemplateMail() throws MessagingException, IOException, TemplateException{
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); //第二个参数含义：whether to create a multipart message that supports alternative texts, inline elements and attachments
		helper.setFrom(username);
		helper.setTo("jverson@126.com");
		helper.setSubject("主题：模板邮件");
		
		Map<String, Object> model = new HashMap();
		model.put("name", "jverson");
		
		//使用FreeMaker模板来渲染邮件内容
		Configuration cfg = new Configuration();
		StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
		cfg.setTemplateLoader(stringTemplateLoader);
		String sbTemp = "<html><body><h1>亲爱的${name}，你好！您接收到一份使用Freemaker模板，并使用JavaMail发送的邮件。</h1></body></html>";
		Template temp = new Template("test",new StringReader(sbTemp.toString()), cfg, "GBK");
		String text = FreeMarkerTemplateUtils.processTemplateIntoString(temp, model);
		
		helper.setText(text, true);
		mailSender.send(mimeMessage);
	}
	
	/*@Test
	public void sendAttachmentsMail() throws Exception {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setFrom(username);
		helper.setTo("jverson@126.com");
		helper.setSubject("主题：有附件");
		helper.setText("有附件的邮件");
		
		InputStream input = this.getClass().getResourceAsStream("resources/static/");
		
		FileSystemResource file = new FileSystemResource(new File("weixin.jpg"));
		helper.addAttachment("附件-1.jpg", file);
		helper.addAttachment("附件-2.jpg", file);
		mailSender.send(mimeMessage);
	}*/
	
}
