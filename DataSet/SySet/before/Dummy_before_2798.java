package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2798 {
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
			String GPA = scan.nextLine();
			int r = sendRequest(conn, GPA );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String GPA ) throws SQLException {
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate("INSERT into goodStudents(id,name) select id,name from students  where gpa= "+GPA);
		return rs;
	}
}