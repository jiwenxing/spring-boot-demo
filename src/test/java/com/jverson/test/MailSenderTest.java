package com.jverson.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jverson.springboot.HelloSpringBoot;
import com.jverson.springboot.demos.MailSenderService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class MailSenderTest {

	@Autowired 
	private MailSenderService mailSenderService;
	
	@Value("${spring.mail.username}")
    private String username;
	
	@Test
	public void send(){
		mailSenderService.sendSimpleMail("jverson@126.com", "sendSimpleMail Test", "hello this mail is from spring javaMail");
	}
	
	@Test
	public void sendTemplateMail(){
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "jverson");
		mailSenderService.sendFreeMarkerTemplateMail("jverson@126.com", "sendFreeMarkerTemplateMail Test", "welcome.ftl", model);
	}
	
	@Test
	public void sendAttachmentsMail() throws Exception {
		mailSenderService.sendAttachmentsMail("jverson@126.com", "sendAttachmentsMail Test", "有附件的邮件", "favicon.ico");
	}
	
	@Test
	public void sendInlineResourceMail(){
		String rscId = "tttt"; //contentId
	    String content="<html><body>这是有图片的邮件：<img src=\'cid:" + rscId + "\' ></body></html>";
	    String imgPath = "C:\\Users\\jiwenxing\\Pictures\\hello-world.gif"; //文件绝对路径
		mailSenderService.sendInlineResourceMail("jverson@126.com", "InlineResourceMail Test", content, imgPath, rscId);
	}
	
}
