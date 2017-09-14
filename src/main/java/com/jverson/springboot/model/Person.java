package com.jverson.springboot.model;

//@PropertySource("classpath:person.properties")
//@ConfigurationProperties(prefix = "person", ignoreUnknownFields = false)
//@Component
public class Person {

	String firstName;
	String lastName;
	int age;
	double salary;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public double getSalary() {
		return salary;
	}
	public void setSalary(double salary) {
		this.salary = salary;
	}
	@Override
	public String toString() {
		return "Person [firstName=" + firstName + ", lastName=" + lastName + ", age=" + age + ", salary=" + salary
				+ "]";
	}
	
}
