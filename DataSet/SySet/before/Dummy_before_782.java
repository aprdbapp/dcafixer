package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_782 {
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
			String email = scan.nextLine();
			String name = scan.nextLine();
			int r = sendRequest(conn, email ,name );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String email ,String name ) throws SQLException {
		String sql = "Insert into table (Name,email) values("+name+","+email+")";
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate(sql);
		return rs;
	}
}