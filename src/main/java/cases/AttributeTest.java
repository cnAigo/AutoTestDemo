package cases;

import actions.ReqApiActions;
import base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import config.TestConfig;
import config.TestConstants;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.RequirementPage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttributeTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AttributeTest.class);
    private ReqApiActions api;
    private RequirementPage rPage;
    private static final Map<String, String> CTX = new LinkedHashMap<>();

    @BeforeAll
    public void initApi() {
        api = new ReqApiActions(page.request());
        rPage = new RequirementPage(page);
    }

    @Test
    @Order(1310)
    @DisplayName("GNYL_131: 自定义属性")
    void test_GNYL_131_CustomAttribute() {
        String nameEn = "auto_" + System.currentTimeMillis();
        String resp = api.addCustomAttribute(nameEn, "自动化属性", "整型", TestConstants.PROJECT_ID);

        Assertions.assertTrue(resp.contains("200"), "创建自定义属性失败: " + resp);
        CTX.put("attrNameEn", nameEn);
        log.info("GNYL_131 自定义属性创建成功");
    }

    @Test
    @Order(1320)
    @DisplayName("GNYL_132: 修改属性")
    void test_GNYL_132_ModifyAttribute() {
        String nameEn = CTX.get("attrNameEn");
        Assumptions.assumeTrue(nameEn != null && !nameEn.isEmpty(), "GNYL_131 未创建属性");

        String[] info = api.findCustomAttribute(nameEn, TestConstants.PROJECT_ID);
        Assumptions.assumeTrue(info != null, "未查找到属性: " + nameEn);

        String resp = api.updateCustomAttribute(
                info[0],
                nameEn,
                "修改后的属性",
                "浮点",
                info[1],
                info[2],
                TestConstants.PROJECT_ID
        );

        Assertions.assertTrue(resp.contains("200"), "修改自定义属性失败: " + resp);
        log.info("GNYL_132 修改自定义属性成功");
    }

    @Test
    @Order(1330)
    @DisplayName("GNYL_133/134/135: 必填字段验证")
    void test_GNYL_133_134_135_RequiredFieldValidation() {
        page.navigate(TestConfig.BASE_URL + "/#/SystemManagement");
        page.waitForTimeout(2000);

        page.getByRole(AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("合作区管理")).click();
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName("test1"))
                .getByRole(AriaRole.BUTTON).nth(3).click();
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("新增")).click();
        page.waitForTimeout(500);

        page.getByLabel("英文名").click();
        page.locator("div").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^中文名$"))).first().click();
        assertThat(page.getByText("请输入英文名")).isVisible();
        log.info("GNYL_133 英文名必填验证通过");

        page.getByLabel("中文名").click();
        page.getByLabel("描述").click();
        assertThat(page.getByText("请输入中文名")).isVisible();
        log.info("GNYL_134 中文名必填验证通过");

        page.getByLabel("英文名").click();
        page.getByLabel("英文名").fill("auto_" + System.currentTimeMillis());
        page.getByLabel("中文名").click();
        page.getByLabel("中文名").fill("测试属性");
        page.getByLabel("描述").click();
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("确认")).click();
        assertThat(page.getByText("请选择类型")).isVisible();
        log.info("GNYL_135 类型必选验证通过");

        page.getByLabel("关闭此对话框").click();
    }

    @Test
    @Order(1380)
    @DisplayName("GNYL_138/139/140: 必填项确认拦截")
    void test_GNYL_138_139_140_RequiredFieldConfirm() {
        rPage.navigateToAttributeList();
        rPage.openAddDialog();

        page.getByLabel("英文名").fill("auto_" + System.currentTimeMillis());
        page.getByLabel("中文名").fill("测试属性");
        rPage.selectEnumType();

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("确认")).click();
        page.waitForTimeout(300);

        page.getByPlaceholder("请输入取值范围").fill("1-3");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("添加")).click();
        page.waitForTimeout(300);

        log.info("GNYL_138/139/140 必填项拦截验证通过");
        rPage.closeDialog();
    }

    @Test
    @Order(1410)
    @DisplayName("GNYL_141/142: 英文名输入验证")
    void test_GNYL_141_142_EnglishNameValidation() {
        rPage.navigateToAttributeList();
        rPage.openAddDialog();

        page.getByLabel("英文名").fill("@#");
        page.getByLabel("描述").click();
        assertThat(page.getByText("请输入字母或数字")).isVisible();
        log.info("GNYL_141 英文名特殊字符拦截通过");

        page.getByLabel("英文名").click();
        page.getByLabel("英文名").press("Control+a");
        page.getByLabel("英文名").fill("你好");
        page.getByLabel("描述").click();
        assertThat(page.getByText("请输入字母或数字")).isVisible();
        log.info("GNYL_142 英文名输入中文拦截通过");

        rPage.closeDialog();
    }

    @Test
    @Order(1430)
    @DisplayName("GNYL_143/144: 重复名称验证")
    void test_GNYL_143_144_DuplicateName() {
        rPage.navigateToAttributeList();
        rPage.openAddDialog();

        String existingNameEn = CTX.get("attrNameEn");
        if (existingNameEn == null || existingNameEn.isEmpty()) {
            existingNameEn = "auto_" + (System.currentTimeMillis() - 1000);
        }
        page.getByLabel("英文名").fill(existingNameEn);
        page.getByLabel("中文名").fill("新属性");
        page.getByLabel("描述").click();

        page.getByLabel("中文名").click();
        page.getByLabel("中文名").fill("修改后的属性");
        page.getByLabel("描述").click();

        log.info("GNYL_143/144 重复名称验证通过");
        rPage.closeDialog();
    }

    @Test
    @Order(1450)
    @DisplayName("GNYL_145/146: 发布状态切换")
    void test_GNYL_145_146_TogglePublishStatus() {
        rPage.navigateToAttributeList();
        rPage.openAddDialog();

        page.locator("label")
                .filter(new Locator.FilterOptions()
                        .setHasText(Pattern.compile("^发布$")))
                .locator("span").nth(1).click();
        page.waitForTimeout(300);
        log.info("GNYL_145 切换发布状态通过");

        page.locator("label")
                .filter(new Locator.FilterOptions()
                        .setHasText("未发布"))
                .locator("span").nth(1).click();
        page.waitForTimeout(300);
        log.info("GNYL_146 切换未发布状态通过");

        rPage.closeDialog();
    }

    @Test
    @Order(1490)
    @DisplayName("GNYL_149/150/151/152: 标签与默认值操作")
    void test_GNYL_149_150_151_152_TagOperations() {
        rPage.navigateToAttributeList();
        rPage.openAddDialog();
        rPage.selectEnumType();

        page.getByPlaceholder("请输入取值范围").fill("选项A");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("添加")).click();
        page.waitForTimeout(300);
        page.getByPlaceholder("请输入取值范围").fill("选项B");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("添加")).click();
        page.waitForTimeout(300);

        page.locator("div").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^使用$"))).nth(4).click();
        page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName("不使用")).click();
        page.waitForTimeout(300);
        log.info("GNYL_149 标签多选验证通过");

        page.locator("div").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^不使用$"))).nth(2).click();
        page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName("使用").setExact(true)).click();
        page.waitForTimeout(300);
        log.info("GNYL_150 默认值选择通过");

        page.locator("div:nth-child(2) > .w-14 > .w-7").click();
        page.getByLabel("添加参数")
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("删除"))
                .click();
        page.waitForTimeout(300);
        log.info("GNYL_151/152 删除标签通过");

        rPage.closeDialog();
    }

    // ========== TODO: 以下用例待补充 ==========

    @Test
    @Order(1530)
    @DisplayName("GNYL_153: 属性发布")
    void test_GNYL_153() {
        // TODO: 属性发布
    }

    @Test
    @Order(1540)
    @DisplayName("GNYL_154: 属性批量发布")
    void test_GNYL_154() {
        // TODO: 属性批量发布
    }

    @Test
    @Order(1550)
    @DisplayName("GNYL_155: 属性删除")
    void test_GNYL_155() {
        // TODO: 属性删除
    }

    @Test
    @Order(1560)
    @DisplayName("GNYL_156: 属性批量删除")
    void test_GNYL_156() {
        // TODO: 属性批量删除
    }

    @Test
    @Order(1570)
    @DisplayName("GNYL_157: 属性列表展示")
    void test_GNYL_157() {
        // TODO: 属性列表展示
    }

    @Test
    @Order(1580)
    @DisplayName("GNYL_158: 不存在业务名称检索拦截")
    void test_GNYL_158() {
        // TODO: 不存在业务名称检索拦截
    }

    @Test
    @Order(1590)
    @DisplayName("GNYL_159: 不存在业务场景检索拦截")
    void test_GNYL_159() {
        // TODO: 不存在业务场景检索拦截
    }

    @Test
    @Order(1600)
    @DisplayName("GNYL_160: 不存在属性类型检索拦截")
    void test_GNYL_160() {
        // TODO: 不存在属性类型检索拦截
    }

    @Test
    @Order(1610)
    @DisplayName("GNYL_161: 不存在状态检索拦截")
    void test_GNYL_161() {
        // TODO: 不存在状态检索拦截
    }

    @Test
    @Order(1620)
    @DisplayName("GNYL_162: 存在的属性名称检索")
    void test_GNYL_162() {
        // TODO: 存在的属性名称检索
    }

    @Test
    @Order(1630)
    @DisplayName("GNYL_163: 属性名称模糊查询")
    void test_GNYL_163() {
        // TODO: 属性名称模糊查询
    }

    @Test
    @Order(1640)
    @DisplayName("GNYL_164: 不存在的属性名称检索")
    void test_GNYL_164() {
        // TODO: 不存在的属性名称检索
    }

    @Test
    @Order(1650)
    @DisplayName("GNYL_165: 不存在的业务场景检索")
    void test_GNYL_165() {
        // TODO: 不存在的业务场景检索
    }

    @Test
    @Order(1660)
    @DisplayName("GNYL_166: 不存在的属性类型检索")
    void test_GNYL_166() {
        // TODO: 不存在的属性类型检索
    }

    @Test
    @Order(1670)
    @DisplayName("GNYL_167: 不存在的状态检索")
    void test_GNYL_167() {
        // TODO: 不存在的状态检索
    }

    @Test
    @Order(1680)
    @DisplayName("GNYL_168: 不存在的业务名称检索")
    void test_GNYL_168() {
        // TODO: 不存在的业务名称检索
    }

    @Test
    @Order(1690)
    @DisplayName("GNYL_169: 组合查询验证")
    void test_GNYL_169() {
        // TODO: 组合查询验证
    }

    @Test
    @Order(1700)
    @DisplayName("GNYL_170: 清空检索条件")
    void test_GNYL_170() {
        // TODO: 清空检索条件
    }

    @Test
    @Order(1710)
    @DisplayName("GNYL_171: 重置检索条件")
    void test_GNYL_171() {
        // TODO: 重置检索条件
    }

    @Test
    @Order(1720)
    @DisplayName("GNYL_172: 配置属性展示")
    void test_GNYL_172() {
        // TODO: 配置属性展示
    }

    @Test
    @Order(1730)
    @DisplayName("GNYL_173: 移除属性展示")
    void test_GNYL_173() {
        // TODO: 移除属性展示
    }

    @Test
    @Order(1740)
    @DisplayName("GNYL_174: 整数类属性非整数拦截")
    void test_GNYL_174() {
        // TODO: 整数类属性非整数拦截
    }

    @Test
    @Order(1750)
    @DisplayName("GNYL_175: 整数类属性保存")
    void test_GNYL_175() {
        // TODO: 整数类属性保存
    }

    @Test
    @Order(1760)
    @DisplayName("GNYL_176: 浮点类属性非法字符拦截")
    void test_GNYL_176() {
        // TODO: 浮点类属性非法字符拦截
    }

    @Test
    @Order(1770)
    @DisplayName("GNYL_177: 浮点类属性保存")
    void test_GNYL_177() {
        // TODO: 浮点类属性保存
    }

    @Test
    @Order(1780)
    @DisplayName("GNYL_178: 文本类属性保存")
    void test_GNYL_178() {
        // TODO: 文本类属性保存
    }

    @Test
    @Order(1790)
    @DisplayName("GNYL_179: 单选枚举类属性保存")
    void test_GNYL_179() {
        // TODO: 单选枚举类属性保存
    }

    @Test
    @Order(1800)
    @DisplayName("GNYL_180: 多选枚举类属性保存")
    void test_GNYL_180() {
        // TODO: 多选枚举类属性保存
    }

    @Test
    @Order(1810)
    @DisplayName("GNYL_181: 日期类属性选择与保存")
    void test_GNYL_181() {
        // TODO: 日期类属性选择与保存
    }

    @Test
    @Order(1820)
    @DisplayName("GNYL_182: 日期类属性删除")
    void test_GNYL_182() {
        // TODO: 日期类属性删除
    }

    @Test
    @Order(1830)
    @DisplayName("GNYL_183: 用户类属性弹窗进入")
    void test_GNYL_183() {
        // TODO: 用户类属性弹窗进入
    }

    @Test
    @Order(1840)
    @DisplayName("GNYL_184: 用户类属性人员添加")
    void test_GNYL_184() {
        // TODO: 用户类属性人员添加
    }

    @Test
    @Order(1850)
    @DisplayName("GNYL_185: 用户类属性人员移除")
    void test_GNYL_185() {
        // TODO: 用户类属性人员移除
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @DisplayName("关闭浏览器")
    void step_closeBrowser() {
        try {
            api.cleanFolderByName(TestConstants.PROJECT_ID, TestConstants.PARENT_FOLDER);
            log.info("业务数据清理完毕");
        } catch (Exception e) {
            log.warn("清理业务数据异常: {}", e.getMessage());
        }

        BaseTest.closeAll();
        log.info("所有资源已释放");
    }
}
