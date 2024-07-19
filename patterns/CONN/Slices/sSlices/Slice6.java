public class Slice {
    private static String username_dcafixer;
    private static String password_dcafixer;
    public static Connection main1() {
        username_dcafixer = System.getenv("DB_USERNAME");
        password_dcafixer = System.getenv("DB_PASSWORD");
        return DriverManager.getConnection(url_dcafixer, username_dcafixer, password_dcafixer);
    }
}