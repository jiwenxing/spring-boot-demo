package com.jverson.springboot.condition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConditionalConf {
	@Bean
	@Conditional(WindowsCondition.class)
	public CmdService windowsCmdService(){
		return new WindowsCmdServiceImpl();
	}
	
	@Bean
	@Conditional(LinuxCondition.class)
	public CmdService linuxCmdService(){
		return new LinuxCmdServiceImpl();
	}
}
