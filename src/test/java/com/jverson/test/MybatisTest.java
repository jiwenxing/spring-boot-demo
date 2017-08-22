package com.jverson.test;

import static org.junit.Assert.assertTrue;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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

	//MyBatis-Spring-Boot-Starter已经将所有mapper注册到spring容器中，因此可以直接注入
	@Autowired UserMapper userMapper;
	
	//也可通sqlSessionFactory来获取mapper的实例
	@Autowired SqlSessionFactory sqlSessionFactory;
	
	@Test
	public void testInsert(){
		SqlSession session = sqlSessionFactory.openSession();
		UserMapper userMapper1 = session.getMapper(UserMapper.class);
		User user = new User();
		user.setName("kobe");
		user.setAge(41);
		try {
			userMapper1.insert(user);
		} finally {
			session.close();
		}
	}
	
//	@Test
	public void testQuery(){
		assertTrue(userMapper.findByName("kobe").size()==1);
	}
	
//	@Test
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
