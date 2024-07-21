package simpletest;


import java.sql.*;
import java.util.Scanner;
import java.sql.PreparedStatement;


public class VulExample_fixed {		
	
	
	public static void main(String[] args) {
		Connection conn = null;	
		String DB_URL = "jdbc:mysql://localhost:3306/smallbank?serverTimezone=UTC&useSSL=true";
String USER;
String PASS="";
char[] PASSXX;

		Scanner scan = new Scanner(System.in);
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");// STEP 2: Register JDBC driver
Scanner scanner = new Scanner(System.in);
USER = scanner.nextLine();
PASSXX = scanner.nextLine().toCharArray();
scanner.close();
conn = DriverManager.getConnection(DB_URL, USER, String.valueOf(PASSXX));
			
			Statement stmt  = conn.createStatement();// STEP 4: Execute a queryString custname = scan.nextLine();
			String table = scan.nextLine();//ACCOUNTS
			
ResultSet rs1 = stmt.executeQuery("Select * from " + table.replaceAll("[^a-zA-Z0-9-'_']", "") + " where custid<=10;");
			//Print results
			System.out.println("============ sql1 results ============");
			while (rs1.next()) {
				System.out.println("ID:" + rs1.getInt("custid") + ", Name: " + rs1.getString("name"));
			}
			if (rs1 != null) {
				rs1.close();
			}
			
			String sql2= "Select * from ACCOUNTS;";//sig: (q,s,-)
			ResultSet rs2 = stmt.executeQuery(sql2);
			//Print results
			System.out.println("============ sql2 results ============");
			int i=0;
			while (rs2.next() && i<10) {
				i++;
				System.out.println("ID:" + rs2.getInt("custid") + ", Name: " + rs2.getString("name"));
			}
			if (rs2 != null) {rs2.close();}
			String custname = scan.nextLine();
String sql3 = "SELECT * FROM ACCOUNTS WHERE name = ?";
			
PreparedStatement pstmt48 = stmt.getConnection().prepareStatement(sql3);
pstmt48.setObject(1, custname);;
ResultSet rs3 = pstmt48.executeQuery();
			// Print results
			System.out.println("============ sql3 results ============");
			while (rs3.next()) {
				System.out.println("ID:" + rs3.getInt("custid") + ", Name: " + rs3.getString("name"));
			}
			if (rs3 != null) {
				rs3.close();
			}
			
			scan.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		} // end try

		System.out.println("AAAAAA");
	}// end main
	

}// end FirstExample