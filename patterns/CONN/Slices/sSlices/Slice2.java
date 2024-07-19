public class Slice {public static void main() {
    String username_dcafixer;
    char[] password_dcafixerXX;
    Scanner scanner = new Scanner(System.in);
    username_dcafixer = scanner.nextLine();
    password_dcafixerXX = scanner.nextLine().toCharArray();
    scanner.close();
    con = DriverManager.getConnection(url_dcafixer, username_dcafixer, String.valueOf(password_dcafixerXX));
}}