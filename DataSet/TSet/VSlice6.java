public class Slice {public static void main() {
String sql = "Select * from ACCOUNTS where custid = " + colval + ";";
ResultSet rs = stmt.executeQuery(sql);
}}