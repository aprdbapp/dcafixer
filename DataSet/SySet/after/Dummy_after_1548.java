package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_1548 {
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
String sql = "SELECT LAST_NAME, JOB_ID, SALARY FROM EMPLOYEES WHERE JOB_ID < ? AND SALARY < ANY (SELECT SALARY FROM EMPLOYEES WHERE JOB_ID = ?)";
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