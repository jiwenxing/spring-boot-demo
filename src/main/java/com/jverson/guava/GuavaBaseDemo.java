package com.jverson.guava;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.io.Files;
/**
 * 二维映射 table & 双向映射 BiMap & 文件操作 & Preconditions
 * @author jverson
 * @date Jun 19, 2018 3:06:44 PM
 */
public class GuavaBaseDemo {

	public static void main(String[] args) {
		/**
		 * 二维映射 table
		 */
        //Map<String, Map<String, String>> map = Maps.newHashMap();
		Table<String, String, String> table = HashBasedTable.create();
		table.put("1", "1", "1");
		table.put("1", "2", "2");
		table.put("2", "1", "2");
		table.put("2", "2", "4");
		System.out.println("size is " + table.size());
		System.out.println("cellSet is " + table.cellSet()); //二维映射键值对，[(1,1)=1, (1,2)=2, (2,1)=2, (2,2)=4]
		System.out.println("column 1 is " + table.column("1")); //返回的是所有包含该列的rowKey与value，{1=1, 2=2}
		System.out.println("row 1 is " + table.row("1")); //返回的是所有包含该行的columnKey与value，{1=1, 2=2}
		System.out.println("columnMap is" + table.columnMap());//返回等效的Map<String, Map<String, String>>形式 {1={1=1, 2=2}, 2={1=2, 2=4}}
		
		/**
		 * 双向映射 BiMap
		 * 1. 使用BiMap时，要求Value的唯一性
		 * 2. 反转后的map的所有操作都会影响原始的map对象
		 */
		//创建方式1
		BiMap<String, String> biMap = HashBiMap.create();
		biMap.put("a", "65");
		biMap.put("b", "66");
		//创建方式2
		Map<String, String> map = Maps.newHashMap();
		map.put("c", "67");
		map.put("d", "68");
		BiMap<String, String> biMap2 = HashBiMap.create(map);
		
		System.out.println(biMap.get("a"));
		System.out.println(biMap.inverse().get("66"));
		
		System.out.println(biMap2.get("c"));
		System.out.println(biMap2.inverse().get("68"));
		
		/**
		 * 文件操作
		 * 代码很简洁
		 */
		//方式1：使用guava
		List<String> lines = null;
		try {
			File file = new ClassPathResource("banner.txt").getFile();
			lines = Files.readLines(file, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("------方式1-----");
		if (!CollectionUtils.isEmpty(lines)) {
			for (String string : lines) {
				System.out.println(string);
			}
		}
		//方式2：不使用guava
		List<String> lines2 = null;
		try {
			Resource fileResource = new ClassPathResource("banner.txt");
			lines2 = java.nio.file.Files.readAllLines(Paths.get(fileResource.getURI()),  
					Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("------方式2-----");
		if (!CollectionUtils.isEmpty(lines2)) {
			for (String string : lines2) {
				System.out.println(string);
			}
		}
	}
	
	/**
	 * Preconditions 示例
	 */
	public double sqrt(double input) throws IllegalArgumentException {
	      Preconditions.checkArgument(input > 0.0,
	         "Illegal Argument passed: Negative value %s.", input);
	      return Math.sqrt(input);
	}	

	public int sum(Integer a, Integer b){
	      a = Preconditions.checkNotNull(a,
	         "Illegal Argument passed: First parameter is Null.");
	      b = Preconditions.checkNotNull(b,
	         "Illegal Argument passed: Second parameter is Null.");
	      return a+b;
	}
	
}
