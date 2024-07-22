package simpletest2;


import java.sql.*;
import java.util.Scanner;


public class VulExample2_fixed {		
	
	public static void main(String[] args) {
		Connection conn = null;	
		String DB_URL = "jdbc:mysql://localhost:3306/smallbank?serverTimezone=UTC&useSSL=true";

		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
String username_dcafixer = System.getenv("DB_USERNAME"); // You should set an environment variable `export DB_USERNAME=your_username'
String password_dcafixer = System.getenv("DB_PASSWORD"); // You should set an environment variable `export DB_PASSWORD=your_password'
conn = DriverManager.getConnection(DB_URL, username_dcafixer, password_dcafixer);
			
			Statement stmt  = conn.createStatement();
			QExecute.stdId(stmt);
			
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		} // end try

		
	}// end main
	

}// end FirstExample