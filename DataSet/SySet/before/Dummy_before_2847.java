package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2847 {
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
			String ID = scan.nextLine();
			int r = sendRequest(conn, ID );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String ID ) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("DELETE FROM EMPLOYEES WHERE EMPLOYEE_ID,JOB_ID=(SELECT EMPLOYEE_ID, JOB_ID FROM EMPLOYEES MINUS ( SELECT EMPLOYEE_ID, JOB_ID FROM JOB_HISTORY where JOB_ID>="+ID+"))");
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