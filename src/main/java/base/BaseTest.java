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
                    .setHeadless(false) // 方便你在本地看动作
                    .setSlowMo(500));   // 稍微慢一点，更稳

            context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(true));
            page = context.newPage();

            // 设置全局默认等待时间为 30 秒，防止死等
            page.setDefaultTimeout(30000);


        } catch (Exception e) {
            System.err.println("[ERROR] Setup 失败！原因: " + e.getMessage());
            e.printStackTrace();
            throw e; // 抛出异常让 JUnit 停止后续无效执行
        }
    }

    @AfterAll
    public void tearDown() {
        log.info("[Cleanup] 正在关闭浏览器并释放资源...");
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("====== 测试环境已安全关闭 ======");
    }
}