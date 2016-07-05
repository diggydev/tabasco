package com.github.diggydev.tabasco.ws;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.github.diggydev.tabasco.configuration.TabascoConfiguration;

@Controller
@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan(basePackages="com.github.diggydev.tabasco")
public class TabascoEndpoint extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private TabascoConfiguration configuration;
	@Autowired
	private AmazonDynamoDBClient client;
	private InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication;
	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(TabascoEndpoint.class, args);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .authorizeRequests()
        	.antMatchers("/register").permitAll()
            .anyRequest().authenticated()
            .and()
        .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/home")
            .permitAll()
            .and()
        .logout()                                    
            .permitAll();
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	List<String> attributesToGet = Arrays.asList("name", "password");
        inMemoryAuthentication = auth.inMemoryAuthentication();
        ScanResult result = client.scan(configuration.getUserTableName(), attributesToGet);
		for(Map<String, AttributeValue> item : result.getItems()) {
			inMemoryAuthentication.withUser(item.get("name").getS()).password(item.get("password").getS())
			.roles("USER");
		}
    }
    
    @RequestMapping(path={"/home","/"})
    public String home() {
        return "home";
    }
    
    @RequestMapping(path="/register", method=RequestMethod.GET)
    public String registrationForm() {
        return "register";
    }
    
    @RequestMapping(path="/register", method=RequestMethod.POST)
    public ModelAndView processRegistration(
    		@RequestParam("username") String username,
    		@RequestParam("password") String password) {
    	ModelAndView modelAndView;
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
        	modelAndView = new ModelAndView("register");
        	modelAndView.addObject("error", "Invalid username or password. Try again.");
        } else {
        	HashMap<String, AttributeValue> key = new HashMap<>();
        	key.put("name", new AttributeValue(username));
			GetItemResult existingItem = client.getItem(configuration.getUserTableName(), key);
			if(existingItem.getItem()==null) {
				key.put("password", new AttributeValue(password));
				client.putItem(configuration.getUserTableName(), key);
				Collection<? extends GrantedAuthority> authorities = Collections.singleton(()->"ROLES");
				inMemoryAuthentication.getUserDetailsService().createUser(
						new User(username, password,
								true, true, true, true, authorities ));
	        	modelAndView = new ModelAndView("login");
	        	modelAndView.addObject("source", "registration");
			} else {
				modelAndView = new ModelAndView("register");
	        	modelAndView.addObject("error", "Username already exists. Try again.");
			}
        }
        return modelAndView;
    }

    @RequestMapping(path="/login")
    public String login() {
        return "login";
    }
    
}
