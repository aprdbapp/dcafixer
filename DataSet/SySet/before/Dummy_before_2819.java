package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2819 {
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
			String commission = scan.nextLine();
			String id = scan.nextLine();
			String jobtitle = scan.nextLine();
			int r = sendRequest(conn, commission ,id ,jobtitle );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String commission ,String id ,String jobtitle ) throws SQLException {
		String sql = "UPDATE EMPLOYEES SET JOB_ID ="+jobtitle+" , COMMISSION_PCT ="+ commission+" WHERE EMPLOYEE_ID <>"+id;
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate(sql);
		return rs;
	}
}