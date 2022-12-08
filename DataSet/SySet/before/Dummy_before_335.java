package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_335 {
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
			String DEPT_ID = scan.nextLine();
			String DEPT_NAME = scan.nextLine();
			String LOCATION_ID = scan.nextLine();
			String MANAGER_ID = scan.nextLine();
			int r = sendRequest(conn, DEPT_ID ,DEPT_NAME ,LOCATION_ID ,MANAGER_ID );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String DEPT_ID ,String DEPT_NAME ,String LOCATION_ID ,String MANAGER_ID ) throws SQLException {
		String sql = "INSERT INTO DEPARTMENTS VALUES ("+DEPT_ID+","+DEPT_NAME+", "+MANAGER_ID+", "+LOCATION_ID+")";
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate(sql);
		return rs;
	}
}