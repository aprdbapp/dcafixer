package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_1289 {
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
			String val2 = scan.nextLine();
			int r = sendRequest(conn, rand0 ,val2 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String rand0 ,String val2 ) throws SQLException {
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer25 = conn.prepareStatement("SELECT COUNTRY_ID, COUNTRY_NAME, REGION_ID FROM COUNTRIES WHERE COUNTRY_NAME <= ? AND REGION_ID < ?");
pstmt_dcafixer25.setObject(1, val2);
pstmt_dcafixer25.setObject(2, rand0);
ResultSet rs = pstmt_dcafixer25.executeQuery();
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