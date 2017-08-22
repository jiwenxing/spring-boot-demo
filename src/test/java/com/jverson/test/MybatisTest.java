package com.jverson.test;

import static org.junit.Assert.assertTrue;

import org.apache.ibatis.type.JdbcType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jverson.springboot.HelloSpringBoot;
import com.jverson.springboot.bean.User;
import com.jverson.springboot.mapper.UserMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class MybatisTest {

	@Autowired UserMapper userMapper;
	JdbcType jdbcType;
	
//	@Test
	public void testInsert(){
		User user = new User();
		user.setName("kobe");
		user.setAge(41);
		userMapper.insert(user);
	}
	
//	@Test
	public void testQuery(){
		assertTrue(userMapper.findByName("kobe").size()==1);
	}
	
	@Test
	public void testQueryAll(){
		assertTrue(userMapper.getAll().size()>0);
	}
	
//	@Test
	public void testUpdate(){
		User user = new User();
		user.setId(1L);
		user.setName("james");
		userMapper.update(user);
		assertTrue(userMapper.findByName("james").size()==1);
	}
	
}
