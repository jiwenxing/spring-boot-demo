package com.jverson.test.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionTest {

	public static void main(String[] args) {
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver"); //加载JDBC驱动，成功后会自动将Driver类的实例注册到DriverManager中
			conn = DriverManager.getConnection( 
				      "jdbc:MySql://192.168.192.125:3358/spring_boot_test?useUnicode=true", // URL
				      "root", // 用户名
				      "123456" ); // 密码
			conn.setAutoCommit(false); //關閉自動提交，默認開啟
			
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE user SET age = 222 WHERE Name = 'james'");
			stmt.executeUpdate("UPDATE user1 SET age = 2 WHERE Name = 'kobe'"); //更新一個不存在的表
			stmt.executeUpdate("UPDATE user SET age = 123 WHERE Name = 'iverson'");
			conn.commit();
			rs = stmt.executeQuery( "SELECT * FROM user" );
			while ( rs.next() ) {
			     int numColumns = rs.getMetaData().getColumnCount();
			     for ( int i = 1 ; i <= numColumns ; i++ ) {
			        // 与大部分Java API中下标的使用方法不同，字段的下标从1开始
			        System.out.println( "COLUMN " + i + " = " + rs.getObject(i) ); // 也可以使用ResultSet.getXXX()
			     }
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {  
                    stmt.close();  
                } catch (SQLException e) {  
                    e.printStackTrace();  
                    throw new RuntimeException(e);  
                }  
			}
			if(conn!=null) {
				try {  
                    conn.close();  
                } catch (SQLException e) {  
                    e.printStackTrace();  
                    throw new RuntimeException(e);  
                } 
			}
		}
	}
	
	
}
