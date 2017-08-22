package com.jverson.springboot.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

import com.jverson.springboot.bean.User;

public class UserDao {

	@Autowired
	private SqlSession sqlSession;
	
	public User selectUserById(Long id){
		return sqlSession.selectOne("selectUserById", id);
	}
	
}
