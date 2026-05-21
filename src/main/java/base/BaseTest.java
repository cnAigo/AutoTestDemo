package base;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext context;
    protected static Page page;

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    private static final String AUTH_STATE_PATH = "auth.json";

    @BeforeAll
    public void setup() {
        if (page != null) {
            return;
        }

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(0));

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(true);

            java.nio.file.Path authPath = Paths.get(AUTH_STATE_PATH);
            if (java.nio.file.Files.exists(authPath)) {
                contextOptions.setStorageStatePath(authPath);
                log.info("Detected auth.json, loading session...");
            }

            context = browser.newContext(contextOptions);
            page = context.newPage();
            page.setDefaultTimeout(30000);

        } catch (Exception e) {
            log.error("Setup failed: {}", e.getMessage());
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