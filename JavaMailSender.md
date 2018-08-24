
我们知道Spring提供了非常好用的JavaMailSender接口实现邮件发送。在Spring Boot中也有提供对应Starter模块为此提供了自动化配置。这里简单介绍在spring boot中使用spring提供的`JavaMailSender`发送邮件。


## 添加依赖

Spring框架通过JavaMailSender接口为发送邮件提供了一个简单的抽象，并且SpringBoot也为它提供了自动配置和一个starter模块。在pom中添加以下依赖后，Spring Boot将创建一个默认的JavaMailSender，该sender可以通过spring.mail命名空间下的配置项进一步自定义。

```xml
<!-- JavaMail -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

## 配置  

```properties
# 这里注意默认的超时时间是无穷大，避免线程被阻塞尽量手动设置超时时间
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000

# 认证
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# 账号、服务器信息
spring.mail.host=smtp.163.com # smtp服务器
spring.mail.username=jverson@163.com # 邮箱地址
spring.mail.password=******* #邮箱密码
```

注意有些邮件服务器（例如qq）邮箱密码项`spring.mail.password`需要设置为**授权码**，而非真正的邮箱密码

## 发送简单邮件

```java
@Autowired
private JavaMailSender mailSender; //spring boot auto configure

@Value("${spring.mail.username}")
private String senderEmailAddr; //发件人需要和配置文件中配的保持一致

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

@Test
public void send(){
	mailSenderService.sendSimpleMail("jverson@126.com", "sendSimpleMail Test", "hello this mail is from spring javaMail");
}
```

由于Spring Boot的starter模块提供了自动化配置，所以在引入了spring-boot-starter-mail依赖之后，会根据配置文件中的内容去创建JavaMailSender实例，因此我们可以直接在需要使用的地方直接@Autowired来引入邮件发送对象。

## 发送html邮件

```java
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
```

注意`MimeMessageHelper helper = new MimeMessageHelper(message, true);`第二个参数设为true表示邮件支持多媒体内容，例如附件、html及图片等，即whether to create a multipart message that
supports alternative texts, inline elements and attachments。

这个方法可以将写好的html静态页面以邮件正文形式发送出去，但更多的是用在模板邮件中，一般都会需要在模板中填充一些动态数据，例如下面就是使用FreeMaker创建的模板邮件的示例。

## 发送模板邮件

通常我们需要通过一个固定的模板给用户发送邮件，在Spring Boot中可以使用模板引擎来实现模板化的邮件发送。

```java
@Autowired  
private Configuration configuration; //freeMarker configuration 

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

@Test
public void sendTemplateMail(){
	Map<String, Object> model = new HashMap<String, Object>();
	model.put("name", "jverson");
	mailSenderService.sendFreeMarkerTemplateMail("jverson@126.com", "sendFreeMarkerTemplateMail Test", "welcome.ftl", model);
}
```

模板文件位于classpath的templates目录下
![](http://7xry05.com1.z0.glb.clouddn.com/201710141441_9.png

收到的邮件效果：
![](http://7xry05.com1.z0.glb.clouddn.com/201710132224_512.png)

## 发送带附件的邮件

通过MimeMessageHelper来发送一封带有附件的邮件。

```java
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


@Test
public void sendAttachmentsMail() throws Exception {
	mailSenderService.sendAttachmentsMail("jverson@126.com", "sendAttachmentsMail Test", "有附件的邮件", "favicon.ico");
}
```


注意附件是从classpath路径下加载的两个图片文件，目录如下：
![](http://7xry05.com1.z0.glb.clouddn.com/201710141331_435.png)


## 正文中嵌入图片

```java
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

@Test
public void sendInlineResourceMail(){
	String rscId = "tttt"; //contentId
    String content="<html><body>这是有图片的邮件：<img src=\'cid:" + rscId + "\' ></body></html>";
    String imgPath = "C:\\Users\\jiwenxing\\Pictures\\hello-world.gif"; //文件绝对路径
	mailSenderService.sendInlineResourceMail("jverson@126.com", "InlineResourceMail Test", content, imgPath, rscId);
}
```

收到的邮件是这样：
![](http://7xry05.com1.z0.glb.clouddn.com/201710141436_814.png)

> 需要特别注意的是公司网络环境可能会有限制导致连接不上smtp服务器从而测试失败。

## 总结

如果用到发送邮件的功能需求，则可以直接将本例中的`MailSenderService`类引入直接调用即可，其中这些方法都经过了测试，使用方法可参考单元测试。

[MailSenderService.java](https://github.com/jiwenxing/spring-boot-demo/blob/master/src/main/java/com/jverson/springboot/demos/MailSenderService.java)

[MailSenderTest.java](https://github.com/jiwenxing/spring-boot-demo/blob/master/src/test/java/com/jverson/test/MailSenderTest.java)

## References

- [spring-boot-docs-Sending email](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-email)
- [spring-docs-MailSender](https://docs.spring.io/spring/docs/4.3.3.RELEASE/spring-framework-reference/htmlsingle/#mail)
- [Spring Boot中使用JavaMailSender发送邮件](http://blog.didispace.com/springbootmailsender/)
- [springboot邮件服务](http://www.ityouknow.com/springboot/2017/05/06/springboot-mail.html)