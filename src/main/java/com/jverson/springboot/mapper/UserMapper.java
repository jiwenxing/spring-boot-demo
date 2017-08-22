package com.jverson.springboot.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.jverson.springboot.bean.User;

@Mapper
public interface UserMapper {

	//通过注解实现sql与方法的映射
	@Select("SELECT * FROM USER WHERE name = #{name}")
	List<User> findByName(@Param("name") String name);
	
//	@Insert("INSERT INTO user(name,age) VALUES(#{name}, #{age})")
	void insert(User user);
	
//	@Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
	void update(User user);
	
	//通过xml配置实现映射（传统方式）
	List<User> getAll();
	
}
