package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_225 {
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
			String num = scan.nextLine();
			int r = sendRequest(conn, num );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String num ) throws SQLException {
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer24 = conn.prepareStatement("SELECT JOB_ID, SALARY FROM EMPLOYEES WHERE SALARY < ? GROUP BY JOB_ID ORDER BY JOB_ID DESC");
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