package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2760 {
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
			String Date = scan.nextLine();
			String counter = scan.nextLine();
			int r = sendRequest(conn, Date ,counter );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String Date ,String counter ) throws SQLException {
		String sql = "select L.location_name, count(Tr.start_location) AS Number_of_Trips_Last_Month from trip_history Tr RIGHT OUTER JOIN location L ON Tr.start_location = L.location_name where COALESCE(Tr.date,CURRENT_DATE)<="+Date+" group by L.location_name having count(Tr.start_location)<"+counter;
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