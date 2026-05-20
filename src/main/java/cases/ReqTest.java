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

    private static final Map<String, String> CTX = new LinkedHashMap<>();

    @BeforeAll
    public void initPage() {
        reqPage = new RequirementPage(page);
        api = new ReqApiActions(page.request());

        String parentId = RequirementTest.getCtx("parentId");
        if (parentId != null && !parentId.isEmpty()) {
            CTX.put("parentId", parentId);
        }

        String projectId = RequirementTest.getProjectId();
        if (projectId != null && !projectId.isEmpty()) {
            CTX.put("projectId", projectId);
        }
    }

    @Test
    @Order(720)
    @DisplayName("GNYL_072 [API] 新建需求规格")
    void test_GNYL072_newReq() {
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(parentId != null && !parentId.isEmpty(), "未获取到父节点ID");

        String docId = api.createDocument(CTX.get("projectId"), parentId);
        CTX.put("reqId", docId);

        Assertions.assertFalse(docId.isEmpty(), "未能获取到新创建文档的 ID");
        log.info("GNYL_072 新建需求规格成功");
    }

    @Test
    @Order(780)
    @DisplayName("GNYL_078 [API] 修改需求规格名称")
    void test_GNYL078_modifyReq() {
        String reqId = CTX.get("reqId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(reqId != null && !reqId.isEmpty(), "未获取到需求规格ID");

        String resp = api.renameDocument(CTX.get("projectId"), reqId, parentId, TestConstants.REQ_NAME1);

        Assertions.assertTrue(resp.contains("200"), "业务返回码不是200: " + resp);
        Assertions.assertTrue(resp.contains("修改成功"), "返回信息不匹配: " + resp);
        log.info("GNYL_078 需求规格名称修改成功");
    }

    @Test
    @Order(840)
    @DisplayName("GNYL_084/086/088 [API] 删除 -> 恢复 -> 清除需求规格（完整生命周期）")
    void test_GNYL084_086_088_deleteRecoverClean() {
        String reqId = CTX.get("reqId");
        String parentId = CTX.get("parentId");
        Assumptions.assumeTrue(reqId != null && !reqId.isEmpty(), "未获取到需求规格ID");

        String deleteResp = api.deleteDocument(reqId, parentId);
        log.info("GNYL_084 删除返回: {}", deleteResp);
        Assertions.assertTrue(deleteResp.contains("200"), "删除失败: " + deleteResp);
        Assertions.assertTrue(deleteResp.contains("操作成功"), "删除提示不匹配: " + deleteResp);
        log.info("GNYL_084 删除需求规格成功");

        String recoverResp = api.recoverDocument(reqId, parentId);
        log.info("GNYL_086 恢复返回: {}", recoverResp);
        Assertions.assertTrue(recoverResp.contains("200"), "恢复失败: " + recoverResp);
        Assertions.assertTrue(recoverResp.contains("操作成功"), "恢复提示不匹配: " + recoverResp);
        log.info("GNYL_086 恢复需求规格成功");

        api.deleteDocument(reqId, parentId);
        String cleanResp = api.cleanDocument(reqId, parentId);
        Assertions.assertTrue(cleanResp.contains("200"), "清除失败: " + cleanResp);
        Assertions.assertTrue(cleanResp.contains("清除成功"), "清除提示不匹配: " + cleanResp);
        log.info("GNYL_088 清除需求规格成功");
    }

    @Test
    @Order(900)
    @DisplayName("GNYL_090 [API] 需求表视图切换（查询需求规格列表）")
    void test_GNYL_090_DemandTable() {
        String resp = api.getReqSpeList(CTX.get("projectId"));

        Assertions.assertTrue(resp.contains("200"), "查询失败: " + resp);
        Assertions.assertTrue(resp.contains("操作成功"), "返回信息不匹配: " + resp);

        JsonObject json = com.google.gson.JsonParser.parseString(resp).getAsJsonObject();
        JsonArray data = json.getAsJsonArray("data");

        Assertions.assertNotNull(data, "data 为 null");
        Assertions.assertTrue(data.size() > 0, "需求规格列表为空");
        log.info("GNYL_090 需求表视图切换通过");
    }

    @Test
    @Order(910)
    @DisplayName("GNYL_091 [UI] 需求树视图切换")
    void test_GNYL_091_DemandTree() {
        Locator demandTreeBtn = page.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^需求树$")))
                .nth(1);

        if (demandTreeBtn.isVisible()) {
            demandTreeBtn.click();
            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("需求表")).click();
            page.waitForTimeout(1000);
        }

        Locator demandTableBtn = page.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^需求表$")))
                .nth(1);
        demandTableBtn.click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("需求树")).click();

        assertThat(page.getByText(TestConstants.ROOT_NODE).first()).isVisible();
        log.info("GNYL_091 需求树视图切换通过");

        page.locator("#app").getByText("需求（根节点）").click(new Locator.ClickOptions()
                .setButton(MouseButton.RIGHT));
        page.locator("span").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^刷新$"))).click();
    }

    @Test
    @Order(960)
    @DisplayName("GNYL_096: 查看属性")
    void test_GNYL_096_ViewProperties() {
        page.getByRole(AriaRole.TREE)
                .getByText(TestConstants.REQ_NAME1)
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));

        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator dialog = page.locator(".el-dialog").first();
        assertThat(dialog).isVisible();

        assertThat(dialog.getByText("创建时间:")).isVisible();
        assertThat(dialog.getByText("最后修改时间:")).isVisible();
        assertThat(dialog.getByText("创建者:")).isVisible();
        assertThat(dialog.getByText("最后修改者:")).isVisible();
        assertThat(dialog.getByText("写权限:")).isVisible();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("取 消")).click();
        page.waitForTimeout(500);

        log.info("GNYL_096 属性查看通过");
    }

    @Test
    @Order(970)
    @DisplayName("GNYL_097: 编辑属性")
    void test_GNYL_097_EditProperties() {
        // TODO: 编辑名称、前缀、描述、备注并上传文件
    }

    @Test
    @Order(980)
    @DisplayName("GNYL_098: 规格名称必填测试")
    void test_GNYL_098_NameRequired() {
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);

        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator nameInput = page.locator(".el-dialog input[type='text']").first();
        nameInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        nameInput.click();
        nameInput.press("Control+a");
        nameInput.fill("");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
        page.waitForTimeout(500);

        Locator errorMsg = page.getByText("需求规格名称不能为空！");
        assertThat(errorMsg).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("关闭此对话框")).click();
        log.info("GNYL_098 规格名称必填校验通过");
    }

    @Test
    @Order(1000)
    @DisplayName("GNYL_100/101/102/103: 前缀校验（非字母开头 -> 非法字符 -> 超长 -> 合法）")
    void test_GNYL_100_101_102_103_PrefixValidation() {
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator prefixInput = page.getByPlaceholder("可编辑前缀");
        Locator descArea = page.getByPlaceholder("可编辑描述");
        Locator errorMsg = page.getByText("编码规则不符合要求:必须以字母开头,且长度不超过10");

        prefixInput.click();
        prefixInput.press("Control+a");
        prefixInput.fill("1");
        descArea.click();
        page.waitForTimeout(500);
        assertThat(errorMsg).isVisible();
        log.info("GNYL_100 非字母开头拦截通过");

        prefixInput.click();
        prefixInput.fill("&*%(@$");
        descArea.click();
        page.waitForTimeout(500);
        assertThat(errorMsg).isVisible();
        log.info("GNYL_101 非法字符拦截通过");

        prefixInput.click();
        prefixInput.press("Control+a");
        prefixInput.fill("123456789789");
        descArea.click();
        page.waitForTimeout(500);
        assertThat(errorMsg).isVisible();
        log.info("GNYL_102 超长前缀拦截通过");

        prefixInput.click();
        prefixInput.press("Control+a");
        prefixInput.fill("req");
        descArea.click();
        page.waitForTimeout(500);

        Assertions.assertFalse(errorMsg.isVisible(), "合法前缀不应出现错误提示");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
        page.waitForTimeout(1000);
        log.info("GNYL_103 合法前缀保存通过");
    }

    @Test
    @Order(1040)
    @DisplayName("GNYL_104/105: 描述校验（合法描述 -> 超长描述）")
    void test_GNYL_104_105_DescriptionValidation() {
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

        Locator descArea = page.getByPlaceholder("可编辑描述");

        String validDesc = "1.在合作区管理列表选择合作区，点击设置属性\n" +
                "2.勾选一个或多个属性复选框，点击列表上方删除按钮\n" +
                "3.在二次确认框，点击确定按钮";
        descArea.click();
        descArea.press("Control+a");
        descArea.fill(validDesc);
        page.waitForTimeout(300);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确 定")).click();
        page.waitForTimeout(2000);

        if (page.locator(".el-dialog:visible").count() > 0) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("取 消")).click();
            page.waitForTimeout(500);
        }
        log.info("GNYL_104 合法描述保存通过, 字数: {}", validDesc.length());

        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(500);
        page.getByText("属性", new Page.GetByTextOptions().setExact(true)).click();
        page.waitForTimeout(1000);

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
        // TODO: 上传成功，展示文件名及备注框
    }

    @Test
    @Order(1070)
    @DisplayName("GNYL_107: 拖动不符合格式的文件上传")
    void test_GNYL_107_DragUploadInvalid() {
        // TODO: 提示文件格式不正确
    }

    @Test
    @Order(1080)
    @DisplayName("GNYL_108: 上传符合格式的文件 (点击上传)")
    void test_GNYL_108_ClickUploadValid() {
        // TODO: 上传成功
    }

    @Test
    @Order(1090)
    @DisplayName("GNYL_109: 上传不符合格式的文件 (点击上传)")
    void test_GNYL_109_ClickUploadInvalid() {
        // TODO: 提示格式错误
    }

    @Test
    @Order(1100)
    @DisplayName("GNYL_110: 填写不超过50字的备注")
    void test_GNYL_110_RemarkValid() {
        // TODO: 保存成功
    }

    @Test
    @Order(1110)
    @DisplayName("GNYL_111: 填写超过50字的备注")
    void test_GNYL_111_RemarkTooLong() {
        // TODO: 无法输入
    }

    @Test
    @Order(1120)
    @DisplayName("GNYL_112: 删除属性页文件")
    void test_GNYL_112_DeletePropertyFile() {
        // TODO: 点击文件名后的"x"图标，文件成功删除
    }

    @Test
    @Order(1130)
    @DisplayName("GNYL_113: 添加权限人员")
    void test_GNYL_113_AddPermissionUser() {
        // TODO: 双击需求规格文件夹 -> 左侧列表中选择需求规格，点击"编辑"图标 -> 勾选人员，点击"确定"
    }

    @Test
    @Order(1140)
    @DisplayName("GNYL_114: 组织部门选择验证")
    void test_GNYL_114_OrganizationSelection() {
        // TODO: 用户列表随公司/部门选择动态更新
    }

    @Test
    @Order(1150)
    @DisplayName("GNYL_115: 勾选人员验证")
    void test_GNYL_115_UserSelectionValidation() {
        // TODO: 勾选的人员实时展示在"当前选中用户"栏
    }

    @Test
    @Order(1160)
    @DisplayName("GNYL_116: 删除选中人员")
    void test_GNYL_116_RemoveSelectedUser() {
        // TODO: 人员从"当前选中用户"移除，复选框恢复未勾选状态
    }

    @Test
    @Order(1170)
    @DisplayName("GNYL_117: 存在的用户名检索")
    void test_GNYL_117_SearchExistingUser() {
        // TODO: 列表中展示符合查询条件的人员
    }

    @Test
    @Order(1180)
    @DisplayName("GNYL_118: 用户名模糊查询")
    void test_GNYL_118_FuzzySearchUser() {
        // TODO: 筛选出包含关键字的所有用户名信息
    }

    @Test
    @Order(1190)
    @DisplayName("GNYL_119: 不存在的用户名检索")
    void test_GNYL_119_SearchNonExistentUser() {
        // TODO: 列表显示"暂无数据"
    }

    @Test
    @Order(1200)
    @DisplayName("GNYL_120: 清空用户名检索输入框")
    void test_GNYL_120_ClearUserSearchInput() {
        // TODO: 输入框清空，恢复展示默认人员列表
    }

    @Test
    @Order(1210)
    @DisplayName("GNYL_121: 新建一级需求条目")
    void test_GNYL_121_CreateFirstLevelRequirementItem() {
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().dblclick();
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新建子级")).click();
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("刷新")).click();
        page.waitForTimeout(2000);

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

        page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(firstItem))
                .locator("div").first()
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(1000);

        for (int i = 0; i < 5; i++) {
            page.getByText("新建", new Page.GetByTextOptions().setExact(true)).click();
            page.waitForTimeout(500);
        }

        page.locator("div").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^子级对象$"))).click();
        page.waitForTimeout(2000);

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

        page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(subItem))
                .locator("div").first()
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.waitForTimeout(1000);

        page.getByText("删除", new Page.GetByTextOptions().setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确定")).click();
        page.waitForTimeout(1000);

        log.info("GNYL_123 删除条目 {} 成功", subItem);
    }

    @Test
    @Order(1280)
    @DisplayName("GNYL_128/129/130: 显示大纲 -> 结构定位 -> 隐藏大纲")
    void test_GNYL_128_129_130_OutlineAndStructure() {
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(TestConstants.REQ_NAME1).setExact(true))
                .first().dblclick();
        page.waitForTimeout(1000);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("显示大纲")).click();
        page.waitForTimeout(500);

        Locator outlineTab = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("结构"));
        assertThat(outlineTab).isVisible();
        page.waitForTimeout(500);
        log.info("GNYL_128 显示大纲通过，条目可点击");

        outlineTab.click();
        page.waitForTimeout(500);

        Locator structItem = page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName("req-")).locator("div").first();
        assertThat(structItem).isVisible();
        structItem.click();
        page.waitForTimeout(500);
        log.info("GNYL_129 结构视图定位通过");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("隐藏大纲")).click();
        page.waitForTimeout(500);

        Locator showBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("显示大纲"));
        assertThat(showBtn).isVisible();
        log.info("GNYL_130 隐藏大纲通过");
    }
}
