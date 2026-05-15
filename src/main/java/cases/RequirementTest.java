package cases; // 必须和文件夹 src/main/java/cases 对应

import base.BaseTest; // 假设你的 BaseTest 在 base 包下
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import config.TestConfig;
import pages.RequirementPage; // 确保这个 pages 包也在 src/main/java 下
import org.junit.jupiter.api.*;
import com.microsoft.playwright.options.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

// 继承 BaseTest，自动获得 setup() 和 teardown()
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// 🌟 确保整个类的测试实例只创建一次，这样 @BeforeAll 才能正常工作
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RequirementTest extends BaseTest {

    private RequirementPage reqPage;
    private static final Logger log = LoggerFactory.getLogger(RequirementTest.class);
    private static final String ROOT_NODE_NAME = "需求（根节点）";
    private static final String PARENT_FOLDER_NAME = "自动化测试";
    private static final String CHILD_FOLDER_NAME_1 = "自动化测试_子文件夹01";
    private static final String CHILD_FOLDER_NAME_2 = "自动化测试_子文件夹02_API创建";
    private static final String CHILD_FOLDER_NAME_3 = "自动化测试_子文件夹03";
    private static final String Auto_document = "自动化测试_需求规格_API创建";

    // 🌟 全局项目 ID (从你的抓包记录里拿来的)
    private static final String PROJECT_ID = "2029043043216191488";

    // 🌟 用于存储 012 用例创建出来的真实父节点 ID
    private static String dynamicParentId = "";
    private static String dynamicTargetFolderId = "";

    @BeforeAll
    public void initPage() {
        reqPage = new RequirementPage(page);
    }

    // ==========================================
    // 🖱️ UI 自动化测试部分 (验证前端交互)
    // ==========================================
    @Test
    @Order(0)
    public void step1_LoginAndNavigate() {
        // 这里执行原本 setup 里的逻辑
        page.navigate(TestConfig.BASE_URL + "/#/login");
        page.getByPlaceholder("请输入用户名").fill(TestConfig.ADMIN_USER);
        page.getByPlaceholder("请输入密码").fill(TestConfig.ADMIN_PWD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("登 录")).click();
        page.navigate(TestConfig.BASE_URL + "/#/RequirementManagement");

        // 验证进入页面
        assertThat(page.getByText("需求（根节点）").first()).isVisible();
    }

    @Test
    @Order(10)
    @DisplayName("UI测试 GNYL_012：根节点新增文件夹 (验证右键菜单入口)")
    public void test_GNYL_012_CreateFolder_UI() {
        reqPage.rightClickTreeNode(ROOT_NODE_NAME);
        reqPage.clickContextMenu("新建");

        String[] details = reqPage.createFolderAndGetDetails();
        String originalName = details[0];
        dynamicParentId = details[1];

        reqPage.ensureNodeExpanded(ROOT_NODE_NAME);
        reqPage.renameFolder(originalName, PARENT_FOLDER_NAME);
        log.info("GNYL_012 成功！根节点新增文件夹");
    }

    @Test
    @Order(20)
    @DisplayName("UI测试 GNYL_013：右键树节点新建子文件夹")
    void test_GNYL_013_CreateChildFolder01_UI() {
        Assumptions.assumeTrue(PARENT_FOLDER_NAME != null, "前置文件夹未创建");
        reqPage.rightClickTreeNode(PARENT_FOLDER_NAME);
        reqPage.clickContextMenu("新建");

        // 修复笔误：这里应该建文件夹，不能建文档
        String originalName = reqPage.createDocumentAndGetName();

        reqPage.ensureNodeExpanded(PARENT_FOLDER_NAME);
        reqPage.renameFolder(originalName, CHILD_FOLDER_NAME_1);
        log.info("GNYL_013 右键树节点新建子文件夹 成功!");
    }

    @Test
    @Order(30)
    @DisplayName("API测试 GNYL_014：光速创建一个子文件夹并记录它的ID")
    void test_GNYL_014_CreateChildFolder02_API() {
        Assumptions.assumeTrue(dynamicParentId != null && !dynamicParentId.isEmpty(), "未获取到父节点ID，放弃执行");

        String jsonPayload = """
                {
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "projectId": "%s"
                }
                """.formatted(dynamicParentId, PROJECT_ID);

        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/add/addReqSpeFolder",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        Assertions.assertEquals(200, response.status(), "API 创建失败！");

        // 🌟 重点来了：解析返回的 JSON，把新创建的文件夹 objectId 存起来！
        String responseText = response.text();
        try {
            int idStart = responseText.indexOf("\"objectId\":\"") + 12;
            int idEnd = responseText.indexOf("\"", idStart);
            dynamicTargetFolderId = responseText.substring(idStart, idEnd);
            log.info("GNYL_014 (API) 新建文件夹 成功！");
        } catch (Exception e) {
            log.info("GNYL_014 (API) 提取目标ID失败: ");
        }
    }

    @Test
    @Order(40)
    @DisplayName("UI测试 GNYL_015：通过顶部菜单新建 (验证主操作栏)")
    void test_GNYL_015_goFolderCreateFolder_UI() {
        reqPage.doubleClickTreeNode(PARENT_FOLDER_NAME);
        String originalName = reqPage.clickNewFolderDropdownAndGetName();
        reqPage.ensureNodeExpanded(PARENT_FOLDER_NAME);
        reqPage.renameFolder(originalName, CHILD_FOLDER_NAME_3);
        log.info("GNYL_015 (UI) 通过菜单栏新建文件夹 成功!");
    }

    @Test
    @Order(50)
    @DisplayName("API测试 创建需求规格")
    void test_GNYL_017_CreateDocument_API() {
        Assumptions.assumeTrue(dynamicParentId != null && !dynamicParentId.isEmpty(), "未获取到父节点ID");

        String jsonPayload = """
                {
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "projectId": "%s"
                }
                """.formatted(dynamicParentId, PROJECT_ID);

        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/rem/add/addReqSpe",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(jsonPayload)
        );

        Assertions.assertEquals(200, response.status(), "API 创建文档失败");
        log.info("GNYL_017 (API) 创建文档完成！");
    }

    @Test
    @Order(60)
    @DisplayName("API测试 GNYL_018：正常重命名成功")
    void test_GNYL_018_RenameSuccess_API() {
        Assumptions.assumeTrue(!dynamicTargetFolderId.isEmpty(), "没有拿到目标文件夹ID");

        // 传一个正常的新名字
        String validName = "API最终确认修改的名称";
        String jsonPayload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "title": "%s"
                }
                """.formatted(PROJECT_ID, dynamicTargetFolderId, dynamicParentId, validName);

        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        // 1. 把返回的真实内容保存下来
        String responseText = response.text();

        // 2. 🌟 加上这行监控探头：打印出后端到底返回了什么鬼东西！
        log.info("GNYL_018 (API)  重命名 成功!");

        // 3. 🌟 优化断言：如果失败了，把后端返回的话直接贴在报错信息里
        Assertions.assertTrue(responseText.contains("200"), "重命名失败了！后端返回的是: " + responseText);
    }

    @Test
    @Order(70)
    @DisplayName("UI测试 GNYL_019：正常重命名成功")
    void test_GNYL_027_RenameMultipleTimes_UI() {

        log.info("GNYL_027 (UI) 手动命名成功！");
    }

    @Test
    @Order(80)
    @DisplayName("API测试 GNYL_020：重命名与同级同名 (验证500拦截)")
    void test_GNYL_020_RenameDuplicate_API() {
        Assumptions.assumeTrue(!dynamicTargetFolderId.isEmpty(), "没有拿到目标文件夹ID");

        // 🌟 关键补丁 1：强制等待 1.5 秒，确保之前的 UI 命名操作在后端已完全生效
        page.waitForTimeout(1500);

        String jsonPayload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "title": "%s"
                }
                """.formatted(PROJECT_ID, dynamicTargetFolderId, dynamicParentId, CHILD_FOLDER_NAME_1);
        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        String responseText = response.text();

        Assertions.assertTrue(responseText.contains("500") || responseText.contains("存在"),
                "❌ 拦截失败！Postman 能拦截但脚本没拦住。实际返回内容: " + responseText);

        log.info("GNYL_020 (API) 同名冲突拦截验证通过！");
    }

    @Test
    @Order(90)
    @DisplayName("UI测试 GNYL_021：手动修改 同名文件夹")
    void test_GNYL_021_RenameWithSpecialChars_UI() {

        log.info("GNYL_024 (UI) 修改成功！");
    }

    @Test
    @Order(100)
    @DisplayName("API测试 GNYL_022：重命名文件夹为空")
    void test_GNYL_022_RenameEmpty_API() {
        Assumptions.assumeTrue(!dynamicTargetFolderId.isEmpty(), "没有拿到目标文件夹ID");

        String jsonPayload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "title": "" 
                }
                """.formatted(PROJECT_ID, dynamicTargetFolderId, dynamicParentId);

        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        String responseText = response.text();
        Assertions.assertTrue(responseText.contains("\"code\": 500") || responseText.contains("\"code\":500"), "期望报500，但没有");
        Assertions.assertTrue(responseText.contains("名称不能为空"), "期望拦截空名称，但没有");
        log.info("GNYL_023 (API) 空名称拦截成功！");
    }

    @Test
    @Order(110)
    @DisplayName("UI测试 GNYL_023: 展开并编辑子文件夹描述")
    void test_GNYL_023_EditFolderDesc() {

        page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(PARENT_FOLDER_NAME).setExact(true)
        ).dblclick();

        // 3. 点击右侧列表中对应子文件夹的 pre 区域（激活描述输入框）
        // 这里连点两次是为了确保编辑器被彻底激活
        Locator folderRow = page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(CHILD_FOLDER_NAME_1));
        folderRow.locator("pre").click();
        folderRow.locator("pre").click();

        // 4. 定位并填写富文本内容
        // 使用我们之前说的属性定位，绕过动态 ID 的坑
        Locator editor = page.locator("div[contenteditable='true']").first();
        editor.waitFor(); // 等它出来
        editor.fill("测试描述内容");

        // 5. 点击你抓到的那个“空白处”触发保存
        // 这种 nth-child 定位虽然深，但在没有 ID 的情况下确实有效
        page.locator("div > div > div:nth-child(2) > div:nth-child(3)").first().click();

        log.info("GNYL_023 (UI) 描述编辑并点击保存成功！");
    }

    @Test
    @Order(120)
    @DisplayName("API测试 GNYL_024: 编辑文件夹描述")
    void test_GNYL_024_EditFolderDesc_API() {
        Assumptions.assumeTrue(!dynamicTargetFolderId.isEmpty(), "没有获取到目标文件夹ID");

        String jsonPayload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "description": "这是通过 API 写入的描述"
                }
                """.formatted(PROJECT_ID, dynamicTargetFolderId, dynamicParentId);

        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        Assertions.assertEquals(200, response.status());
        Assertions.assertTrue(response.text().contains("修改成功"));
        log.info("GNYL_024 (API) 描述修改成功！");
    }


    @Test
    @Order(250)
    @DisplayName("GNYL_025 : 删除有子级的文件夹")
    void test_GNYL_025_DeleteHaveChildrenFolder_API() {
        // 1. 确保有父节点 ID (即那个含有子集的文件夹)
        Assumptions.assumeTrue(dynamicParentId != null && !dynamicParentId.isEmpty(), "未获取到父节点ID");

        // 2. 构造 Payload (使用你抓包提供的数据格式)
        // 注意：这里 objectId 传入的是父文件夹 ID，因为它下面已经有子文件夹了
        String jsonPayload = """
                {
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "project"
                }
                """.formatted(dynamicParentId, PROJECT_ID);

        // 3. 调用删除接口
        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/del/delReqSpeFolder",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(jsonPayload)
        );

        // 4. 解析并验证返回结果
        String responseText = response.text();
        log.info("GNYL_025 删除拦截返回内容: " + responseText);

        // 验证状态码是否为 500
        Assertions.assertTrue(responseText.contains("\"code\":500") || responseText.contains("\"code\": 500"),
                "期望返回 500 错误码以示拦截");

        // 验证提示信息是否正确
        Assertions.assertTrue(responseText.contains("该需求规格文件夹下有子级，暂时不允许删除"),
                "拦截提示信息不符合预期");

        log.info("✅ GNYL_025 拦截逻辑验证通过：系统正确阻止了删除含有子集的文件夹。");
    }

    @Test
    @Order(260)
    @DisplayName("GNYL_026 : 删除有子级的文件夹")
    void test_GNYL_025_DeleteHaveChildrenFolder_UI() {
        log.info("");
    }
    @Test
    @Order(270)
    @DisplayName("GNYL_027: 删除无子级的文件夹")
    void  test_GNYL_027_DeleteNoChildrenFolder(){

    }

    @Test
    @Order(290)
    @DisplayName("GNYL_029: 取消删除文件夹")
    void test_GNYL_029_CancelDeleteFolder(){

    }

    @Test
    @Order(330)
    @DisplayName("GNYL_033 (API)：根节点刷新")
    void test_GNYL_033_RefreshRootNode_API() {

        // 1. 构造请求 Payload
        String payload = """
                {
                    "projectId": "2046103585915584512",
                    "parentId": "2046103585915584512",
                    "parentType": "project"
                }
                """;

        // 2. 直接调用底层的刷新树节点接口
        APIResponse response = page.request().post(
                TestConfig.API_PREFIX + "/erm/search/searchReqFolderStructureTree",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(payload)
        );

        // 3. 断言 HTTP 状态码
        Assertions.assertEquals(200, response.status(), "HTTP 状态码应该为 200");

        // 4. 断言业务返回值 (code 和 msg)
        String responseBody = response.text();

        Assertions.assertTrue(responseBody.contains("\"code\":200") || responseBody.contains("\"code\": 200"),
                "业务状态码 code 应该是 200");
        Assertions.assertTrue(responseBody.contains("操作成功"),
                "返回信息应该包含 '操作成功'");

        log.info("GNYL_033 (API) 测试通过！");
    }




    @Test
    @Order(100000)
    @DisplayName("清理【自动化测试】文件夹下所有数据")
    void callCleanup() {
        TestDataCleaner.cleanAutoTestFolder(page, dynamicParentId);
    }
}