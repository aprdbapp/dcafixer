package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2781 {
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
			String eid = scan.nextLine();
			String jid = scan.nextLine();
			int r = sendRequest(conn, eid ,jid );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String eid ,String jid ) throws SQLException {
		Statement stmt = conn.createStatement();
		int rs = stmt.executeUpdate("INSERT into table(employee_id,job_id) values("+eid+","+jid+")");
		return rs;
	}
}