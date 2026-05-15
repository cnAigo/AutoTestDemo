package base;

import cases.RequirementTest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    private static final Logger log = LoggerFactory.getLogger(RequirementTest.class);
    @BeforeAll
    public void setup() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(500));

            // 🌟 核心修改点：检查是否有保存好的登录状态
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(true);

            java.nio.file.Path authPath = java.nio.file.Paths.get("auth.json");
            if (java.nio.file.Files.exists(authPath)) {
                contextOptions.setStorageStatePath(authPath); // 加载登录状态
                log.info("🚀 检测到 auth.json，正在跳过登录并加载 Session...");
            }

            context = browser.newContext(contextOptions);
            page = context.newPage();
            page.setDefaultTimeout(30000);

        } catch (Exception e) {
            System.err.println("[ERROR] Setup 失败！" + e.getMessage());
            throw e;
        }
    }


}