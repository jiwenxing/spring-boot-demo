package com.jverson.springboot.domain;

//@PropertySource("classpath:person.properties")
//@ConfigurationProperties(prefix = "person", ignoreUnknownFields = false)
//@Component
public class Person {

	String firstName;
	String lastName;
	Integer age;
	Double salary;
	
	public Person(String lastName, int age, double salary) {
		super();
		this.lastName = lastName;
		this.age = age;
		this.salary = salary;
	}
	
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
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public Double getSalary() {
		return salary;
	}
	public void setSalary(Double salary) {
		this.salary = salary;
	}
	@Override
	public String toString() {
		return "Person [lastName=" + lastName + ", age=" + age + ", salary=" + salary
				+ "]";
	}
	
	public static int compareBySalaryThenAge(Person h1, Person h2) {
		if (h1.getSalary().equals(h2.getSalary())) {
		    return Integer.compare(h1.getAge(), h2.getAge());
		}
		return h1.getSalary().compareTo(h2.getSalary());
	}
	
}
