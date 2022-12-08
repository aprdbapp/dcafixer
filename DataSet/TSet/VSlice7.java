public class Slice {public static void main() {
String sql = "Select * from ACCOUNTS where custid = " + colval + ";";
try ( rs = stmt.executeQuery(sql)) {
}}