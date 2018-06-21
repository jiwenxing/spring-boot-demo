package com.jverson.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;

public class EesyExcelDemo {

	
	public static void main(String[] args) throws FileNotFoundException {
		File f=new File("D:\\aa.xlsx");
	    InputStream inputStream= new FileInputStream(f);

	    ExcelLogs logs = new ExcelLogs();
	    Collection<Map> importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs , 0);

	    System.out.println(importExcel.size());
	    for(Map m : importExcel){
	    	System.out.println(new StringBuilder("('").append(m.get("伟嘉SN号")).append("', '").append(m.get("安立生坦片sn")).append("', NOW()),"));
	    }
	    
	}
	
	
}
