package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_1004 {
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
			String DEPTID = scan.nextLine();
			String DEPTNAME = scan.nextLine();
			String LOCATIONID = scan.nextLine();
			String MANAGERID = scan.nextLine();
			int r = sendRequest(conn, DEPTID ,DEPTNAME ,LOCATIONID ,MANAGERID );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String DEPTID ,String DEPTNAME ,String LOCATIONID ,String MANAGERID ) throws SQLException {
		String sql = "INSERT INTO DEPARTMENTS VALUES ("+DEPTID+","+DEPTNAME+", "+MANAGERID+", "+LOCATIONID+")";
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate(sql);
		return rs;
	}
}