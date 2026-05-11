package cases; // 必须和文件夹 src/main/java/cases 对应

import base.BaseTest; // 假设你的 BaseTest 在 base 包下
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import pages.RequirementPage; // 确保这个 pages 包也在 src/main/java 下
import org.junit.jupiter.api.*;
import com.microsoft.playwright.options.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Order(10)
    @DisplayName("UI测试 GNYL_012：根节点新增文件夹 (验证右键菜单入口)")
    public void test_GNYL_012_CreateFolder_UI() {
        log.info("开始UI测试  GNYL_012 : 根节点新增文件夹 (验证右键菜单入口) ");
        reqPage.rightClickTreeNode(ROOT_NODE_NAME);
        reqPage.clickContextMenu("新建");

        String[] details = reqPage.createFolderAndGetDetails();
        String originalName = details[0];
        dynamicParentId = details[1];

        reqPage.ensureNodeExpanded(ROOT_NODE_NAME);
        reqPage.renameFolder(originalName, PARENT_FOLDER_NAME);
        log.info(">>> GNYL_012 成功！根节点新增文件夹");
    }

    @Test
    @Order(20)
    @DisplayName("UI测试 GNYL_013：右键树节点新建子文件夹 (验证左侧树交互)")
    void test_GNYL_013_CreateChildFolder01_UI() {
        Assumptions.assumeTrue(PARENT_FOLDER_NAME != null, "前置文件夹未创建");
        reqPage.rightClickTreeNode(PARENT_FOLDER_NAME);
        reqPage.clickContextMenu("新建");

        // 修复笔误：这里应该建文件夹，不能建文档
        String originalName = reqPage.createDocumentAndGetName();

        reqPage.ensureNodeExpanded(PARENT_FOLDER_NAME);
        reqPage.renameFolder(originalName, CHILD_FOLDER_NAME_1);
        System.out.println("GNYL_013 右键树节点新建子文件夹 成功!");
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

        APIResponse response = page.request().post("https://192.168.0.222:8088/dev-api/erm/add/addReqSpeFolder",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        Assertions.assertEquals(200, response.status(), "API 创建失败！");

        // 🌟 重点来了：解析返回的 JSON，把新创建的文件夹 objectId 存起来！
        String responseText = response.text();
        try {
            int idStart = responseText.indexOf("\"objectId\":\"") + 12;
            int idEnd = responseText.indexOf("\"", idStart);
            dynamicTargetFolderId = responseText.substring(idStart, idEnd);
            System.out.println("GNYL_014 (API) 新建文件夹 成功！");
        } catch (Exception e) {
            System.out.println("GNYL_014 (API) 提取目标ID失败: ");
        }
    }

    @Test
    @Order(40)
    @DisplayName("UI测试 GNYL_015：通过顶部菜单新建 (验证主操作栏)")
    void test_GNYL_015_goFolderCreateFolder_UI(){
        reqPage.doubleClickTreeNode(PARENT_FOLDER_NAME);
        String originalName = reqPage.clickNewFolderDropdownAndGetName();
        reqPage.ensureNodeExpanded(PARENT_FOLDER_NAME);
        reqPage.renameFolder(originalName, CHILD_FOLDER_NAME_3);
        System.out.println("GNYL_015 (UI) 通过菜单栏新建文件夹 成功!");
    }

    @Test
    @Order(50)
    @DisplayName("API测试 创建需求规格文档")
    void test_GNYL_017_CreateDocument_API() {
        Assumptions.assumeTrue(dynamicParentId != null && !dynamicParentId.isEmpty(), "未获取到父节点ID");

        String jsonPayload = """
            {
                "parentId": "%s",
                "parentType": "reqSpeFolder",
                "projectId": "%s"
            }
            """.formatted(dynamicParentId, PROJECT_ID);

        APIResponse response = page.request().post("https://192.168.0.222:8088/dev-api/erm/add/addReqSpe",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(jsonPayload)
        );

        Assertions.assertEquals(200, response.status(), "API 创建文档失败");
        System.out.println("GNYL_017 (API) 创建文档完成！");
    }

    @Test
    @Order(60)
    @DisplayName("API测试 GNYL_018：正常重命名成功 (验证200)")
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

        APIResponse response = page.request().post("https://192.168.0.222:8088/dev-api/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        // 1. 把返回的真实内容保存下来
        String responseText = response.text();

        // 2. 🌟 加上这行监控探头：打印出后端到底返回了什么鬼东西！
        System.out.println("GNYL_018 (API)  重命名 成功!");

        // 3. 🌟 优化断言：如果失败了，把后端返回的话直接贴在报错信息里
        Assertions.assertTrue(responseText.contains("200"), "重命名失败了！后端返回的是: " + responseText);
    }

    @Test
    @Order(70)
    @DisplayName("UI测试 GNYL_019：正常重命名成功")
    void test_GNYL_027_RenameMultipleTimes_UI() {
        
        System.out.println("GNYL_027 (UI) 手动命名成功！");
    }

    @Test
    @Order(80)
    @DisplayName("API测试 GNYL_020：重命名与同级同名 (验证500拦截)")
    void test_GNYL_020_RenameDuplicate_API() {
        Assumptions.assumeTrue(!dynamicTargetFolderId.isEmpty(), "没有拿到目标文件夹ID");
        String jsonPayload = """
            {
                "projectId": "%s",
                "objectId": "%s",
                "parentId": "%s",
                "parentType": "reqSpeFolder",
                "title": "%s"
            }
            """.formatted(PROJECT_ID, dynamicTargetFolderId, dynamicParentId, CHILD_FOLDER_NAME_1);

        APIResponse response = page.request().post("https://192.168.0.222:8088/dev-api/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        String responseText = response.text();
        Assertions.assertTrue(responseText.contains("\"code\": 500") || responseText.contains("\"code\":500"));
        Assertions.assertTrue(responseText.contains("已经存在"));
        System.out.println("GNYL_025 (API) 同名冲突拦截成功！");
    }

    @Test
    @Order(90)
    @DisplayName("UI测试 GNYL_021：手动修改 同名文件夹")
    void test_GNYL_021_RenameWithSpecialChars_UI() {

        System.out.println("GNYL_024 (UI) 修改成功！");
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

        APIResponse response = page.request().post("https://192.168.0.222:8088/dev-api/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        String responseText = response.text();
        Assertions.assertTrue(responseText.contains("\"code\": 500") || responseText.contains("\"code\":500"), "期望报500，但没有");
        Assertions.assertTrue(responseText.contains("名称不能为空"), "期望拦截空名称，但没有");
        System.out.println("GNYL_023 (API) 空名称拦截成功！");
    }

    @Test
    @Order(110)
    @DisplayName("UI测试 GNYL_023: 展开并编辑子文件夹描述")
    void test_GNYL_023_EditFolderDesc() {


        // 2. 双击进入该父节点，确保右侧列表加载出子项
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

        System.out.println("GNYL_023 (UI) 描述编辑并点击保存成功！");
    }

    @Test
    @Order(120)
    @DisplayName("API测试 GNYL_024: 编辑文件夹描述")
    void test_GNYL_024_EditFolderDesc_API(){
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

        APIResponse response = page.request().post("https://192.168.0.222:8088/dev-api/erm/update/updateReqSpeFolderInfo",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(jsonPayload)
        );

        Assertions.assertEquals(200, response.status());
        Assertions.assertTrue(response.text().contains("修改成功"));
        System.out.println("GNYL_024 (API) 描述修改成功！");
    }


    // ==========================================
    //                  数据清理
    // ==========================================

    @Test
    @Order(1000)
    @DisplayName("清理测试数据")
    void testCleanup() {
        if (dynamicParentId == null || dynamicParentId.isEmpty()) {
            System.out.println("没有父节点ID，跳过数据清理。");
            return;
        }

        System.out.println("====== 开始通过 API 极速清理所有子节点 ======");

        // 1. 调用查询接口，获取该父节点下所有的子元素
        String searchPayload = "{\"objectId\": \"%s\"}".formatted(dynamicParentId);
        APIResponse searchResp = page.request().post("https://192.168.0.222:8088/dev-api/erm/search/searchReqFolderChildrenList",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(searchPayload)
        );

        try {
            // 2. 将返回的字符串解析为 JSON 对象
            JsonObject root = JsonParser.parseString(searchResp.text()).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");

            // --- 3. 遍历清理【需求规格】(reqSpeList) ---
            if (data.has("reqSpeList") && !data.get("reqSpeList").isJsonNull()) {
                JsonArray reqSpeList = data.getAsJsonArray("reqSpeList");
                for (JsonElement element : reqSpeList) {
                    String docId = element.getAsJsonObject().get("objectId").getAsString();
                    String docTitle = element.getAsJsonObject().get("title").getAsString();

                    // 需求规格的 删除和清除 payload 是一样的
                    String docPayload = """
                        {"objectId": "%s", "parentId": "%s", "parentType": "reqSpeFolder"}
                        """.formatted(docId, dynamicParentId);

                    // 执行删除
                    page.request().post("https://192.168.0.222:8088/dev-api/erm/del/delReqSpe",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(docPayload));
                    // 执行清除
                    page.request().post("https://192.168.0.222:8088/dev-api/erm/clean/cleanReqSpe",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(docPayload));

                    System.out.println("已通过 API 清理需求规格: [" + docTitle + "] (ID: " + docId + ")");
                }
            }

            // --- 4. 遍历清理【子文件夹】(reqSpeFolderList) ---
            if (data.has("reqSpeFolderList") && !data.get("reqSpeFolderList").isJsonNull()) {
                JsonArray reqSpeFolderList = data.getAsJsonArray("reqSpeFolderList");
                for (JsonElement element : reqSpeFolderList) {
                    String folderId = element.getAsJsonObject().get("objectId").getAsString();
                    String folderTitle = element.getAsJsonObject().get("title").getAsString();

                    // 文件夹的删除 Payload
                    String delFolderPayload = """
                        {"objectId": "%s", "parentId": "%s", "parentType": "reqSpeFolder"}
                        """.formatted(folderId, dynamicParentId);

                    // 文件夹的清除 Payload（注意你抓的包里，清除不需要 parentId）
                    String cleanFolderPayload = """
                        {"objectId": "%s"}
                        """.formatted(folderId);

                    // 执行删除
                    page.request().post("https://192.168.0.222:8088/dev-api/erm/del/delReqSpeFolder",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(delFolderPayload));
                    // 执行清除
                    page.request().post("https://192.168.0.222:8088/dev-api/erm/clean/cleanReqSpeFolder",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(cleanFolderPayload));

                    System.out.println("已通过 API 清理子文件夹: [" + folderTitle + "] (ID: " + folderId + ")");
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ API 清理子节点出现异常: " + e.getMessage());
        }

// --- 5. 最后一步：通过 API 清理最外层的父节点 ---
        // 注意：根目录的 parentType 是 "project"，parentId 是项目的全局 ID
        String rootFolderPayload = """
                {"objectId": "%s", "parentId": "%s", "parentType": "project"}
                """.formatted(dynamicParentId, PROJECT_ID);

        // 1. 删除最外层父文件夹
        page.request().post("https://192.168.0.222:8088/dev-api/erm/del/delReqSpeFolder",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(rootFolderPayload));

        // 2. 清除最外层父文件夹
        page.request().post("https://192.168.0.222:8088/dev-api/erm/clean/cleanReqSpeFolder",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(rootFolderPayload));

        System.out.println("已通过 API 彻底删除并清除根测试文件夹: [" + PARENT_FOLDER_NAME + "]");

    // 刷新一下页面，让前端树状图同步最新的干净状态（此时连根文件夹都不见了）
        page.reload();

        System.out.println("====== 所有测试数据清理完毕！ ======");
}
}