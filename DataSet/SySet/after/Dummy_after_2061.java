package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_2061 {
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
			String var1 = scan.nextLine();
			int r = sendRequest(conn, rand6 ,var1 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand6 ,String var1 ) throws SQLException {
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer25 = conn.prepareStatement("SELECT EMPLOYEE_ID, SALARY, COMMISSION_PCT FROM EMPLOYEES WHERE SALARY > ? OR COMMISSION_PCT > ?");
pstmt_dcafixer25.setObject(1, var1);
pstmt_dcafixer25.setObject(2, rand6);
ResultSet rs = pstmt_dcafixer25.executeQuery();
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