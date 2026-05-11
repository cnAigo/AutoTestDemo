package com.test.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class RequirementPage {
    private final Page page;

    public RequirementPage(Page page) {
        this.page = page;
    }

    // ==========================================
    // 🧱 基础操作积木 (UI交互细节全在这里)
    // ==========================================

    /**
     * 积木：右键点击左侧树状图的指定节点
     */
    public void rightClickTreeNode(String nodeName) {
        Locator node = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(nodeName)).locator("div").first();
        node.click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
    }

    /**
     * 积木：双击左侧树状图的指定节点
     */
    public void doubleClickTreeNode(String nodeName) {
        page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(nodeName)).first().dblclick();
    }



    /**
     * 积木：点击右键弹出的菜单项（如“新建”、“删除”）
     */
    public void clickContextMenu(String menuName) {
        page.locator("span").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + menuName + "$"))).click();
    }




    /**
     * 积木：智能展开树节点 (修改为接收节点名称，不再接收 Locator)
     */
    public void ensureNodeExpanded(String nodeName) {
        Locator node = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(nodeName)).locator("div").first();
        Locator expandArrow = node.locator(".el-tree-node__expand-icon").first();
        String classAttribute = expandArrow.getAttribute("class");
        if (classAttribute != null && !classAttribute.contains("expanded") && !classAttribute.contains("is-leaf")) {
            expandArrow.click();
        }
    }


    // ==========================================
    // 🧱 复合业务积木 (你原来写好的优秀逻辑)
    // ==========================================

    /**
     * 积木：点击“文件夹”，拦截并解析API返回的新建文件夹名称
     */
    /**
     * 👑 超级积木：点击“文件夹”，不仅拦截默认名称，还拦截系统生成的真实 objectId
     */
    public String[] createFolderAndGetDetails() {
        Response response = page.waitForResponse(
                res -> res.url().contains("/erm/add/addReqSpeFolder") && res.status() == 200,
                () -> page.getByText("文件夹", new Page.GetByTextOptions().setExact(true)).click()
        );

        String responseBody = response.text();

        String title = "";
        String objectId = "";

        try {
            // 1. 精准提取 title
            int titleStart = responseBody.indexOf("\"title\":\"") + 9;
            int titleEnd = responseBody.indexOf("\"", titleStart);
            title = responseBody.substring(titleStart, titleEnd);

            // 2. 🌟 核心修复点：精准提取 objectId！
            int idStart = responseBody.indexOf("\"objectId\":\"") + 12;
            int idEnd = responseBody.indexOf("\"", idStart);
            objectId = responseBody.substring(idStart, idEnd);

        } catch (Exception e) {
            System.out.println("⚠️ 解析 JSON 失败！请检查返回值格式: " + responseBody);
        }

        // 把名字和真实ID打包返回
        return new String[]{title, objectId};
    }


    /**
     * 积木：点击“文件夹”，拦截并解析API返回的新建需求规格名称
     */
    public String createDocumentAndGetName() {
        Response response = page.waitForResponse(
                res -> res.url().contains("/erm/add/addReqSpeFolder") && res.status() == 200,
                () -> page.getByText("文件夹", new Page.GetByTextOptions().setExact(true)).click()
        );

        String responseBody = response.text();
        int startIndex = responseBody.indexOf("\"title\":\"") + 9;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }

    /**
     * 积木：通用的重命名逻辑
     */
    public void renameFolder(String originalName, String targetName) {
        // 1. 加上精确匹配，精准定位左侧树节点
        Locator folderItem = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(originalName).setExact(true)).locator("div").first();
        folderItem.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 2. 点击选中该节点
        folderItem.click();

        // 3. 只在该节点内部找文本并点击（触发改名），拒绝全局搜索误伤！
        folderItem.getByText(originalName).click();

        // 4. 等待输入框出现并填入新名字
        Locator editInput = page.locator("input:focus");
        editInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editInput.fill(targetName);

        // 5. 点击根节点保存
        page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName("需求（根节点）")).locator("div").first().click();
        page.waitForTimeout(500);

        // 6. 验证新名字是否生效
        Locator renamedFolder = page.locator(".el-tree-node:visible")
                .filter(new Locator.FilterOptions().setHasText(targetName))
                .first();
        renamedFolder.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertThat(renamedFolder).isVisible();
    }

    /**
     * 积木：自动删除指定的文件夹/文件
     */
    public void safeDeleteFolder(String folderName) {
        Locator targetNode = page.getByRole(AriaRole.TREEITEM,
                new Page.GetByRoleOptions().setName(folderName).setExact(true));

        if (targetNode.count() == 0) return;
        targetNode.scrollIntoViewIfNeeded();

        // 阶段一：删除
        targetNode.locator("div").first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        Locator deleteMenuBtn = page.getByText("删除", new Page.GetByTextOptions().setExact(true)).last();
        deleteMenuBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        deleteMenuBtn.click();
        page.mouse().click(0, 0);
        page.waitForTimeout(500);

        // 阶段二：清除
        targetNode.locator("div").first().click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        Locator clearMenuBtn = page.getByText("清除", new Page.GetByTextOptions().setExact(true)).last();
        clearMenuBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        clearMenuBtn.click();

        // 阶段三：弹窗确认
        try {
            Locator confirmBtn = page.locator(".el-message-box__btns button, .el-dialog__footer button")
                    .filter(new Locator.FilterOptions().setHasText("确定")).first();
            confirmBtn.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            confirmBtn.focus();
            confirmBtn.press("Space");
        } catch (TimeoutError e) {
            Locator fallbackConfirmBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("确定"));
            fallbackConfirmBtn.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            fallbackConfirmBtn.click();
        }

        // 验证消失
        try {
            targetNode.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}
        assertThat(targetNode).isHidden();
    }


    /**
     * 积木：点击“新建”按钮 -> 弹出菜单 -> 点击“新增文件夹” -> 截获API返回的名字
     */
    public String clickNewFolderDropdownAndGetName() {
        // 1. 先点击那个“新建”主按钮
        // 注意：这里建议用精确匹配，防止点到别的按钮
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新建").setExact(true)).click();

        // 2. 关键点：等待下拉菜单中的“新增文件夹”变为可见状态
        // 这一步解决了你说的“间隔时间”问题，它是智能等待，弹出来的一瞬间就会继续
        Locator folderOption = page.getByText("新增文件夹").last(); // 有时菜单项会重复，取最后一个比较稳
        folderOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 3. 盯着接口，点击“新增文件夹”选项
        Response response = page.waitForResponse(
                res -> res.url().contains("/erm/add/addReqSpeFolder") && res.status() == 200,
                () -> folderOption.click()
        );

        // 4. 解析并返回名字（代码同前）
        String responseBody = response.text();
        int startIndex = responseBody.indexOf("\"title\":\"") + 9;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }
    /**
     * 公共方法：打开父文件夹并激活指定子文件夹的重命名输入框
     * @param parentName 父文件夹名称
     * @param targetName 要修改的子文件夹原名
     */
    private void openFolderAndActivateEdit(String parentName, String targetName) {
        // 1. 展开左侧树（使用精确匹配防止冲突）
        page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(parentName).setExact(true))
                .locator("img").click();

        // 2. 双击父节点确保右侧列表加载
        page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(parentName).setExact(true))
                .dblclick();

        // 3. 点击右侧列表中的目标文件夹，激活编辑状态
        // 注意：这里用 .cell-title 或者 pre 定位比较稳
        page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(targetName))
                .locator(".cell-title").first().click();

        // 双击文字激活输入框
        page.getByText(targetName).dblclick();
    }

}