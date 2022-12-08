package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_1407 {
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
			String rand0 = scan.nextLine();
			String var2 = scan.nextLine();
			int r = sendRequest(conn, rand0 ,var2 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand0 ,String var2 ) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT DEPARTMENT_ID, DEPARTMENT_NAME, MANAGER_ID FROM DEPARTMENTS WHERE DEPARTMENT_NAME <>" + var2+" OR MANAGER_ID <" + rand0);
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