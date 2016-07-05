package com.github.diggydev.tabasco.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TabascoConfiguration.class, loader=AnnotationConfigContextLoader.class)
public class TabascoConfigurationTest {
	
	@Autowired
	private TabascoConfiguration config;
	
	@Test
	public void testDefaults() {
		assertEquals("tabasco-users", config.getUserTableName());
	}

}
