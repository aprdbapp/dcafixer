public class Slice {
    private static String USER = "DCAFixer_un_litral";
    private static String PASS = "DCAFixer_pw_litral";
    public static Connection main1() {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}