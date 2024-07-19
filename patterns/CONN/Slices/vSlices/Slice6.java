public class Slice {
    private static String username_dcafixer = "DCAFixer_un_litral";
    private static String password_dcafixer = "DCAFixer_pw_litral";
    public static Connection main1() {
        return DriverManager.getConnection(url_dcafixer, username_dcafixer, password_dcafixer);
    }
}