package com.jverson.springboot.condition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SpringConditionalConf {
	
	@Bean
	@Conditional(WindowsCondition.class) //WindowsCondition 条件成立时创建此 Bean
	public CmdService windowsCmdService(){
		return new WindowsCmdServiceImpl();
	}
	
	@Bean
	@Conditional(LinuxCondition.class) //LinuxCondition 条件成立时创建此 Bean
	public CmdService linuxCmdService(){
		return new LinuxCmdServiceImpl();
	}
	
}
