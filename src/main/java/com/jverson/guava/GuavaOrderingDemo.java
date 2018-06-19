package com.jverson.guava;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.jverson.springboot.domain.Person;
/**
 * 自定义对象排序，这里和guava关系不大
 * @author jiwenxing
 * @date Jun 19, 2018 6:40:38 PM
 */
public class GuavaOrderingDemo {

	public static void main(String[] args) {
		System.out.println("---基本数据类型集合排序---");
		List<Integer> numbers = Lists.newArrayList();
	    numbers.add(new Integer(5));
	    numbers.add(new Integer(15));
	    numbers.add(new Integer(53));
	    numbers.add(new Integer(35));
	    numbers.add(new Integer(16));
	    
	    System.out.println("input list is " + numbers);
	    
//	    Collections.sort(numbers, Comparator.naturalOrder());
	    numbers.sort(Comparator.naturalOrder());
	    System.out.println("after sort is " + numbers);
	    
//	    Collections.sort(numbers, Comparator.reverseOrder());
	    numbers.sort(Comparator.reverseOrder());
	    System.out.println("after reverse sort is " + numbers);
	    
//	    Ordering<Integer> ordering = Ordering.natural(); 
//	    numbers.sort(ordering);
//	    System.out.println(ordering.max(numbers));
//	    numbers.sort(ordering.reverse());
//	    System.out.println("after sort is " + numbers);
	    
	    System.out.println("=====================");
	    
	    /**
	     * 自定义对象列表排序
	     */
	    List<Person> persons = Lists.newArrayList();
	    Person p1 = new Person("zhang",25,52000);
	    Person p2 = new Person("wang",35,32000);
	    Person p3 = new Person("chen",29,27000);
	    persons.add(p1);
	    persons.add(p2);
	    persons.add(p3);
	    
	    /*JDK8排序方式*/
	    System.out.println("---使用JDK8方式对自定义对象排序---");
	    System.out.println("origin list: " + persons);
	    Collections.sort(persons, Comparator.comparing(Person::getAge));
	    System.out.println("age sort: " + persons);
	    Collections.sort(persons, Comparator.comparing(Person::getSalary));
	    System.out.println("salary sort: " + persons);
	    //逆序
	    Collections.sort(persons, Comparator.comparing(Person::getSalary).reversed());
	    System.out.println("salary reversed sort: " + persons);
	    
	    /*常规方法（匿名内部类实现Comparator接口）*/
	    System.out.println("---使用常规方式对自定义对象排序---");
	    Collections.sort(persons, new Comparator<Person>() {
	    	//按照年龄升序排列
		    public int compare(Person p1, Person p2){
		    	return Integer.valueOf(p1.getAge()).compareTo(p2.getAge());
		    }
	    });
	    System.out.println("age sort: " + persons);
	    
	    /*使用静态方法引用方式*/
	    //1. 需要在Person类中定义一个静态的比较方法compareBySalaryThenAge
	    //2. 是用静态引用进行比较
	    System.out.println("---使用静态方法引用对自定义对象排序---");
	    persons.sort(Person::compareBySalaryThenAge);
	    System.out.println("static reference sort: " + persons);
	    
	    
	    Comparator<Person> comparator = (h1, h2) -> h1.getAge().compareTo(h2.getAge());
	    persons.sort(comparator);
	    persons.sort(comparator.reversed());
	    
	    /*上面可以简写为如下形式*/
	    persons.sort((Person h1, Person h2) -> h1.getAge().compareTo(h2.getAge()));
	    
	    
	    long cnt = persons.stream().filter(person -> person.getAge()>25).count();
	    System.out.println(cnt);
	    
	}
	
}
