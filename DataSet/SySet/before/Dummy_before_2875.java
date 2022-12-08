package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2875 {
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
			String str1 = scan.nextLine();
			String str2 = scan.nextLine();
			String str3 = scan.nextLine();
			int r = sendRequest(conn, str1 ,str2 ,str3 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String str1 ,String str2 ,String str3 ) throws SQLException {
		String sql = "SELECT LOCATION_ID,(STREET_ADDRESS || ', '||CITY|| ', '|| STATE_PROVINCE|| ', '|| POSTAL_CODE) ADDRESS FROM LOCATIONS JOIN COUNTRIES USING(COUNTRY_ID) WHERE COUNTRY_NAME IN("+str1+","+str2+","+str3+")";
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