package simpletest2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.PreparedStatement;

public class QExecute_fixed {
	public static void stdId (Statement stmt) throws SQLException {
		Scanner scan = new Scanner(System.in);
		String table = scan.nextLine();
		
ResultSet rs1 = stmt.executeQuery("Select * from " + table.replaceAll("[^a-zA-Z0-9-'_']", "") + " where stdid<=50;");
		//Print results
		System.out.println("============ sql1 results ============");
		while (rs1.next()) {
			System.out.println("ID:" + rs1.getInt("custid") + ", Name: " + rs1.getString("name"));
		}
		if (rs1 != null) {
			rs1.close();
		}
		
		
		String stdname = scan.nextLine();
String sql2 = "SELECT * FROM STUDENTS WHERE name = ?";
		
PreparedStatement pstmt27 = stmt.getConnection().prepareStatement(sql2);
pstmt27.setObject(1, stdname);;
ResultSet rs2 = pstmt27.executeQuery();
		// Print results
		System.out.println("============ sql2 results ============");
		while (rs2.next()) {
			System.out.println("ID:" + rs2.getInt("custid") + ", Name: " + rs2.getString("name"));
		}
		if (rs2 != null) {
			rs2.close();
		}
		if (scan != null)
			scan.close();
	}
	
}