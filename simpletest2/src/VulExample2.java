package simpletest2;


import java.sql.*;


public class VulExample2 {		
	
	public static void main(String[] args) {
		Connection conn = null;	
		String DB_URL = "jdbc:mysql://localhost:3306/smallbank?serverTimezone=UTC&useSSL=true";

		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, "root", "purdue");
			
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
