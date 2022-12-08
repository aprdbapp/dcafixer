package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_1444 {
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
			String val1 = scan.nextLine();
			int r = sendRequest(conn, rand1 ,val1 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand1 ,String val1 ) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT DEPARTMENT_ID, MANAGER_ID, LOCATION_ID FROM DEPARTMENTS WHERE MANAGER_ID >=" + val1+" AND LOCATION_ID >" + rand1);
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