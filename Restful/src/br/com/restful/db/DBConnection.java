package br.com.restful.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/*
 * 获取数据库连接
 * 当需要一个Connection做参数的时候使用
 */

public class DBConnection 
{
	// 获取oracle 106 连接
	public static Connection getBIOracle106Conn() throws SQLException
	{
		String db_106_conn_str = "jdbc:oracle:thin:@10.0.22.106:1522:kaddw02";
		// String dbName = "kaddw02";
		
		String db_106_user_name = "bidev";
		String db_106_password = "bi2015dev";
		
		Connection connection = null;
		try
		{
			  if(connection == null || connection.isClosed())
			  {
				   // Class.forName("oracle.jdbc.driver.OracleDriver");
				   connection= DriverManager.getConnection(db_106_conn_str, db_106_user_name, db_106_password); 
			  }
		}
		catch (SQLException e) 
		{
		   e.printStackTrace();
		}
		  
		return connection;
	}
}
