package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2754 {
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
			String counter = scan.nextLine();
			String id = scan.nextLine();
			int r = sendRequest(conn, counter ,id );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String counter ,String id ) throws SQLException {
		String sql = "select V1.owner_id, count(*) AS Number_of_Vehicles from vehicle V1 where V1.owner_id in (select owner_id from owner where owner_id>"+id+") group by V1.owner_id having count(*)<="+counter+" order by Number_of_Vehicles DESC,V1.owner_id DESC";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
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