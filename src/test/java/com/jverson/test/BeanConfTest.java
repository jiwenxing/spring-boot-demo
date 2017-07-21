package com.jverson.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.jverson.springboot.bean.Car;

@RunWith(SpringRunner.class)
public class BeanConfTest {

	@Autowired Car car;
	
	@Test
	public void getCar(){
		assertNotNull(car);
		assertEquals("ford red", car.getColor());
		assertEquals("ford", car.getBrand());
		assertEquals((Double)30.8, car.getPrice());
	}
}
