package config;

public class TestConfig {

    public static final String BASE_URL = System.getenv("TEST_BASE_URL") != null
            ? System.getenv("TEST_BASE_URL")
            : "https://192.168.0.222:8088";

    public static final String LOGIN_URL = BASE_URL + "/#/login";
    public static final String REQUIREMENT_URL = BASE_URL + "/#/RequirementManagement";
    public static final String API_PREFIX = BASE_URL + "/dev-api";

    public static final String ADMIN_USER = System.getenv("TEST_USER") != null
            ? System.getenv("TEST_USER")
            : "admin";

    public static final String ADMIN_PWD = System.getenv("TEST_PASSWORD") != null
            ? System.getenv("TEST_PASSWORD")
            : "Aa123456";

    public static final String AUTH_STATE_PATH = "auth.json";
}
