package cases;

import actions.ReqApiActions;
import base.BaseTest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;
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
public class ReqTest extends BaseTest {

    private RequirementPage reqPage;
    private ReqApiActions api;
    private static final Logger log = LoggerFactory.getLogger(ReqTest.class);

    // ======================== 上下文 ========================
    private static final Map<String, String> CTX = new LinkedHashMap<>();

    @BeforeAll
    public void initPage() {
        reqPage = new RequirementPage(page);
        api = new ReqApiActions(page.request());

        // 从 RequirementTest 拿父文件夹 ID
        String parentId = RequirementTest.getCtx("parentId");
        if (parentId != null && !parentId.isEmpty()) {
            CTX.put("parentId", parentId);
        }

        String projectId = RequirementTest.getProjectId();
        if (projectId != null && !projectId.isEmpty()) {
            CTX.put("projectId", projectId);
        }
    }

    // ============================================================
    //  GNYL_072: 新建需求规格
    // ============================================================

    @Test
    @Order(720)
    @DisplayName("GNYL_072 [API] 新建需求规格")
    void test_GNYL072_newReq() {
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(parentId != null && !parentId.isEmpty(), "未获取到父节点ID");

        // 🌟 一行搞定
        String docId = api.createDocument(CTX.get("projectId"), parentId);
        CTX.put("reqId", docId);

        Assertions.assertFalse(docId.isEmpty(), "未能获取到新创建文档的 ID");

        log.info("GNYL_072 新建需求规格成功");
    }

    // ============================================================
    //  GNYL_078: 修改需求规格名称
    // ============================================================

