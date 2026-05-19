package cases;



import actions.ReqApiActions;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import config.TestConfig;
import config.TestConstants;
import base.BaseTest;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttributeTest extends BaseTest {


    private static final Logger log = LoggerFactory.getLogger(AttributeTest.class);
    private ReqApiActions api;

    private static final java.util.Map<String, String> CTX = new java.util.LinkedHashMap<>();

    @BeforeAll
    public void initApi() {
        api = new ReqApiActions(page.request());
    }


    @Test
    @Order(1310)
    @DisplayName("GNYL_131: 自定义属性")
    void test_GNYL_131_CustomAttribute() {
        String nameEn = "auto_" + System.currentTimeMillis();
        String resp = api.addCustomAttribute(nameEn, "自动化属性", "整型", TestConstants.PROJECT_ID);

        Assertions.assertTrue(resp.contains("200"), "创建自定义属性失败: " + resp);
        CTX.put("attrNameEn", nameEn);
        log.info("GNYL_131 自定义属性创建成功！");
    }



    @Test
    @Order(1320)
    @DisplayName("GNYL_132: 修改属性")
    void test_GNYL_132_ModifyAttribute() {
        String nameEn = CTX.get("attrNameEn");
        Assumptions.assumeTrue(nameEn != null && !nameEn.isEmpty(), "GNYL_131 未创建属性");

        // 1. 查找刚创建的属性，拿到 id
        String[] info = api.findCustomAttribute(nameEn, TestConstants.PROJECT_ID);
        Assumptions.assumeTrue(info != null, "未查找到属性: " + nameEn);

        // 2. 修改：改中文名和类型
        String resp = api.updateCustomAttribute(
                info[0],           // id
                nameEn,            // nameEn 保持不变
                "修改后的属性",     // 新中文名
                "浮点",            // 新类型
                info[1],           // createTime
                info[2],           // creator
                TestConstants.PROJECT_ID
        );

        Assertions.assertTrue(resp.contains("200"), "修改自定义属性失败: " + resp);
        log.info("GNYL_132 修改自定义属性成功！");
    }


    @Test
    @Order(1330)
    @DisplayName("GNYL_133: 英文名必填验证")
    void test_GNYL_133() {
    }

    @Test
    @Order(1340)
    @DisplayName("GNYL_134: 中文名必填验证")
    void test_GNYL_134() {
    }

    @Test
    @Order(1350)
    @DisplayName("GNYL_135: 类型必选验证")
    void test_GNYL_135() {
    }

    @Test
    @Order(1360)
    @DisplayName("GNYL_136: 选择枚举类型验证")
    void test_GNYL_136() {
    }

    @Test
    @Order(1370)
    @DisplayName("GNYL_137: 未输入枚举值范围验证")
    void test_GNYL_137() {
    }

    @Test
    @Order(1380)
    @DisplayName("GNYL_138: 枚举值范围非空测试")
    void test_GNYL_138() {
    }

    @Test
    @Order(1390)
    @DisplayName("GNYL_139: 默认值非空测试")
    void test_GNYL_139() {
    }

    @Test
    @Order(1400)
    @DisplayName("GNYL_140: 是否必填必选测试")
    void test_GNYL_140() {
    }

    @Test
    @Order(1410)
    @DisplayName("GNYL_141: 英文名输入特殊字符验证")
    void test_GNYL_141() {
    }

    @Test
    @Order(1420)
    @DisplayName("GNYL_142: 英文名输入中文验证")
    void test_GNYL_142() {
    }

    @Test
    @Order(1430)
    @DisplayName("GNYL_143: 重复英文名验证")
    void test_GNYL_143() {
    }

    @Test
    @Order(1440)
    @DisplayName("GNYL_144: 重复中文名验证")
    void test_GNYL_144() {
    }

    @Test
    @Order(1450)
    @DisplayName("GNYL_145: 切换发布状态")
    void test_GNYL_145() {
    }

    @Test
    @Order(1460)
    @DisplayName("GNYL_146: 编辑发布状态")
    void test_GNYL_146() {
    }

    @Test
    @Order(1470)
    @DisplayName("GNYL_147: 添加合理的标签")
    void test_GNYL_147() {
    }

    @Test
    @Order(1480)
    @DisplayName("GNYL_148: 重复添加标签")
    void test_GNYL_148() {
    }

    @Test
    @Order(1490)
    @DisplayName("GNYL_149: 标签多选验证")
    void test_GNYL_149() {
    }

    @Test
    @Order(1500)
    @DisplayName("GNYL_150: 默认值选择")
    void test_GNYL_150() {
    }

    @Test
    @Order(1510)
    @DisplayName("GNYL_151: 默认值重选验证")
    void test_GNYL_151() {
    }

    @Test
    @Order(1520)
    @DisplayName("GNYL_152: 删除标签")
    void test_GNYL_152() {
    }

    // ==========================================
    // 🚀 属性发布与删除 (GNYL_153 - GNYL_156)
    // ==========================================

    @Test
    @Order(1530)
    @DisplayName("GNYL_153: 属性发布")
    void test_GNYL_153() {
    }

    @Test
    @Order(1540)
    @DisplayName("GNYL_154: 属性批量发布")
    void test_GNYL_154() {
    }

    @Test
    @Order(1550)
    @DisplayName("GNYL_155: 属性删除")
    void test_GNYL_155() {
    }

    @Test
    @Order(1560)
    @DisplayName("GNYL_156: 属性批量删除")
    void test_GNYL_156() {
    }

    // ==========================================
    // 🔍 属性检索功能 (GNYL_157 - GNYL_171)
    // ==========================================

    @Test
    @Order(1570)
    @DisplayName("GNYL_157: 属性列表展示")
    void test_GNYL_157() {
    }

    @Test
    @Order(1580)
    @DisplayName("GNYL_158: 不存在业务名称检索拦截")
    void test_GNYL_158() {
    }

    @Test
    @Order(1590)
    @DisplayName("GNYL_159: 不存在业务场景检索拦截")
    void test_GNYL_159() {
    }

    @Test
    @Order(1600)
    @DisplayName("GNYL_160: 不存在属性类型检索拦截")
    void test_GNYL_160() {
    }

    @Test
    @Order(1610)
    @DisplayName("GNYL_161: 不存在状态检索拦截")
    void test_GNYL_161() {
    }

    @Test
    @Order(1620)
    @DisplayName("GNYL_162: 存在的属性名称检索")
    void test_GNYL_162() {
    }

    @Test
    @Order(1630)
    @DisplayName("GNYL_163: 属性名称模糊查询")
    void test_GNYL_163() {
    }

    @Test
    @Order(1640)
    @DisplayName("GNYL_164: 不存在的属性名称检索")
    void test_GNYL_164() {
    }

    @Test
    @Order(1650)
    @DisplayName("GNYL_165: 不存在的业务场景检索")
    void test_GNYL_165() {
    }

    @Test
    @Order(1660)
    @DisplayName("GNYL_166: 不存在的属性类型检索")
    void test_GNYL_166() {
    }

    @Test
    @Order(1670)
    @DisplayName("GNYL_167: 不存在的状态检索")
    void test_GNYL_167() {
    }

    @Test
    @Order(1680)
    @DisplayName("GNYL_168: 不存在的业务名称检索")
    void test_GNYL_168() {
    }

    @Test
    @Order(1690)
    @DisplayName("GNYL_169: 组合查询验证")
    void test_GNYL_169() {
    }

    @Test
    @Order(1700)
    @DisplayName("GNYL_170: 清空检索条件")
    void test_GNYL_170() {
    }

    @Test
    @Order(1710)
    @DisplayName("GNYL_171: 重置检索条件")
    void test_GNYL_171() {
    }

    // ==========================================
    // 🖼️ 属性配置与视图 (GNYL_172 - GNYL_173)
    // ==========================================

    @Test
    @Order(1720)
    @DisplayName("GNYL_172: 配置属性展示")
    void test_GNYL_172() {
    }

    @Test
    @Order(1730)
    @DisplayName("GNYL_173: 移除属性展示")
    void test_GNYL_173() {
    }

    // ==========================================
    // ✅ 自定义属性类型校验 (GNYL_174 - GNYL_185)
    // ==========================================

    @Test
    @Order(1740)
    @DisplayName("GNYL_174: 整数类属性非整数拦截")
    void test_GNYL_174() {
    }

    @Test
    @Order(1750)
    @DisplayName("GNYL_175: 整数类属性保存")
    void test_GNYL_175() {
    }

    @Test
    @Order(1760)
    @DisplayName("GNYL_176: 浮点类属性非法字符拦截")
    void test_GNYL_176() {
    }

    @Test
    @Order(1770)
    @DisplayName("GNYL_177: 浮点类属性保存")
    void test_GNYL_177() {
    }

    @Test
    @Order(1780)
    @DisplayName("GNYL_178: 文本类属性保存")
    void test_GNYL_178() {
    }

    @Test
    @Order(1790)
    @DisplayName("GNYL_179: 单选枚举类属性保存")
    void test_GNYL_179() {
    }

    @Test
    @Order(1800)
    @DisplayName("GNYL_180: 多选枚举类属性保存")
    void test_GNYL_180() {
    }

    @Test
    @Order(1810)
    @DisplayName("GNYL_181: 日期类属性选择与保存")
    void test_GNYL_181() {
    }

    @Test
    @Order(1820)
    @DisplayName("GNYL_182: 日期类属性删除")
    void test_GNYL_182() {
    }

    @Test
    @Order(1830)
    @DisplayName("GNYL_183: 用户类属性弹窗进入")
    void test_GNYL_183() {
    }

    @Test
    @Order(1840)
    @DisplayName("GNYL_184: 用户类属性人员添加")
    void test_GNYL_184() {
    }

    @Test
    @Order(1850)
    @DisplayName("GNYL_185: 用户类属性人员移除")
    void test_GNYL_185() {
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
