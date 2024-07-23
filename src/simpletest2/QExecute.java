package simpletest2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class QExecute {
	public static void stdId (Statement stmt) throws SQLException {
		Scanner scan = new Scanner(System.in);
		String table = scan.nextLine();
		
		ResultSet rs1 = stmt.executeQuery("Select * from " + table + " where stdid<=50;");//sig: (q,s,t)
		//Print results
		System.out.println("============ sql1 results ============");
		while (rs1.next()) {
			System.out.println("ID:" + rs1.getInt("custid") + ", Name: " + rs1.getString("name"));
		}
		if (rs1 != null) {
			rs1.close();
		}
		
		
		String stdname = scan.nextLine();
		String sql2= "Select * from STUDENTS where name = '" + stdname + "';";//sig: q,s,v
		
		ResultSet rs2 = stmt.executeQuery(sql2);
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