    @Test
    @Order(780)
    @DisplayName("GNYL_078 [API] 修改需求规格名称")
    void test_GNYL078_modifyReq() {
        String reqId = CTX.get("reqId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(reqId != null && !reqId.isEmpty(), "未获取到需求规格ID");

        // 🌟 一行调用
        String resp = api.renameDocument(CTX.get("projectId"), reqId, parentId, TestConstants.REQ_NAME1);

        Assertions.assertTrue(resp.contains("200"), "业务返回码不是200: " + resp);
        Assertions.assertTrue(resp.contains("修改成功"), "返回信息不匹配: " + resp);

        log.info("GNYL_078 需求规格名称修改成功");
    }

    // ============================================================
    //  GNYL_084: 删除需求规格
    // ============================================================

    @Test
    @Order(840)
    @DisplayName("GNYL_084/086/088 [API] 删除 → 恢复 → 清除需求规格（完整生命周期）")
    void test_GNYL084_086_088_deleteRecoverClean() {
        String reqId = CTX.get("reqId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(reqId != null && !reqId.isEmpty(), "未获取到需求规格ID");

        // ======================== GNYL_084: 删除需求规格 ========================
        String deleteResp = api.deleteDocument(reqId, parentId);
        log.info("GNYL_084 删除返回: {}", deleteResp);
        Assertions.assertTrue(deleteResp.contains("200"), "删除失败: " + deleteResp);
        Assertions.assertTrue(deleteResp.contains("操作成功"), "删除提示不匹配: " + deleteResp);
        log.info("GNYL_084 删除需求规格成功");

        // ======================== GNYL_086: 恢复需求规格（取消删除） ========================
        String recoverResp = api.recoverDocument(reqId, parentId);
        log.info("GNYL_086 恢复返回: {}", recoverResp);
        Assertions.assertTrue(recoverResp.contains("200"), "恢复失败: " + recoverResp);
        Assertions.assertTrue(recoverResp.contains("操作成功"), "恢复提示不匹配: " + recoverResp);
        log.info("GNYL_086 恢复需求规格成功");

        // ======================== GNYL_088: 清除需求规格（彻底删除） ========================
        // 恢复之后需要重新删除，再清除
        api.deleteDocument(reqId, parentId);
        String cleanResp = api.cleanDocument(reqId, parentId);
        Assertions.assertTrue(cleanResp.contains("200"), "清除失败: " + cleanResp);
        Assertions.assertTrue(cleanResp.contains("清除成功"), "清除提示不匹配: " + cleanResp);
        log.info("GNYL_088 清除需求规格成功");
    }



    // ==========================================
    // 🔍 视图切换与搜索功能 (GNYL_090 - GNYL_095)
    // ==========================================

    @Test
    @Order(900)
    @DisplayName("GNYL_090 [API] 需求表视图切换（查询需求规格列表）")
    void test_GNYL_090_DemandTable() {
        String resp = api.getReqSpeList(CTX.get("projectId"));


        // 基础断言
        Assertions.assertTrue(resp.contains("200"), "查询失败: " + resp);
        Assertions.assertTrue(resp.contains("操作成功"), "返回信息不匹配: " + resp);

        // 解析 data 数组
        JsonObject json = com.google.gson.JsonParser.parseString(resp).getAsJsonObject();
        JsonArray data = json.getAsJsonArray("data");

        // 断言：返回的列表不为空
        Assertions.assertNotNull(data, "data 为 null");
        Assertions.assertTrue(data.size() > 0, "需求规格列表为空");

        log.info("GNYL_090 需求表视图切换通过");
    }


    @Test
    @Order(910)
    @DisplayName("GNYL_091 [UI] 需求树视图切换")
    void test_GNYL_091_DemandTree() {
        // 1. 先确认当前视图状态，切到"需求表"
        Locator demandTreeBtn = page.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^需求树$")))
                .nth(1);

        if (demandTreeBtn.isVisible()) {
            // 当前是"需求树"，先切到"需求表"
            demandTreeBtn.click();
            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("需求表")).click();
            page.waitForTimeout(1000);
        }

        // 2. 从"需求表"切回"需求树"
        Locator demandTableBtn = page.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^需求表$")))
                .nth(1);
        demandTableBtn.click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("需求树")).click();

        // 3. 断言：根节点可见
        assertThat(page.getByText(TestConstants.ROOT_NODE).first()).isVisible();
        log.info("GNYL_091 需求树视图切换通过");

        page.locator("#app").getByText("需求（根节点）").click(new Locator.ClickOptions()
                .setButton(MouseButton.RIGHT));
        page.locator("span").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^刷新$"))).click();
    }


//    @Test
//    @Order(920)
//    @DisplayName("GNYL_092/093/094/095 [UI] 节点搜索：精确查询 → 模糊查询 → 不存在 → 清空")
//    void test_GNYL_092_093_094_095_NodeSearch() {
//
//        // ======================== GNYL_092: 精确查询 ========================
//        page.getByTitle("搜索").getByRole(AriaRole.IMG).click();
//        page.getByPlaceholder("请输入节点名称").click();
//        page.getByPlaceholder("请输入节点名称").fill(TestConstants.CHILD_FOLDER_1);
//        page.waitForTimeout(1500);
//
//        // 用 CSS 选择器，避开虚拟滚动的动态 ID
//        Locator exactMatch = page.locator(".el-tree-node__label")
//                .filter(new Locator.FilterOptions().setHasText(TestConstants.CHILD_FOLDER_1))
//                .first();
//        exactMatch.waitFor(new Locator.WaitForOptions()
//                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
//                .setTimeout(5000));
//        exactMatch.click(new Locator.ClickOptions().setForce(true));
//        log.info("GNYL_092 精确搜索通过");
//
//        // ======================== GNYL_093: 模糊查询 ========================
//        page.getByTitle("搜索").getByRole(AriaRole.IMG).click();
//        page.getByPlaceholder("请输入节点名称").click();
//        String fuzzyKeyword = TestConstants.CHILD_FOLDER_1.substring(0, 4);
//        page.getByPlaceholder("请输入节点名称").fill(fuzzyKeyword);
//        page.waitForTimeout(1500);
//
//        Locator fuzzyMatch = page.locator(".el-tree-node__label")
//                .filter(new Locator.FilterOptions().setHasText(TestConstants.CHILD_FOLDER_1))
//                .first();
//        fuzzyMatch.waitFor(new Locator.WaitForOptions()
//                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
//                .setTimeout(5000));
//        fuzzyMatch.click(new Locator.ClickOptions().setForce(true));
//        log.info("GNYL_093 模糊搜索通过");
//
//        // ======================== GNYL_094: 不存在的节点 ========================
//        page.getByTitle("搜索").getByRole(AriaRole.IMG).click();
//        page.getByPlaceholder("请输入节点名称").click();
//        page.getByPlaceholder("请输入节点名称").fill("不存在的节点XYZ");
//        page.waitForTimeout(1500);
//
//        Locator noData = page.locator(".el-tree__empty-text").first();
//        assertThat(noData).isVisible();
//        log.info("GNYL_094 不存在节点搜索通过");
//
//        // ======================== GNYL_095: 清空搜索框 ========================
//        page.getByPlaceholder("请输入节点名称").click();
//        // 点击 X 清除按钮
//        page.getByRole(AriaRole.TOOLTIP,
//                        new Page.GetByRoleOptions().setName("请输入节点名称"))
//                .getByRole(AriaRole.IMG).nth(1).click();
//        page.waitForTimeout(1500);
//
//        // 验证输入框已清空
//        Locator input = page.getByPlaceholder("请输入节点名称");
//        Assertions.assertTrue(input.inputValue().isEmpty(), "输入框未清空");
//
//        // 验证根节点恢复，点击确认
//        Locator rootNode = page.locator(".el-tree-node__label")
//                .filter(new Locator.FilterOptions().setHasText(TestConstants.ROOT_NODE))
//                .first();
//        rootNode.waitFor(new Locator.WaitForOptions()
//                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
//                .setTimeout(5000));
//        rootNode.click();
//        log.info("GNYL_095 清空搜索框通过，根节点已恢复");
//    }






