package com.jverson.springboot.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class WindowsCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		System.out.println("os.name:" + context.getEnvironment().getProperty("os.name"));
		return context.getEnvironment().getProperty("os.name").contains("Windows");
	}
	
}
