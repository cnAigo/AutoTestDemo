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
public class ReqExportFullTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ReqExportFullTest.class);
    private ReqApiActions api;
    private RequirementPage rPage;
    private static final Map<String, String> CTX = new LinkedHashMap<>();

    @BeforeAll
    public void init() {
        api = new ReqApiActions(page.request());
        rPage = new RequirementPage(page);
    }

    // ========== 导出excel ==========
    @Test
    @Order(610)
    @DisplayName("GNYL_061: 需求规格导出excel(右键)")
    public void test_GNYL_061() {
        // TODO: 右键单击需求规格 → 导出 → excel → 选择路径保存
    }

    @Test
    @Order(620)
    @DisplayName("GNYL_062: 需求规格导出excel(表头)")
    public void test_GNYL_062() {
        // TODO: 双击需求规格 → 右侧表头点击"导出" → excel → 保存
    }

    // ========== 导出word ==========
    @Test
    @Order(640)
    @DisplayName("GNYL_064: 需求规格导出word(右键)")
    public void test_GNYL_064() {
        // TODO: 右键单击需求规格 → 导出 → word → 保存
    }

    @Test
    @Order(650)
    @DisplayName("GNYL_065: 需求规格导出word(表头)")
    public void test_GNYL_065() {
        // TODO: 双击需求规格 → 右侧表头"导出" → word → 保存
    }

    // ========== 导出ReqIf ==========
    @Test
    @Order(670)
    @DisplayName("GNYL_067: 需求规格文件夹下导出ReqIf")
    public void test_GNYL_067() {
        // TODO: 双击文件夹 → 导出 → ReqIf文件 → 填写信息 → 导出
    }

    @Test
    @Order(680)
    @DisplayName("GNYL_068: 右键文件夹导出ReqIf")
    public void test_GNYL_068() {
        // TODO: 右键单击文件夹 → 导出 → ReqIf → 填写信息 → 导出
    }

    @Test
    @Order(690)
    @DisplayName("GNYL_069: ReqIf文件名称必填测试")
    public void test_GNYL_069() {
        // TODO: 清空ReqIf文件名称 → 点击导出 → 验证提示
    }

    @Test
    @Order(700)
    @DisplayName("GNYL_070: ReqIf属性名称必填测试")
    public void test_GNYL_070() {
        // TODO: 未填写ReqIf属性名称 → 点击导出 → 验证提示
    }

    @Test
    @Order(710)
    @DisplayName("GNYL_071: ReqIf属性数据类型必选测试")
    public void test_GNYL_071() {
        // TODO: 未选择数据类型 → 点击导出 → 验证提示
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @DisplayName("关闭浏览器")
    void step_closeBrowser() {
        BaseTest.closeAll();
        log.info("所有资源已释放");
    }
}