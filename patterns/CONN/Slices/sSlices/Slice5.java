public class Slice {public static Connection main1() {
    String username_dcafixer = System.getenv("DB_USERNAME");
    String password_dcafixer = System.getenv("DB_PASSWORD");
    return DriverManager.getConnection(url_dcafixer, username_dcafixer, password_dcafixer);
}}