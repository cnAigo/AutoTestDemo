package config;

public class TestConfig {
    // 基础 URL
    public static final String BASE_URL = "https://192.168.6.171:8088";

    // 组合后的页面地址
    public static final String LOGIN_URL = BASE_URL + "/#/login";
    public static final String REQUIREMENT_URL = BASE_URL + "/#/RequirementManagement";

    // API 接口前缀
    public static final String API_PREFIX = BASE_URL + "/dev-api";

    // 也可以放通用的账号密码
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PWD = "Aa123456";
}