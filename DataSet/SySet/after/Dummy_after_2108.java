package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_2108 {
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
			String rand6 = scan.nextLine();
			String val2 = scan.nextLine();
			int r = sendRequest(conn, rand6 ,val2 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand6 ,String val2 ) throws SQLException {
java.lang.String  sql = "SELECT EMPLOYEE_ID, SALARY, COMMISSION_PCT FROM EMPLOYEES WHERE SALARY <> ? OR COMMISSION_PCT < ?";
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer26 = conn.prepareStatement(sql);
pstmt_dcafixer26.setObject(1, val2);
pstmt_dcafixer26.setObject(2, rand6);
ResultSet rs = pstmt_dcafixer26.executeQuery();
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