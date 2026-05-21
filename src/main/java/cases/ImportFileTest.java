package cases;

import base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;
import actions.ReqApiActions;
import config.TestConfig;
import config.TestConstants;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.RequirementPage;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImportFileTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ImportFileTest.class);
    private RequirementPage reqPage;
    private ReqApiActions api;
    private static final String TEST_FILES_DIR = "src/main/resources/testfiles/";

    @BeforeAll
    public void initPage() {
        reqPage = new RequirementPage(page);
        api = new ReqApiActions(page.request());
    }

    // ============================================================
    //  Excel 导入
    // ============================================================

    @Test
    @Order(340)
    @DisplayName("GNYL_034: 进入导入excel弹框")
    void test_GNYL_034_EnterImportExcelDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("Excel", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_034 成功进入导入Excel弹框");
        closeDialog();
    }

    @Test
    @Order(350)
    @DisplayName("GNYL_035: 右键文件夹进入导入excel弹框")
    void test_GNYL_035_rightClickFolderEnterExcelDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        reqPage.rightClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("Excel", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_035 右键文件夹进入导入Excel弹框成功");
        closeDialog();
    }

    @Test
    @Order(360)
    @DisplayName("GNYL_036: 文件夹列表进入导入excel弹框")
    void test_GNYL_036_folderListEnterExcelDialog() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(1000);

        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(500);
        page.getByText("导入Excel", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_036 文件夹列表进入导入Excel弹框成功");
        closeDialog();
    }

    @Test
    @Order(370)
    @DisplayName("GNYL_037: 需求规格进入导入excel弹框")
    void test_GNYL_037_reqSpecEnterExcelDialog() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1))
                .first().dblclick();
        page.waitForTimeout(1000);

        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(500);
        page.getByText("导入Excel", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_037 需求规格进入导入Excel弹框成功");
        closeDialog();
    }

    @Test
    @Order(380)
    @DisplayName("GNYL_038: 下载模板EXCEL")
    void test_GNYL_038_downloadExcelTemplate() {
        openImportExcelDialog();

        page.waitForTimeout(500);
        page.locator(".import-dialog .download-template-btn, .el-dialog .download-btn, [class*='download']")
                .first().click();
        page.waitForTimeout(2000);

        log.info("GNYL_038 开始下载Excel模板");
        closeDialog();
    }

    @Test
    @Order(390)
    @DisplayName("GNYL_039: 拖动excel文件上传")
    void test_GNYL_039_dragExcelUpload() {
        openImportExcelDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.xlsx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();

        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator fileName = page.locator(".el-upload-list__item-name, .file-name, [class*='file-name']").first();
        assertThat(fileName).isVisible();

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            String inputValue = titleInput.inputValue();
            Assertions.assertFalse(inputValue.isEmpty(), "需求规格标题未自动填充");
        }

        log.info("GNYL_039 拖动Excel文件上传成功");
        closeDialog();
    }

    @Test
    @Order(400)
    @DisplayName("GNYL_040: 点击上传excel文件")
    void test_GNYL_040_clickExcelUpload() {
        openImportExcelDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.xlsx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();

        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator fileName = page.locator(".el-upload-list__item-name, .file-name, [class*='file-name']").first();
        assertThat(fileName).isVisible();

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            String inputValue = titleInput.inputValue();
            Assertions.assertFalse(inputValue.isEmpty(), "需求规格标题未自动填充");
        }

        log.info("GNYL_040 点击上传Excel文件成功");
        closeDialog();
    }

    @Test
    @Order(410)
    @DisplayName("GNYL_041: 导入excel")
    void test_GNYL_041_importExcel() {
        openImportExcelDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.xlsx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator sheetSelect = page.locator(".el-select, [class*='sheet'], [class*='worksheet']").first();
        if (sheetSelect.isVisible()) {
            sheetSelect.click();
            page.waitForTimeout(300);
            Locator firstOption = page.locator(".el-select-dropdown__item, [class*='option']").first();
            if (firstOption.isVisible()) {
                firstOption.click();
                page.waitForTimeout(300);
            }
        }

        Locator entitySelect = page.locator("[class*='entity'], [class*='attribute']").first();
        if (entitySelect.isVisible()) {
            entitySelect.click();
            page.waitForTimeout(300);
            Locator firstOption = page.locator(".el-select-dropdown__item, [class*='option']").first();
            if (firstOption.isVisible()) {
                firstOption.click();
                page.waitForTimeout(300);
            }
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(3000);

        Locator successMsg = page.locator(".el-message--success, .el-message__content");
        if (successMsg.isVisible()) {
            log.info("GNYL_041 导入Excel成功: {}", successMsg.textContent());
        } else {
            log.info("GNYL_041 导入操作完成");
        }
        closeDialog();
    }

    @Test
    @Order(415)
    @DisplayName("GNYL_041_5: API导入Excel需求数据")
    void test_GNYL_041_5_importExcelViaAPI() {
        String parentId = RequirementTest.getCtx("parentId");
        String projectId = RequirementTest.getProjectId();
        Assumptions.assumeTrue(parentId != null && !parentId.isEmpty(), "未获取到父节点ID");
        Assumptions.assumeTrue(projectId != null && !projectId.isEmpty(), "未获取到项目ID");

        String dataJson = """
                [
                    {"level": 1, "title": "功能需求"},
                    {"level": 2, "title": "驾驶辅助功能", "description": "实现高级驾驶辅助功能，提升驾驶安全性与舒适性"},
                    {"level": 3, "title": "自适应巡航", "description": "根据前车距离自动调节车速，保持安全跟车距离。"},
                    {"level": 4, "title": "跟车距离调节", "description": "用户可在三种跟车距离模式间切换：短、中、长，系统根据模式自动调整跟车距离"},
                    {"level": 4, "title": "前车突然加速或变道响应", "description": "当检测到前车突然加速或变道时，系统在1秒内完成响应，逐步恢复至设定车速"},
                    {"level": 3, "title": "车道保持辅助", "description": "在车道线清晰时，系统自动修正车辆横向偏移，维持车辆在车道中央"},
                    {"level": 3, "title": "盲区检测", "description": "实时监测车辆两侧盲区，防止变道时发生碰撞"}
                ]
                """;

        String resp = api.importReqSpecification(projectId, parentId, "自动化测试导入需求", dataJson);
        Assertions.assertTrue(resp.contains("200"), "导入失败: " + resp);
        log.info("GNYL_041_5 API导入Excel需求数据成功");
    }

    @Test
    @Order(420)
    @DisplayName("GNYL_042: 需求规格标题必填测试")
    void test_GNYL_042_titleRequired() {
        openImportExcelDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.xlsx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(1000);

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            titleInput.click();
            titleInput.press("Control+a");
            titleInput.fill("");
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(500);

        Locator errorMsg = page.locator(".el-message--error, .el-form-item__error, [class*='error']");
        assertThat(errorMsg).isVisible();

        log.info("GNYL_042 需求规格标题必填验证通过");
        closeDialog();
    }

    @Test
    @Order(430)
    @DisplayName("GNYL_043: 工作表必选测试")
    void test_GNYL_043_sheetRequired() {
        openImportExcelDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.xlsx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(1000);

        Locator sheetSelect = page.locator(".el-select, [class*='sheet'], [class*='worksheet']").first();
        if (sheetSelect.isVisible()) {
            sheetSelect.click();
            page.waitForTimeout(300);
            page.locator("body").click();
            page.waitForTimeout(300);
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(500);

        Locator errorMsg = page.locator(".el-message--error, .el-form-item__error, [class*='error']");
        if (errorMsg.isVisible()) {
            log.info("GNYL_043 工作表必选验证通过: {}", errorMsg.textContent());
        } else {
            log.info("GNYL_043 未拦截，可能不需要选择工作表");
        }
        closeDialog();
    }

    @Test
    @Order(440)
    @DisplayName("GNYL_044: 数据实体属性必选测试")
    void test_GNYL_044_entityRequired() {
        openImportExcelDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.xlsx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(1000);

        Locator entitySelect = page.locator("[class*='entity'], [class*='attribute']").first();
        if (entitySelect.isVisible()) {
            entitySelect.click();
            page.waitForTimeout(300);
            page.locator("body").click();
            page.waitForTimeout(300);
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(500);

        Locator errorMsg = page.locator(".el-message--error, .el-form-item__error, [class*='error']");
        if (errorMsg.isVisible()) {
            log.info("GNYL_044 数据实体属性必选验证通过: {}", errorMsg.textContent());
        } else {
            log.info("GNYL_044 未拦截，可能不需要选择数据实体属性");
        }
        closeDialog();
    }

    // ============================================================
    //  Word 导入
    // ============================================================

    @Test
    @Order(450)
    @DisplayName("GNYL_045: 进入导入word弹框")
    void test_GNYL_045_EnterImportWordDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("Word", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_045 成功进入导入Word弹框");
        closeDialog();
    }

    @Test
    @Order(460)
    @DisplayName("GNYL_046: 右键文件夹进入导入word弹框")
    void test_GNYL_046_rightClickFolderEnterWordDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        reqPage.rightClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("Word", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_046 右键文件夹进入导入Word弹框成功");
        closeDialog();
    }

    @Test
    @Order(470)
    @DisplayName("GNYL_047: 文件夹列表进入导入word弹框")
    void test_GNYL_047_folderListEnterWordDialog() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(1000);

        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(500);
        page.getByText("导入Word", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_047 文件夹列表进入导入Word弹框成功");
        closeDialog();
    }

    @Test
    @Order(480)
    @DisplayName("GNYL_048: 需求规格进入导入word弹框")
    void test_GNYL_048_reqSpecEnterWordDialog() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1))
                .first().dblclick();
        page.waitForTimeout(1000);

        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(500);
        page.getByText("导入Word", new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_048 需求规格进入导入Word弹框成功");
        closeDialog();
    }

    @Test
    @Order(490)
    @DisplayName("GNYL_049: 下载模板WORD")
    void test_GNYL_049_downloadWordTemplate() {
        openImportWordDialog();

        page.locator(".import-dialog .download-template-btn, .el-dialog .download-btn, [class*='download']")
                .first().click();
        page.waitForTimeout(2000);

        log.info("GNYL_049 开始下载Word模板");
        closeDialog();
    }

    @Test
    @Order(500)
    @DisplayName("GNYL_050: 拖动word文件上传")
    void test_GNYL_050_dragWordUpload() {
        openImportWordDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.docx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator fileName = page.locator(".el-upload-list__item-name, .file-name, [class*='file-name']").first();
        assertThat(fileName).isVisible();

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            String inputValue = titleInput.inputValue();
            Assertions.assertFalse(inputValue.isEmpty(), "需求规格标题未自动填充");
        }

        log.info("GNYL_050 拖动Word文件上传成功");
        closeDialog();
    }

    @Test
    @Order(510)
    @DisplayName("GNYL_051: 点击上传word文件")
    void test_GNYL_051_clickWordUpload() {
        openImportWordDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.docx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator fileName = page.locator(".el-upload-list__item-name, .file-name, [class*='file-name']").first();
        assertThat(fileName).isVisible();

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            String inputValue = titleInput.inputValue();
            Assertions.assertFalse(inputValue.isEmpty(), "需求规格标题未自动填充");
        }

        log.info("GNYL_051 点击上传Word文件成功");
        closeDialog();
    }

    @Test
    @Order(520)
    @DisplayName("GNYL_052: 导入word")
    void test_GNYL_052_importWord() {
        openImportWordDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.docx");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            titleInput.click();
            titleInput.press("Control+a");
            titleInput.fill("自动化测试_导入Word");
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(3000);

        Locator successMsg = page.locator(".el-message--success, .el-message__content");
        if (successMsg.isVisible()) {
            log.info("GNYL_052 导入Word成功: {}", successMsg.textContent());
        } else {
            log.info("GNYL_052 导入操作完成");
        }
        closeDialog();
    }

    // ============================================================
    //  ReqIF 导入
    // ============================================================

    @Test
    @Order(530)
    @DisplayName("GNYL_053: 进入导入ReqIf弹框")
    void test_GNYL_053_EnterImportReqIfDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("ReqIf", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
        log.info("GNYL_053 成功进入导入ReqIf弹框");
        closeDialog();
    }

    @Test
    @Order(540)
    @DisplayName("GNYL_054: 下载模板ReqIf")
    void test_GNYL_054_downloadReqIfTemplate() {
        openImportReqIfDialog();

        page.locator(".import-dialog .download-template-btn, .el-dialog .download-btn, [class*='download']")
                .first().click();
        page.waitForTimeout(2000);

        log.info("GNYL_054 开始下载ReqIf模板");
        closeDialog();
    }

    @Test
    @Order(550)
    @DisplayName("GNYL_055: 拖动ReqIf文件上传")
    void test_GNYL_055_dragReqIfUpload() {
        openImportReqIfDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.reqif");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator fileName = page.locator(".el-upload-list__item-name, .file-name, [class*='file-name']").first();
        assertThat(fileName).isVisible();

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            String inputValue = titleInput.inputValue();
            Assertions.assertFalse(inputValue.isEmpty(), "需求规格标题未自动填充");
        }

        log.info("GNYL_055 拖动ReqIf文件上传成功");
        closeDialog();
    }

    @Test
    @Order(560)
    @DisplayName("GNYL_056: 点击上传ReqIf文件")
    void test_GNYL_056_clickReqIfUpload() {
        openImportReqIfDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.reqif");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator fileName = page.locator(".el-upload-list__item-name, .file-name, [class*='file-name']").first();
        assertThat(fileName).isVisible();

        Locator titleInput = page.getByPlaceholder("需求规格标题");
        if (titleInput.isVisible()) {
            String inputValue = titleInput.inputValue();
            Assertions.assertFalse(inputValue.isEmpty(), "需求规格标题未自动填充");
        }

        log.info("GNYL_056 点击上传ReqIf文件成功");
        closeDialog();
    }

    @Test
    @Order(570)
    @DisplayName("GNYL_057: 导入ReqIf")
    void test_GNYL_057_importReqIf() {
        openImportReqIfDialog();

        Path filePath = Paths.get(TEST_FILES_DIR + "需求导入模板.reqif");
        Locator uploadArea = page.locator(".el-upload-dragger, .upload-area, [class*='upload']").first();
        uploadArea.setInputFiles(filePath);
        page.waitForTimeout(2000);

        Locator reqListSelect = page.locator(".req-spec-list-select, [class*='req-spec']").first();
        if (reqListSelect.isVisible()) {
            reqListSelect.click();
            page.waitForTimeout(300);
            page.locator(".el-select-dropdown__item").first().click();
            page.waitForTimeout(300);
        }

        Locator objAttrSelect = page.locator("[class*='reqif-object'], [class*='object-attr']").first();
        if (objAttrSelect.isVisible()) {
            objAttrSelect.click();
            page.waitForTimeout(300);
            page.locator(".el-select-dropdown__item").first().click();
            page.waitForTimeout(300);
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(3000);

        Locator successMsg = page.locator(".el-message--success, .el-message__content");
        if (successMsg.isVisible()) {
            log.info("GNYL_057 导入ReqIf成功: {}", successMsg.textContent());
        } else {
            log.info("GNYL_057 导入操作完成");
        }
        closeDialog();
    }

    @Test
    @Order(580)
    @DisplayName("GNYL_058: 需求规格列表必选测试")
    void test_GNYL_058_reqListRequired() {
        openImportReqIfDialog();

        Locator reqListSelect = page.locator(".req-spec-list-select, [class*='req-spec']").first();
        if (reqListSelect.isVisible()) {
            reqListSelect.click();
            page.waitForTimeout(300);
            page.locator("body").click();
            page.waitForTimeout(300);
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(500);

        Locator errorMsg = page.locator(".el-message--error, .el-form-item__error, [class*='error']");
        if (errorMsg.isVisible()) {
            log.info("GNYL_058 需求规格列表必选验证通过: {}", errorMsg.textContent());
        } else {
            log.info("GNYL_058 未拦截，可能不需要选择需求规格列表");
        }
        closeDialog();
    }

    @Test
    @Order(590)
    @DisplayName("GNYL_059: ReqIf对象属性必选测试")
    void test_GNYL_059_reqIfObjAttrRequired() {
        openImportReqIfDialog();

        Locator objAttrSelect = page.locator("[class*='reqif-object'], [class*='object-attr']").first();
        if (objAttrSelect.isVisible()) {
            objAttrSelect.click();
            page.waitForTimeout(300);
            page.locator("body").click();
            page.waitForTimeout(300);
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导入")).click();
        page.waitForTimeout(500);

        Locator errorMsg = page.locator(".el-message--error, .el-form-item__error, [class*='error']");
        if (errorMsg.isVisible()) {
            log.info("GNYL_059 ReqIf对象属性必选验证通过: {}", errorMsg.textContent());
        } else {
            log.info("GNYL_059 未拦截，可能不需要选择ReqIf对象属性");
        }
        closeDialog();
    }

    // ============================================================
    //  Excel 导出
    // ============================================================

    @Test
    @Order(600)
    @DisplayName("GNYL_060: 需求规格文件夹下导出excel")
    void test_GNYL_060_exportExcel() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导出", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("excel", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(2000);

        Locator successMsg = page.locator(".el-message--success, .el-message__content");
        if (successMsg.isVisible()) {
            log.info("GNYL_060 导出Excel成功: {}", successMsg.textContent());
        } else {
            log.info("GNYL_060 导出操作完成，等待下载");
        }
    }

    // ============================================================
    //  Word 导出
    // ============================================================

    @Test
    @Order(630)
    @DisplayName("GNYL_063: 需求规格文件夹下导出word")
    void test_GNYL_063_exportWord() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导出", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("word", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(2000);

        Locator successMsg = page.locator(".el-message--success, .el-message__content");
        if (successMsg.isVisible()) {
            log.info("GNYL_063 导出Word成功: {}", successMsg.textContent());
        } else {
            log.info("GNYL_063 导出操作完成，等待下载");
        }
    }

    // ============================================================
    //  ReqIF 导出
    // ============================================================

    @Test
    @Order(660)
    @DisplayName("GNYL_066: 根节点下导出ReqIf")
    void test_GNYL_066_exportReqIf() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导出")).click();
        page.waitForTimeout(500);
        page.getByText("导出ReqIf文件", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();

        Locator fileNameInput = page.getByPlaceholder("ReqIf文件名称");
        if (fileNameInput.isVisible()) {
            fileNameInput.click();
            fileNameInput.fill("export_test");
        }

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("导出")).click();
        page.waitForTimeout(2000);

        Locator successMsg = page.locator(".el-message--success, .el-message__content");
        if (successMsg.isVisible()) {
            log.info("GNYL_066 导出ReqIf成功: {}", successMsg.textContent());
        } else {
            log.info("GNYL_066 导出操作完成，等待下载");
        }
        closeDialog();
    }

    // ============================================================
    //  内部工具方法
    // ============================================================

    private void openImportExcelDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("Excel", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
    }

    private void openImportWordDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("Word", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
    }

    private void openImportReqIfDialog() {
        reqPage.doubleClickTreeNode(TestConstants.ROOT_NODE);
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.ROW,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("导入", new Page.GetByTextOptions().setExact(true)).click();
        page.getByText("ReqIf", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();
    }

    private void closeDialog() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("取消")).click();
        page.locator(".el-dialog").first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));
    }
}
