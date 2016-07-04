package com.github.diggydev.tabasco;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

@Controller
@EnableAutoConfiguration
@EnableWebSecurity
public class TabascoEndpoint {
	
	@Value("#{systemProperties.getProperty('tabasco.users.table', 'tabasco-users')}")
	private String userTableName;
	
	private AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
	private AmazonDynamoDBClient client = new AmazonDynamoDBClient(awsCredentialsProvider).withRegion(Regions.EU_WEST_1);
	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(TabascoEndpoint.class, args);
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	List<String> attributesToGet = Arrays.asList("name", "password");
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication = auth
            .inMemoryAuthentication();
        ScanResult result = client.scan(userTableName, attributesToGet);
		for(Map<String, AttributeValue> item : result.getItems()) {
			inMemoryAuthentication.withUser(item.get("name").getS()).password(item.get("password").getS()).roles("USER");
		}
    }
	
	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Yo";
	}

}