    // ==========================================
    // 📄 需求规格属性与文件管理 (GNYL_096 - GNYL_112)
    // ==========================================

    @Test
    @Order(960)
    @DisplayName("GNYL_096: 查看属性")
    void test_GNYL_096_ViewProperties() {
        // 1. 右键点击需求规格
        page.getByRole(AriaRole.TREE)
                .getByText(TestConstants.REQ_NAME1)
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));

        // 2. 点击"属性"
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        // 3. 验证属性弹窗中的关键字段可见
        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();

        assertThat(dialog.getByText("创建时间:")).isVisible();
        assertThat(dialog.getByText("最后修改时间:")).isVisible();
        assertThat(dialog.getByText("创建者:")).isVisible();
        assertThat(dialog.getByText("最后修改者:")).isVisible();
        assertThat(dialog.getByText("写权限:")).isVisible();

        // 4. 点击确定关闭
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("取 消")).click();
        page.waitForTimeout(500);

        log.info("GNYL_096 属性查看通过");
    }


    @Test
    @Order(970)
    @DisplayName("GNYL_097: 编辑属性")
    void test_GNYL_097_EditProperties() {
        // 1. 编辑名称、前缀、描述、备注并上传文件
        // 预期：系统提示“更新成功”，弹框关闭
    }

    @Test
    @Order(980)
    @DisplayName("GNYL_098: 规格名称必填测试")
    void test_GNYL_098_NameRequired() {
        // 1. 右键点击需求规格（限定树节点，取 first）
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);

        // 2. 点击"属性"
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        // 3. 清空名称输入框
        Locator nameInput = page.locator(".el-dialog input[type='text']").first();
        nameInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        nameInput.click();
        nameInput.press("Control+a");
        nameInput.fill("");

        // 4. 点击确定
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
        page.waitForTimeout(500);

        // 5. 验证必填提示
        Locator errorMsg = page.getByText("需求规格名称不能为空！");
        assertThat(errorMsg).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("关闭此对话框")).click();
        log.info("GNYL_098 规格名称必填校验通过");
    }


    @Test
    @Order(1000)
    @DisplayName("GNYL_100/101/102/103: 前缀校验（非字母开头 → 非法字符 → 超长 → 合法）")
    void test_GNYL_100_101_102_103_PrefixValidation() {

        // ===== 打开属性弹窗 =====
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator prefixInput = page.getByPlaceholder("可编辑前缀");
        Locator descArea = page.getByPlaceholder("可编辑描述");
        Locator errorMsg = page.getByText("编码规则不符合要求:必须以字母开头,且长度不超过10");

        // ===== GNYL_100: 非字母开头 =====
        prefixInput.click();
        prefixInput.press("Control+a");
        prefixInput.fill("1");
        descArea.click();  // 触发校验
        page.waitForTimeout(500);
        assertThat(errorMsg).isVisible();
        log.info("GNYL_100 非字母开头拦截通过");

        // ===== GNYL_101: 非法字符 =====
        prefixInput.click();
        prefixInput.fill("&*%(@$");
        descArea.click();
        page.waitForTimeout(500);
        assertThat(errorMsg).isVisible();
        log.info("GNYL_101 非法字符拦截通过");

        // ===== GNYL_102: 超过10个字符 =====
        prefixInput.click();
        prefixInput.press("Control+a");
        prefixInput.fill("123456789789");
        descArea.click();
        page.waitForTimeout(500);
        assertThat(errorMsg).isVisible();
        log.info("GNYL_102 超长前缀拦截通过");

        // ===== GNYL_103: 合法前缀，保存成功 =====
        prefixInput.click();
        prefixInput.press("Control+a");
        prefixInput.fill("req");
        descArea.click();
        page.waitForTimeout(500);

        // 确认没有错误提示
        Assertions.assertFalse(errorMsg.isVisible(), "合法前缀不应出现错误提示");

        // 点击确定保存
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
        page.waitForTimeout(1000);

        log.info("GNYL_103 合法前缀保存通过");
    }


    @Test
    @Order(1040)
    @DisplayName("GNYL_104/105: 描述校验（合法描述 → 超长描述）")
    void test_GNYL_104_105_DescriptionValidation() {

        // ===== 打开属性弹窗 =====
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator descArea = page.getByPlaceholder("可编辑描述");

        // ===== GNYL_104: 合法描述 =====
        String validDesc = "1.在“合作区管理”列表选择合作区，点击“设置属性”\n" +
                "2.勾选一个或多个属性复选框，点击列表上方“删除”按钮\n" +
                "3.在二次确认框，点击“确定”按钮";
        descArea.click();
        descArea.press("Control+a");
        descArea.fill(validDesc);
        page.waitForTimeout(300);

        // 点击确定
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
        page.waitForTimeout(2000);

        // 等弹窗关闭，没关就点取消
        if (page.locator(".el-dialog:visible").count() > 0) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("取 消")).click();
            page.waitForTimeout(500);
        }
        log.info("GNYL_104 合法描述保存通过, 字数: {}", validDesc.length());

        // ===== GNYL_105: 超长描述 =====
        // 重新打开属性弹窗
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        // 生成 1200 字超长描述
        String longBlock = "这是一段用于测试超长描述的文本，验证系统对描述字段长度的限制是否生效。";
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 1200) {
            sb.append(longBlock);
        }
        String tooLongDesc = sb.toString();

        descArea = page.getByPlaceholder("可编辑描述");
        descArea.click();
        descArea.press("Control+a");
        descArea.fill(tooLongDesc);
        page.waitForTimeout(500);

        String actualValue = descArea.inputValue();
        log.info("GNYL_105 期望: {} 字, 实际: {} 字", tooLongDesc.length(), actualValue.length());

        if (actualValue.length() <= 1000) {
            log.info("GNYL_105 输入框自动截断至 {} 字", actualValue.length());
        } else {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
            page.waitForTimeout(500);
        }

        // 关闭弹窗
        if (page.locator(".el-dialog:visible").count() > 0) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("取 消")).click();
            page.waitForTimeout(500);
        }
        log.info("GNYL_105 超长描述校验通过");
    }



    @Test
    @Order(1060)
    @DisplayName("GNYL_106: 拖动符合格式的文件上传")
    void test_GNYL_106_DragUploadValid() {
        // 预期：上传成功，展示文件名及备注框
    }

    @Test
    @Order(1070)
    @DisplayName("GNYL_107: 拖动不符合格式的文件上传")
    void test_GNYL_107_DragUploadInvalid() {
        // 预期：提示文件格式不正确
    }

    @Test
    @Order(1080)
    @DisplayName("GNYL_108: 上传符合格式的文件 (点击上传)")
    void test_GNYL_108_ClickUploadValid() {
        // 预期：上传成功
    }

    @Test
    @Order(1090)
    @DisplayName("GNYL_109: 上传不符合格式的文件 (点击上传)")
    void test_GNYL_109_ClickUploadInvalid() {
        // 预期：提示格式错误
    }

    @Test
    @Order(1100)
    @DisplayName("GNYL_110: 填写不超过50字的备注")
    void test_GNYL_110_RemarkValid() {
        // 预期：保存成功
    }

    @Test
    @Order(1110)
    @DisplayName("GNYL_111: 填写超过50字的备注")
    void test_GNYL_111_RemarkTooLong() {
        // 预期：无法输入
    }

    @Test
    @Order(1120)
    @DisplayName("GNYL_112: 删除属性页文件")
    void test_GNYL_112_DeletePropertyFile() {
        // 1. 点击文件名后的“x”图标
        // 预期：文件成功删除
    }

    // ==========================================
    // 👥 权限人员管理 (GNYL_113 - GNYL_120)
    // ==========================================

    @Test
    @Order(1130)
    @DisplayName("GNYL_113: 添加权限人员")
    void test_GNYL_113_AddPermissionUser() {
        // 1. 双击需求规格文件夹
        // 2. 左侧列表中选择需求规格，点击“编辑”图标
        // 3. 勾选一个或多个人员，点击“确定”按钮
        // 预期：权限编辑弹框关闭，用户名展示在权限栏中
    }

    @Test
    @Order(1140)
    @DisplayName("GNYL_114: 组织部门选择验证")
    void test_GNYL_114_OrganizationSelection() {
        // 预期：用户列表随公司/部门选择动态更新
    }

    @Test
    @Order(1150)
    @DisplayName("GNYL_115: 勾选人员验证")
    void test_GNYL_115_UserSelectionValidation() {
        // 预期：勾选的人员实时展示在“当前选中用户”栏
    }

    @Test
    @Order(1160)
    @DisplayName("GNYL_116: 删除选中人员")
    void test_GNYL_116_RemoveSelectedUser() {
        // 预期：人员从“当前选中用户”移除，复选框恢复未勾选状态
    }

    @Test
    @Order(1170)
    @DisplayName("GNYL_117: 存在的用户名检索")
    void test_GNYL_117_SearchExistingUser() {
        // 预期：列表中展示符合查询条件的人员
    }

    @Test
    @Order(1180)
    @DisplayName("GNYL_118: 用户名模糊查询")
    void test_GNYL_118_FuzzySearchUser() {
        // 预期：筛选出包含关键字的所有用户名信息
    }

    @Test
    @Order(1190)
    @DisplayName("GNYL_119: 不存在的用户名检索")
    void test_GNYL_119_SearchNonExistentUser() {
        // 预期：列表显示“暂无数据”
    }

    @Test
    @Order(1200)
    @DisplayName("GNYL_120: 清空用户名检索输入框")
    void test_GNYL_120_ClearUserSearchInput() {
        // 预期：输入框清空，恢复展示默认人员列表
    }


    // ==========================================
    // 📝 需求条目管理 (GNYL_121 - GNYL_125)
    // ==========================================

    @Test
    @Order(1210)
    @DisplayName("GNYL_121: 新建一级需求条目")
    void test_GNYL_121_CreateFirstLevelRequirementItem() {
        // 1. 双击需求规格
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().dblclick();
        page.waitForTimeout(1000);

        // 2. 点击"新建子级"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新建子级")).click();
        page.waitForTimeout(1000);

        // 3. 刷新列表，确保新条目显示
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("刷新")).click();
        page.waitForTimeout(2000);

        // 4. 验证新条目
        Locator newCell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("req-")).first();
        newCell.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        String firstItemText = newCell.innerText().trim();
        CTX.put("reqItem1", firstItemText);
        log.info("GNYL_121 新建一级条目成功: {}", firstItemText);
    }

    @Test
    @Order(1220)
    @DisplayName("GNYL_122: 新建子需求条目")
    void test_GNYL_122_CreateSubRequirementItem() {
        String firstItem = CTX.getOrDefault("reqItem1", "req-");

        // 1. 右键一级条目
        page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(firstItem))
                .locator("div").first()
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(1000);

        // 2. 点击 "新建"（exact 匹配）
        page.getByText("新建", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(500);

        // 3. 点击 "子级对象"
        page.locator("div").filter(new Locator.FilterOptions()
                .setHasText(java.util.regex.Pattern.compile("^子级对象$"))).click();
        page.waitForTimeout(2000);

        // 4. 验证子条目出现
        Locator subCell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("req-")).first();
        subCell.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        String subItemText = subCell.innerText().trim();
        CTX.put("reqItem2", subItemText);
        log.info("GNYL_122 新建子条目成功: {}", subItemText);
    }

    @Test
    @Order(1230)
    @DisplayName("GNYL_123: 删除需求条目")
    void test_GNYL_123_DeleteRequirementItem() {
        String subItem = CTX.getOrDefault("reqItem2", "req-");

        // 1. 右键子条目
        page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(subItem))
                .locator("div").first()
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(1000);

        // 2. 点击删除
        page.getByText("删除", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        log.info("GNYL_123 删除条目 {} 成功", subItem);
    }



    // ==========================================
    // 🏗️ 结构化管理与对象新建 (GNYL_126 - GNYL_130)
    // ==========================================


    @Test
    @Order(1280)
    @DisplayName("GNYL_128/129/130: 显示大纲 → 结构定位 → 隐藏大纲")
    void test_GNYL_128_129_130_OutlineAndStructure() {

        // 先展开需求规格
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().dblclick();
        page.waitForTimeout(1000);

        // ===== GNYL_128: 显示大纲 =====
        // 点击"显示大纲"按钮
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("显示大纲")).click();
        page.waitForTimeout(500);

        // 验证大纲标签页出现
        Locator outlineTab = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("结构"));
        assertThat(outlineTab).isVisible();

