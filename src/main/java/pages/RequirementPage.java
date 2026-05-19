package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;
import config.TestConstants;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * 需求管理页面 - 纯 UI 操作封装
 * 所有方法只做页面交互，不直接发 API 请求
 */
public class RequirementPage {

    private final Page page;

    public RequirementPage(Page page) {
        this.page = page;
    }

    // ======================== 树节点基础操作 ========================
    /**
     * 等待指定名称的树节点出现在可视区域
     */
    public void waitForTreeNodeVisible(String nodeName) {
        page.locator(".el-tree-node:visible")
                .filter(new Locator.FilterOptions().setHasText(nodeName))
                .first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    /** 右键点击左侧树节点 */
    public void rightClickTreeNode(String nodeName) {
        Locator node = page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(nodeName)).locator("div").first();
        node.click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
    }

    /** 双击左侧树节点 */
    public void doubleClickTreeNode(String nodeName) {
        page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(nodeName)).first().dblclick();
    }

    /** 点击右键菜单项（新建、删除等） */
    public void clickContextMenu(String menuName) {
        page.locator("span")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + menuName + "$")))
                .click();
    }

    /** 智能展开树节点（已展开则跳过） */
    public void ensureNodeExpanded(String nodeName) {
        Locator node = page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(nodeName)).locator("div").first();
        Locator expandArrow = node.locator(".el-tree-node__expand-icon").first();
        String cls = expandArrow.getAttribute("class");
        if (cls != null && !cls.contains("expanded") && !cls.contains("is-leaf")) {
            expandArrow.click();
        }
    }

    // ======================== 创建（UI操作 + 拦截响应） ========================

    /**
     * 右键菜单 → 点击"文件夹" → 拦截API响应 → 返回 [自动生成的名称, objectId]
     * 虽然读了网络响应，但触发方式是 UI 交互，所以归类为 UI 操作
     */
    public String[] createFolderAndGetDetails() {
        Response response = page.waitForResponse(
                res -> res.url().contains("/erm/add/addReqSpeFolder") && res.status() == 200,
                () -> page.getByText("文件夹", new Page.GetByTextOptions().setExact(true)).click()
        );

        String body = response.text();
        String title = extractJsonValue(body, "title");
        String objectId = extractJsonValue(body, "objectId");

        return new String[]{title, objectId};
    }

    /**
     * 右键菜单 → 点击"文件夹" → 拦截响应 → 返回自动生成的名称
     */
    public String createDocumentAndGetName() {
        Response response = page.waitForResponse(
                res -> res.url().contains("/erm/add/addReqSpeFolder") && res.status() == 200,
                () -> page.getByText("文件夹", new Page.GetByTextOptions().setExact(true)).click()
        );

        return extractJsonValue(response.text(), "title");
    }

    /**
     * 顶部菜单 → 新建 → 新增文件夹 → 拦截响应 → 返回自动生成的名称
     */
    public String clickNewFolderDropdownAndGetName() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("新建").setExact(true)).click();

        Locator folderOption = page.getByText("新增文件夹").last();
        folderOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        Response response = page.waitForResponse(
                res -> res.url().contains("/erm/add/addReqSpeFolder") && res.status() == 200,
                () -> folderOption.click()
        );

        return extractJsonValue(response.text(), "title");
    }

    // ======================== 重命名 ========================

    /** 重命名树节点（选中 → 点击文字 → 输入新名 → 点击根节点保存） */
    /** 重命名树节点（选中 → 点击文字 → 输入新名 → 点击根节点保存） */
    public void renameFolder(String originalName, String newName) {
        // 1. 先用 CSS 选择器找到包含该文本的节点（不依赖 role）
        Locator node = page.locator(".el-tree-node")
                .filter(new Locator.FilterOptions().setHasText(originalName))
                .first();
        node.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(15000));

        // 2. 如果节点已经在编辑态（有 focus input），直接填
        Locator focusedInput = page.locator("input:focus");
        if (focusedInput.count() > 0 && focusedInput.isVisible()) {
            focusedInput.press("Control+a");
            focusedInput.fill(newName);
            // 点击根节点保存
            page.getByRole(AriaRole.TREEITEM,
                            new Page.GetByRoleOptions().setName(TestConstants.ROOT_NODE))
                    .locator("div").first().click();
            page.waitForTimeout(500);
        } else {
            // 3. 不在编辑态，走正常双击激活流程
            node.locator(".el-tree-node__label, .custom-tree-node").first().click();
            page.waitForTimeout(300);
            node.locator(".el-tree-node__label, .custom-tree-node").first().click();
            page.waitForTimeout(300);

            Locator editInput = page.locator("input:focus");
            editInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            editInput.press("Control+a");
            editInput.fill(newName);

            page.getByRole(AriaRole.TREEITEM,
                            new Page.GetByRoleOptions().setName(TestConstants.ROOT_NODE))
                    .locator("div").first().click();
            page.waitForTimeout(500);
        }

        // 4. 验证
        Locator renamed = page.locator(".el-tree-node")
                .filter(new Locator.FilterOptions().setHasText(newName)).first();
        renamed.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        assertThat(renamed).isVisible();
    }


    // ======================== 删除（纯 UI） ========================

    /** 右键删除 + 清除 + 弹窗确认 */
    public void safeDeleteFolder(String folderName) {
        Locator target = page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(folderName).setExact(true));

        if (target.count() == 0) return;
        target.scrollIntoViewIfNeeded();

        // 阶段一：删除
        target.locator("div").first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.getByText("删除", new Page.GetByTextOptions().setExact(true)).last()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.getByText("删除", new Page.GetByTextOptions().setExact(true)).last().click();
        page.mouse().click(0, 0);
        page.waitForTimeout(500);

        // 阶段二：清除
        target.locator("div").first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        page.getByText("清除", new Page.GetByTextOptions().setExact(true)).last()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.getByText("清除", new Page.GetByTextOptions().setExact(true)).last().click();

        // 阶段三：确认弹窗
        try {
            Locator confirm = page.locator(".el-message-box__btns button, .el-dialog__footer button")
                    .filter(new Locator.FilterOptions().setHasText("确定")).first();
            confirm.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            confirm.focus();
            confirm.press("Space");
        } catch (TimeoutError e) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确定"))
                    .waitFor(new Locator.WaitForOptions().setTimeout(3000));
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确定")).click();
        }

        try {
            target.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}
        assertThat(target).isHidden();
    }

    // ======================== 描述编辑 ========================

    /** 展开父节点并激活子节点的编辑状态 */
    public void openFolderAndActivateEdit(String parentName, String targetName) {
        page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(parentName).setExact(true)).locator("img").click();

        page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(parentName).setExact(true)).dblclick();

        page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(targetName))
                .locator(".cell-title").first().click();

        page.getByText(targetName).dblclick();
    }

    // ======================== 内部工具 ========================

    /** 从 JSON 字符串中提取指定字段值 */
    private String extractJsonValue(String json, String fieldName) {
        try {
            int start = json.indexOf("\"" + fieldName + "\":\"") + fieldName.length() + 4;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            System.out.println("⚠️ 解析字段 [" + fieldName + "] 失败: " + json);
            return "";
        }
    }

    // ======================== RequirementPage.java 新增 ========================

    /**
     * 积木：点击树节点的文字，激活重命名输入框
     */
    public void activateRenameInput(String nodeName) {
        Locator node = page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName(nodeName).setExact(true))
                .locator("div").first();
        node.click();
        node.getByText(nodeName).click();
    }

    /**
     * 积木：在当前激活的输入框中填入新名字，然后点击根节点保存
     * 注意：不包含断言，适合"期望成功"和"期望失败"的场景都能用
     */
    public String fillRenameAndSave(String newName) {
        // 1. 填入新名字
        Locator editInput = page.locator("input:focus");
        editInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editInput.press("Control+a");
        editInput.fill(newName);

        // 2. 点击根节点触发保存
        page.getByRole(AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName("需求（根节点）"))
                .locator("div").first().click();
        page.waitForTimeout(1000);

        // 3. 尝试捕获错误提示（有的话就返回，没有就返回空）
        try {
            Locator toast = page.locator(".el-message--error, .el-message__content")
                    .first();
            if (toast.isVisible()) {
                return toast.textContent();
            }
        } catch (Exception ignored) {}

        return "";
    }

}
