package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2862 {
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
			String DATE1 = scan.nextLine();
			String DATE2 = scan.nextLine();
			int r = sendRequest(conn, DATE1 ,DATE2 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String DATE1 ,String DATE2 ) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("DELETE FROM EMPLOYEES WHERE FIRST_NAME=(SELECT FIRST_NAME FROM EMPLOYEES WHERE HIRE_DATE >=TO_DATE("+DATE1+",'Month DD,YYYY') OR HIRE_DATE<=TO_DATE("+DATE2+", 1998','Month DD,YYYY'))");
		int rows = 0;
		if (rs != null) {
			while (rs.next()) {
				rows++;
			}
			rs.close();
		}
		return rows;
	}
}