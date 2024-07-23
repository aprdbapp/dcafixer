package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_1837 {
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
			String rand3 = scan.nextLine();
			String val1 = scan.nextLine();
			int r = sendRequest(conn, rand3 ,val1 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand3 ,String val1 ) throws SQLException {
java.lang.String  sql = "SELECT EMPLOYEE_ID, PHONE_NUMBER, HIRE_DATE FROM EMPLOYEES WHERE PHONE_NUMBER <> ? OR HIRE_DATE < ?";
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer26 = conn.prepareStatement(sql);
pstmt_dcafixer26.setObject(1, val1);
pstmt_dcafixer26.setObject(2, rand3);
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