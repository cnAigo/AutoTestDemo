package base;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {
    // 1. 🌟 加上 static，让所有继承它的测试类都共享这一套实例
    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext context;
    protected static Page page;

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    @BeforeAll
    public void setup() {
        // 2. 🌟 核心拦截：如果 page 已经有值了，说明前面的测试类已经开过浏览器了，直接 return 退出！
        if (page != null) {
            return;
        }

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(500));

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(true);

            java.nio.file.Path authPath = java.nio.file.Paths.get("auth.json");
            if (java.nio.file.Files.exists(authPath)) {
                contextOptions.setStorageStatePath(authPath); // 加载登录状态
                log.info("🚀 检测到 auth.json，加载 Session...");
            }

            context = browser.newContext(contextOptions);
            page = context.newPage();
            page.setDefaultTimeout(30000);

        } catch (Exception e) {
            System.err.println("[ERROR] Setup 失败！" + e.getMessage());
            throw e;
        }
    }


    public static void closeAll() {
        try { if (page != null) page.close(); } catch (Exception ignored) {}
        try { if (context != null) context.close(); } catch (Exception ignored) {}
        try { if (browser != null) browser.close(); } catch (Exception ignored) {}
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) {}

        page = null;
        context = null;
        browser = null;
        playwright = null;
    }

}