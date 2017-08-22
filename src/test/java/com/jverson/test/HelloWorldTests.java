package com.jverson.test;


import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.jverson.springboot.HelloSpringBoot;

/*
@RunWith(SpringJUnit4ClassRunner.class)
SpringRunner is just an alias for the SpringJUnit4ClassRunner. Both are OK.
*/
@RunWith(SpringRunner.class)
public class HelloWorldTests {

	private MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(new HelloSpringBoot()).build();
	}

	@Test
	public void getHello() throws Exception {
		MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Hello World!"))
				.andDo(MockMvcResultHandlers.print())
				.andReturn();
		assertFalse(result.getResponse().containsHeader("sid"));
	}
	
}
