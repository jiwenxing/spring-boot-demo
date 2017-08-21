package com.jverson.springboot.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.jverson.springboot.bean.User;

@Mapper
public interface UserMapper {

	@Select("SELECT * FROM USER WHERE name = #{name}")
	List<User> findByName(@Param("name") String name);
	
	@Insert("INSERT INTO user(name,age) VALUES(#{name}, #{age})")
	void insert(User user);
	
	
}