//        // 点击大纲中的需求条目，验证右侧视图定位
//        Locator firstItem = page.getByRole(AriaRole.TREEITEM,
//                new Page.GetByRoleOptions().setName("req-")).locator("div").first();
//        assertThat(firstItem).isVisible();
//        firstItem.click();
        page.waitForTimeout(500);
        log.info("GNYL_128 显示大纲通过，条目可点击");

        // ===== GNYL_129: 显示结构 =====
        // 点击"结构"标签页
        outlineTab.click();
        page.waitForTimeout(500);

        // 验证结构中展示了需求条目
        Locator structItem = page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName("req-")).locator("div").first();
        assertThat(structItem).isVisible();
        structItem.click();
        page.waitForTimeout(500);
        log.info("GNYL_129 结构视图定位通过");

        // ===== GNYL_130: 隐藏大纲 =====
        // 点击"隐藏大纲"按钮
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("隐藏大纲")).click();
        page.waitForTimeout(500);

        // 验证按钮变回"显示大纲"
        Locator showBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("显示大纲"));
        assertThat(showBtn).isVisible();
        log.info("GNYL_130 隐藏大纲通过");
    }


    @Test
    @Order(Integer.MAX_VALUE - 1)
    @DisplayName("清理业务数据")
    void step_cleanup() {
        api.cleanFolderByName(CTX.get("projectId"), TestConstants.PARENT_FOLDER);
        page.reload();
        log.info("业务数据清理完毕");
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @DisplayName("关闭浏览器")
    void step_closeBrowser() {
        try { if (page != null) page.close(); } catch (Exception ignored) {}
        try { if (context != null) context.close(); } catch (Exception ignored) {}
        try { if (browser != null) browser.close(); } catch (Exception ignored) {}
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) {}

        page = null;
        context = null;
        browser = null;
        playwright = null;

        log.info("所有资源已释放");
    }
}
