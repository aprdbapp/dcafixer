package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2462 {
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
			String rand1 = scan.nextLine();
			String val2 = scan.nextLine();
			int r = sendRequest(conn, rand1 ,val2 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand1 ,String val2 ) throws SQLException {
		String sql = "SELECT EMPLOYEE_ID, END_DATE, JOB_ID FROM JOB_HISTORY WHERE END_DATE <>" + val2+" AND JOB_ID <" + rand1;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
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