package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_2671 {
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
			String sal = scan.nextLine();
			int r = sendRequest(conn, sal );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String sal ) throws SQLException {
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer24 = conn.prepareStatement("SELECT max(salary) Max_salary FROM employees GROUP BY department_id HAVING min(salary) > ? ORDER BY Max_salary ASC");
pstmt_dcafixer24.setObject(1, sal);
ResultSet rs = pstmt_dcafixer24.executeQuery();
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