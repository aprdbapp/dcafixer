package datasets.AppsAfter;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_after_2962 {
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
			String id1 = scan.nextLine();
			String id2 = scan.nextLine();
			String id3 = scan.nextLine();
			String id4 = scan.nextLine();
			int r = sendRequest(conn, id1 ,id2 ,id3 ,id4 );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String id1 ,String id2 ,String id3 ,String id4 ) throws SQLException {
		Statement stmt = conn.createStatement();
PreparedStatement pstmt_dcafixer27 = conn.prepareStatement("SELECT * FROM EMPLOYEES WHERE (SALARY NOT BETWEEN ? AND ?) AND (SALARY NOT IN (?, ?))");
pstmt_dcafixer27.setObject(1, id1);
pstmt_dcafixer27.setObject(2, id2);
pstmt_dcafixer27.setObject(3, id3);
pstmt_dcafixer27.setObject(4, id4);
ResultSet rs = pstmt_dcafixer27.executeQuery();
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