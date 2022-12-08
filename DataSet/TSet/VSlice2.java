public class Slice {public static void main() {
sql = "Select * from ACCOUNTS where custid = " + colval + ";";
ResultSet rs = stmt.executeQuery(sql);
}}