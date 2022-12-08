package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2746 {
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
			String id = scan.nextLine();
			String status = scan.nextLine();
			int r = sendRequest(conn, id ,status );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String id ,String status ) throws SQLException {
		String sql = "Select driver_id, overall_rating from driver_performance where driver_id IN (select driver_id from driver where current_location = (select current_location from customer where customer_id ="+id+") AND current_status ="+ status+") order by overall_rating DESC";
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