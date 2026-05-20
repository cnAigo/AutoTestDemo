package cases;

import actions.ReqApiActions;
import base.BaseTest;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.MouseButton;
import config.TestConfig;
import config.TestConstants;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.RequirementPage;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RequirementTest extends BaseTest {

    private RequirementPage reqPage;
    private ReqApiActions api;
    private static final Logger log = LoggerFactory.getLogger(RequirementTest.class);

    private static final Map<String, String> CTX = new LinkedHashMap<>();

    public static String getCtx(String key) {
        return CTX.getOrDefault(key, "");
    }

    public static String getProjectId() {
        return CTX.getOrDefault("projectId", "");
    }

    @BeforeAll
    public void initPage() {
        reqPage = new RequirementPage(page);
        api = new ReqApiActions(page.request());
    }

    @Test
    @Order(0)
    @DisplayName("登录并导航到需求管理")
    public void step00_LoginAndNavigate() {
        page.navigate(TestConfig.BASE_URL + "/#/login");
        page.getByPlaceholder("请输入用户名").fill(TestConfig.ADMIN_USER);
        page.getByPlaceholder("请输入密码").fill(TestConfig.ADMIN_PWD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("登 录")).click();
        page.waitForTimeout(2000);

        page.navigate(TestConfig.BASE_URL + "/#/RequirementManagement");
        page.waitForTimeout(2000);

        page.locator(".el-dropdown .el-tag__content").first().click();
        page.waitForTimeout(500);
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName(TestConstants.PROJECT_NAME)).click();
        page.waitForTimeout(2000);

        CTX.put("projectId", TestConstants.PROJECT_ID);

        assertThat(page.getByText(TestConstants.ROOT_NODE).first()).isVisible();

        context.storageState(
                new BrowserContext.StorageStateOptions()
                        .setPath(Paths.get(TestConfig.AUTH_STATE_PATH))
        );
        log.info("登录成功, 项目: {}, projectId: {}", TestConstants.PROJECT_NAME, TestConstants.PROJECT_ID);
    }

    @Test
    @Order(10)
    @DisplayName("GNYL_012 [UI] 根节点右键新建文件夹")
    public void test_GNYL_012_CreateFolder_UI() {
        reqPage.rightClickTreeNode(TestConstants.ROOT_NODE);
        reqPage.clickContextMenu("新建");

        String[] details = reqPage.createFolderAndGetDetails();
        CTX.put("parentId", details[1]);

        reqPage.ensureNodeExpanded(TestConstants.ROOT_NODE);
        reqPage.renameFolder(details[0], TestConstants.PARENT_FOLDER);
        log.info("GNYL_012 成功, 父文件夹ID: {}", CTX.get("parentId"));
    }

    @Test
    @Order(20)
    @DisplayName("GNYL_013 [UI] 右键新建子文件夹01")
    void test_GNYL_013_CreateChildFolder01_UI() {
        Assumptions.assumeTrue(CTX.containsKey("parentId"), "前置文件夹未创建");

        reqPage.ensureNodeExpanded(TestConstants.PARENT_FOLDER);
        reqPage.rightClickTreeNode(TestConstants.PARENT_FOLDER);
        reqPage.clickContextMenu("新建");

        String originalName = reqPage.createDocumentAndGetName();
        reqPage.waitForTreeNodeVisible(originalName);
        reqPage.renameFolder(originalName, TestConstants.CHILD_FOLDER_1);
        log.info("GNYL_013 子文件夹01 创建成功");
    }

    @Test
    @Order(30)
    @DisplayName("GNYL_014 [API] 创建子文件夹02")
    void test_GNYL_014_CreateChildFolder02_API() {
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(parentId != null && !parentId.isEmpty(), "未获取到父节点ID");

        String newId = api.createFolder(getProjectId(), parentId);
        CTX.put("targetFolderId", newId);

        Assertions.assertFalse(newId.isEmpty(), "创建文件夹未返回ID");
        log.info("GNYL_014 子文件夹02 ID: {}", newId);
    }

    @Test
    @Order(40)
    @DisplayName("GNYL_015 [UI] 顶部菜单新建子文件夹03")
    void test_GNYL_015_MenuCreateFolder_UI() {
        reqPage.doubleClickTreeNode(TestConstants.PARENT_FOLDER);
        String originalName = reqPage.clickNewFolderDropdownAndGetName();

        reqPage.ensureNodeExpanded(TestConstants.PARENT_FOLDER);
        reqPage.renameFolder(originalName, TestConstants.CHILD_FOLDER_3);
        log.info("GNYL_015 子文件夹03 创建成功");
    }

    @Test
    @Order(50)
    @DisplayName("GNYL_017 [API] 创建需求规格文档")
    void test_GNYL_017_CreateDocument_API() {
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(parentId != null && !parentId.isEmpty(), "未获取到父节点ID");

        String docId1 = api.createDocument(getProjectId(), parentId);
        Assertions.assertFalse(docId1.isEmpty(), "未能获取到文档1的ID");
        api.renameDocument(getProjectId(), docId1, parentId, TestConstants.REQ_NAME1);
        CTX.put("reqId1", docId1);
        log.info("GNYL_017 文档1 ID: {}, 名称: {}", docId1, TestConstants.REQ_NAME1);

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        String docId2 = api.createDocument(getProjectId(), parentId);
        Assertions.assertFalse(docId2.isEmpty(), "未能获取到文档2的ID");
        api.renameDocument(getProjectId(), docId2, parentId, TestConstants.REQ_NAME2);
        CTX.put("reqId2", docId2);
        log.info("GNYL_017 文档2 ID: {}, 名称: {}", docId2, TestConstants.REQ_NAME2);
    }

    @Test
    @Order(60)
    @DisplayName("GNYL_018 [API] 正常重命名成功")
    void test_GNYL_018_RenameSuccess_API() {
        String targetId = CTX.get("targetFolderId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(targetId != null && !targetId.isEmpty(), "没有拿到目标文件夹ID");

        String resp = api.renameFolder(getProjectId(), targetId, parentId, TestConstants.CHILD_FOLDER_2);

        Assertions.assertTrue(resp.contains("200"), "重命名失败: " + resp);
        log.info("GNYL_018 重命名成功");
    }

    @Test
    @Order(70)
    @DisplayName("GNYL_019 [UI] 手动重命名文件夹")
    void test_GNYL_019_Rename_UI() {
        log.info("GNYL_019 待补充UI操作");
    }

    @Test
    @Order(80)
    @DisplayName("GNYL_020 [API] 同名冲突拦截")
    void test_GNYL_020_RenameDuplicate_API() {
        String targetId = CTX.get("targetFolderId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(targetId != null, "没有拿到目标文件夹ID");

        page.waitForTimeout(1500);

        String resp = api.renameFolder(getProjectId(), targetId, parentId, TestConstants.CHILD_FOLDER_1);

        Assertions.assertTrue(
                resp.contains("500") || resp.contains("存在"),
                "同名拦截失败: " + resp
        );
        log.info("GNYL_020 同名冲突拦截通过");
    }

    @Test
    @Order(90)
    @DisplayName("GNYL_021 [UI] 手动修改同名文件夹（验证UI拦截）")
    void test_GNYL_021_RenameSameName_UI() {
        reqPage.ensureNodeExpanded(TestConstants.PARENT_FOLDER);
        reqPage.activateRenameInput(TestConstants.CHILD_FOLDER_3);

        String errorMsg = reqPage.fillRenameAndSave(TestConstants.CHILD_FOLDER_1);

        Assertions.assertTrue(
                errorMsg.contains("已经存在"),
                "期望出现重名提示，实际: " + errorMsg
        );
        log.info("GNYL_021 UI同名冲突拦截通过");
    }

    @Test
    @Order(100)
    @DisplayName("GNYL_022 [API] 重命名为空")
    void test_GNYL_022_RenameEmpty_API() {
        String targetId = CTX.get("targetFolderId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(targetId != null && !targetId.isEmpty(), "没有拿到目标文件夹ID");

        String resp = api.renameFolder(getProjectId(), targetId, parentId, "");

        Assertions.assertTrue(resp.contains("500"), "期望返回500: " + resp);
        Assertions.assertTrue(resp.contains("名称不能为空"), "期望拦截空名称: " + resp);
        log.info("GNYL_022 空名称拦截成功");
    }

    @Test
    @Order(110)
    @DisplayName("GNYL_023 [UI] 展开并编辑子文件夹描述")
    void test_GNYL_023_EditFolderDesc_UI() {
        page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER).setExact(true)
        ).dblclick();

        Locator folderRow = page.getByRole(AriaRole.ROW,
                new Page.GetByRoleOptions().setName(TestConstants.CHILD_FOLDER_1));
        folderRow.locator("pre").click();
        folderRow.locator("pre").click();

        Locator editor = page.locator("div[contenteditable='true']").first();
        editor.waitFor();
        editor.fill("测试描述内容");

        page.locator("div > div > div:nth-child(2) > div:nth-child(3)").first().click();
        log.info("GNYL_023 UI描述编辑成功");
    }

    @Test
    @Order(120)
    @DisplayName("GNYL_024 [API] 编辑文件夹描述")
    void test_GNYL_024_EditFolderDesc_API() {
        String targetId = CTX.get("targetFolderId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(targetId != null && !targetId.isEmpty(), "没有拿到目标文件夹ID");

        String resp = api.editDescription(getProjectId(), targetId, parentId, "这是通过API写入的描述");

        Assertions.assertTrue(resp.contains("修改成功"), "描述修改失败: " + resp);
        log.info("GNYL_024 API描述修改成功");
    }

    @Test
    @Order(250)
    @DisplayName("GNYL_025 [API] 删除有子级的文件夹（应拦截）")
    void test_GNYL_025_DeleteHaveChildrenFolder_API() {
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(parentId != null && !parentId.isEmpty(), "未获取到父节点ID");

        String resp = api.deleteFolder(parentId, getProjectId(), "project");

        Assertions.assertTrue(resp.contains("500"), "期望返回500: " + resp);
        Assertions.assertTrue(resp.contains("该需求规格文件夹下有子级，暂时不允许删除"), "拦截提示不匹配: " + resp);
        log.info("GNYL_025 子级删除拦截通过");
    }

    @Test
    @Order(260)
    @DisplayName("GNYL_026 [UI] 删除有子级的文件夹（验证UI拦截）")
    void test_GNYL_026_DeleteHaveChildrenFolder_UI() {
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.PARENT_FOLDER).setExact(true))
                .locator("path").click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));

        page.getByText("删除", new Page.GetByTextOptions().setExact(true)).click();

        Locator errorMsg = page.getByText("该需求规格文件夹下有子级，暂时不允许删除！");
        assertThat(errorMsg).isVisible();

        log.info("GNYL_026 UI删除有子级文件夹拦截通过");
    }

    @Test
    @Order(270)
    @DisplayName("GNYL_027 [API] 删除无子级的文件夹")
    void test_GNYL_027_DeleteNoChildrenFolder() {
        String targetId = CTX.get("targetFolderId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(targetId != null && !targetId.isEmpty(), "没有拿到目标文件夹ID");

        String resp = api.deleteFolder(targetId, parentId, "reqSpeFolder");

        Assertions.assertTrue(resp.contains("200"), "业务返回码不是200: " + resp);
        Assertions.assertTrue(resp.contains("删除成功"), "返回信息不匹配: " + resp);
        log.info("GNYL_027 删除成功");
    }

    @Test
    @Order(290)
    @DisplayName("GNYL_029 [API] 恢复已删除的文件夹")
    void test_GNYL_029_RecoverFolder() {
        String targetId = CTX.get("targetFolderId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(targetId != null && !targetId.isEmpty(), "没有拿到目标文件夹ID");

        String resp = api.recoverFolder(targetId, parentId);

        Assertions.assertTrue(resp.contains("200"), "HTTP状态码错误: " + resp);
        Assertions.assertTrue(resp.contains("恢复成功"), "恢复失败: " + resp);
        log.info("GNYL_029 恢复成功");
    }

    @Test
    @Order(330)
    @DisplayName("GNYL_033 [API] 根节点刷新")
    void test_GNYL_033_RefreshRootNode_API() {
        String resp = api.getTree(TestConstants.PROJECT_ID, TestConstants.PROJECT_ID);

        Assertions.assertTrue(resp.contains("200"), "业务状态码不是200: " + resp);
        Assertions.assertTrue(resp.contains("操作成功"), "返回信息不匹配: " + resp);
        log.info("GNYL_033 刷新通过");
    }
}
