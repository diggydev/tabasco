package com.github.diggydev.tabasco.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:aws-context.xml")
public class TabascoConfiguration {
	
	@Value("#{systemProperties.getProperty('tabasco.users.table', 'tabasco-users')}")
	private String userTableName;
	
	public String getUserTableName() {
		return userTableName;
	}

}
