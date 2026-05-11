package com.test.base;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(300));
        context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
        page = context.newPage();

        page.setDefaultTimeout(150000);
        page.setDefaultNavigationTimeout(30000);

        page.navigate("https://192.168.0.222:8088/#/login");
        page.getByPlaceholder("请输入用户名").fill("zhangke");
        page.getByPlaceholder("请输入密码").fill("Aa123456");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("登 录")).click();

        // 稳妥地等待元素出现并点击
        Locator appBtn = page.getByText("应用");
        appBtn.waitFor();
        appBtn.click();

        page.locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^需求管理$"))).click();

        // 1. 定位根节点元素
        Locator rootNode = page.getByText("需求（根节点）").first();

        // 2. ⚡ 核心：显式等待它“出现在界面上”且“变为可见状态”
        rootNode.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 3. 再进行断言确认
        assertThat(rootNode).isVisible();

        System.out.println("====== 根节点已加载，开始执行后续操作 ======");
    }

    @AfterAll
    public void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        System.out.println("====== 浏览器已退出 ======");
    }
}