package datasets.Apps_Before;
import java.sql.*;
import java.util.Scanner;
import java.io.IOException;

public class Dummy_before_2704 {
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
			String days = scan.nextLine();
			String sal = scan.nextLine();
			int r = sendRequest(conn, days ,sal );
			System.out.println(r);
		}
	}
	public static int sendRequest(Connection conn, String days ,String sal ) throws SQLException {
		String sql = "select e1.employee_id,e1.job_id,e2.job_title from employees e1 ,jobs e2,job_history e3 where e1.salary>"+sal+" and e1.job_id=e2.job_id and e1.employee_id=e3.employee_id and (sysdate-e3.start_date)/365>="+ days;
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