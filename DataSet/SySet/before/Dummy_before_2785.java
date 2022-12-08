package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2785 {
	static final String JDBC_DRIVER = ConnUtil.JDBC_DRIVER;
	static final String DB_URL = ConnUtil.DB_URL;
	static final String DB_UN = ConnUtil.DB_UN;
	static final String DB_PW = ConnUtil.DB_PW;
	public static void main(String[] args)
			throws IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		Connection conn = null;
		Class.forName("com.mysql.cj.jdbc.Driver");
		conn = DriverManager.getConnection(DB_URL, DB_UN, DB_PW);
		try (Scanner scan = new Scanner(System.in)) {
			String id1 = scan.nextLine();
			String id2 = scan.nextLine();
			int r = sendRequest(conn, id1 ,id2 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String id1 ,String id2 ) throws SQLException {
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate("UPDATE EMPLOYEES SET DEPARTMENT_ID ="+ id1+" WHERE EMPLOYEE_ID >" +id2);
		return rs;
	}
}