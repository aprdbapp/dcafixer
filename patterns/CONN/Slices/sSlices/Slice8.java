public class Slice {public static void main() {
    Scanner scanner = new Scanner(System.in);
    String username_dcafixer = scanner.nextLine();
    char[] password_dcafixerXX = scanner.nextLine().toCharArray();
    scanner.close();
    Connection conn_dcafixer = DriverManager.getConnection(url_dcafixer, username_dcafixer, String.valueOf(password_dcafixerXX));
}}