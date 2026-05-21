package cases;

import actions.ReqApiActions;
import base.BaseTest;
import config.TestConstants;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.RequirementPage;

import java.util.LinkedHashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReqFolderTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ReqFolderTest.class);
    private ReqApiActions api;
    private RequirementPage rPage;
    private static final Map<String, String> CTX = new LinkedHashMap<>();

    @BeforeAll
    public void init() {
        api = new ReqApiActions(page.request());
        rPage = new RequirementPage(page);
    }

    // ========== 需求结构树 - 新建文件夹 ==========
    @Test
    @Order(160)
    @DisplayName("GNYL_016: 需求规格下新建同级文件夹")
    public void test_GNYL_016() {
        // TODO: 右键点击需求规格 → 新建 → 文件夹
    }

    // ========== 需求结构树 - 删除文件夹 ==========
    @Test
    @Order(280)
    @DisplayName("GNYL_028: 删除无子级的文件夹(UI)")
    public void test_GNYL_028() {
        // TODO: 双击文件夹 → 右键单击无子级文件夹 → 删除
    }

    @Test
    @Order(300)
    @DisplayName("GNYL_030: 取消删除文件夹(UI)")
    public void test_GNYL_030() {
        // TODO: 双击文件夹 → 右键单击有删除标识文件夹 → 取消删除
    }

    @Test
    @Order(310)
    @DisplayName("GNYL_031: 清除文件夹")
    public void test_GNYL_031() {
        // TODO: 右键单击有删除标识文件夹 → 清除 → 确定
    }

    @Test
    @Order(320)
    @DisplayName("GNYL_032: 清除文件夹(UI)")
    public void test_GNYL_032() {
        // TODO: 双击文件夹 → 右键单击有删除标识文件夹 → 清除
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @DisplayName("关闭浏览器")
    void step_closeBrowser() {
        BaseTest.closeAll();
        log.info("所有资源已释放");
    }
}