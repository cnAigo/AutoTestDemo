package cases;


import base.BaseTest;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import config.TestConfig;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.RequirementPage;

import static cases.RequirementTest.PROJECT_ID;
import static cases.RequirementTest.dynamicParentId;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReqTest extends BaseTest {

    private RequirementPage reqPage;

    // 🌟 必须是 static，否则修改和删除方法拿不到这个 ID
    private static String reqId = "";
    private static final String ReqName = "自动化需求规格";
    private static final Logger log = LoggerFactory.getLogger(RequirementTest.class);
    @BeforeAll
    public void initPage() {
        // 这里会正确引用父类 BaseTest 的 page
        reqPage = new RequirementPage(page);
    }

    @Test
    @Order(720)
    @DisplayName("GNYL_72:新建需求规格")
    void test_NGYL072_newReq(){
        // 前置检查
        Assumptions.assumeTrue(dynamicParentId != null && !dynamicParentId.isEmpty(), "未获取到父节点ID");

        // 调用拎出来的积木方法
        String docId = reqPage.createDocumentViaAPI(PROJECT_ID, dynamicParentId);

        // 验证结果
        Assertions.assertFalse(docId.isEmpty(), "未能获取到新创建文档的 ID");
        reqId = docId;
        log.info("GNYL_072 (API) 创建文档完成!");

    }

    @Test
    @Order(780)
    @DisplayName("GNYL_078: 修改需求规格名称")
    void test_NGYL078_modifyReq() {
        // 1. 前置检查：确保我们有要修改的需求规格 ID
        Assumptions.assumeTrue(reqId != null && !reqId.isEmpty(), "未获取到需求规格ID，无法执行修改");

        // 2. 构造 Payload
        String jsonPayload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "title": "%s"
                }
                """.formatted(PROJECT_ID, reqId, dynamicParentId, ReqName);

        // 3. 发送修改请求
        APIResponse response = page.request().post(TestConfig.API_PREFIX + "/erm/update/updateReqSpeInfo",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(jsonPayload)
        );

        // 4. 解析并验证返回结果
        String responseText = response.text();
        log.info("GNYL_078 修改返回内容: " + responseText);

        // 5. 断言：验证状态码和业务返回码
        Assertions.assertEquals(200, response.status(), "HTTP状态码错误");
        Assertions.assertTrue(responseText.contains("\"code\":200") || responseText.contains("\"code\": 200"),
                "业务返回码不是 200");
        Assertions.assertTrue(responseText.contains("修改成功"), "返回信息未包含 '修改成功'");


        log.info("GNYL_078 测试通过");
    }

    @Test
    @Order(840)
    @DisplayName("GNYL_84:删除需求规格")
    void test_NGYL084_deleteReq(){

    }


    // ==========================================
    // 🔍 视图切换与搜索功能 (GNYL_090 - GNYL_095)
    // ==========================================

    @Test
    @Order(900)
    @DisplayName("GNYL_090: 需求表视图切换")
    void test_GNYL_090_DemandTable() {
        // 待补充：点击展开需求视图下拉选，选择“需求表”
    }

    @Test
    @Order(910)
    @DisplayName("GNYL_091: 需求树视图切换")
    void test_GNYL_091_DemandTree() {
        // 待补充：点击展开需求视图下拉选，选择“需求树”
    }

    @Test
    @Order(920)
    @DisplayName("GNYL_092: 存在的节点名称检索")
    void test_GNYL_092_SearchExistingNode() {
        // 待补充：输入存在的“节点名称”并搜索定位
    }

    @Test
    @Order(930)
    @DisplayName("GNYL_093: 节点名称模糊查询")
    void test_GNYL_093_FuzzySearchNode() {
        // 待补充：输入部分节点名称关键字查询
    }

    @Test
    @Order(940)
    @DisplayName("GNYL_094: 不存在的节点名称检索")
    void test_GNYL_094_SearchNonExistentNode() {
        // 待补充：输入不存在的“节点名称”验证暂无数据
    }

    @Test
    @Order(950)
    @DisplayName("GNYL_095: 清空节点名称输入框")
    void test_GNYL_095_ClearSearchInput() {
        // 待补充：输入名称后点击“x”清空
    }

    // ==========================================
    // 📄 需求规格属性与文件管理 (GNYL_096 - GNYL_112)
    // ==========================================

    @Test
    @Order(960)
    @DisplayName("GNYL_096: 查看属性")
    void test_GNYL_096_ViewProperties() {
        // 1. 选择需求规格，右键点击“属性”
        // 预期：成功进入属性页面，展示属性信息
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
        // 1. 清空名称并确定
        // 预期：提示“需求规格名称不能为空”
    }

    @Test
    @Order(990)
    @DisplayName("GNYL_099: 规格名称重复校验")
    void test_GNYL_099_NameDuplicate() {
        // 1. 输入已存在的名称并确定
        // 预期：提示“需求规格名称已存在”
    }

    @Test
    @Order(1000)
    @DisplayName("GNYL_100: 输入非字母开头的前缀")
    void test_GNYL_100_PrefixNotStartWithLetter() {
        // 预期：提示编码规则不符合要求（必须字母开头）
    }

    @Test
    @Order(1010)
    @DisplayName("GNYL_101: 输入非法组合的前缀")
    void test_GNYL_101_PrefixIllegalChars() {
        // 预期：提示仅能包含字母和数字
    }

    @Test
    @Order(1020)
    @DisplayName("GNYL_102: 输入长度超过10个字符的前缀")
    void test_GNYL_102_PrefixTooLong() {
        // 预期：提示长度不能超过10个字符
    }

    @Test
    @Order(1030)
    @DisplayName("GNYL_103: 输入符合条件的前缀")
    void test_GNYL_103_PrefixValid() {
        // 预期：保存成功
    }

    @Test
    @Order(1040)
    @DisplayName("GNYL_104: 填写不超过1000字的描述")
    void test_GNYL_104_DescriptionValid() {
        // 预期：保存成功
    }

    @Test
    @Order(1050)
    @DisplayName("GNYL_105: 填写超过1000字的描述")
    void test_GNYL_105_DescriptionTooLong() {
        // 预期：无法输入或保存失败
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
        // 1. 在“需求树”列表，双击需求规格
        // 2. 在右侧操作栏，点击“新建”
        // 预期：下方列表新增一条需求条目
    }

    @Test
    @Order(1220)
    @DisplayName("GNYL_122: 新建子需求条目")
    void test_GNYL_122_CreateSubRequirementItem() {
        // 1. 在右侧需求条目列表，点击选择需求条目
        // 2. 在列表上方操作栏，点击“新建”
        // 预期：选择的需求条目下方新增一条子需求条目
    }

    @Test
    @Order(1230)
    @DisplayName("GNYL_123: 删除需求条目")
    void test_GNYL_123_DeleteRequirementItem() {
        // 1. 右键单击需求条目，点击“删除”
        // 预期：条目变为删除状态并增加删除标识
    }

    @Test
    @Order(1240)
    @DisplayName("GNYL_124: 取消删除需求条目")
    void test_GNYL_124_CancelDeleteRequirementItem() {
        // 1. 右键单击已删除的需求条目，点击“取消删除”
        // 预期：删除标识消失，状态恢复正常
    }

    @Test
    @Order(1250)
    @DisplayName("GNYL_125: 清除需求条目")
    void test_GNYL_125_ClearRequirementItem() {
        // 1. 右键单击需求条目，点击“清除”
        // 预期：该需求条目被彻底清除
    }

    // ==========================================
    // 🏗️ 结构化管理与对象新建 (GNYL_126 - GNYL_130)
    // ==========================================

    @Test
    @Order(1260)
    @DisplayName("GNYL_126: 新建同级对象")
    void test_GNYL_126_CreatePeerObject() {
        // 1. 双击需求规格，选择需求条目，右键点击“新建” -> “同级对象”
        // 预期：列表新增一条所选需求同级的需求条目
    }

    @Test
    @Order(1270)
    @DisplayName("GNYL_127: 新建子级对象")
    void test_GNYL_127_CreateChildObject() {
        // 1. 选择需求条目，右键点击“新建” -> “子级对象”
        // 预期：列表新增一条所选需求的子级需求条目
    }

    @Test
    @Order(1280)
    @DisplayName("GNYL_128: 显示大纲")
    void test_GNYL_128_ShowOutline() {
        // 1. 点击“显示大纲”，在大纲中点击需求条目
        // 预期：左侧展示大纲标签页，点击后右侧视图成功定位
    }

    @Test
    @Order(1290)
    @DisplayName("GNYL_129: 显示结构")
    void test_GNYL_129_ShowStructure() {
        // 1. 点击左侧“结构”标签页，点击需求条目
        // 预期：展示所有需求条目，点击后右侧视图成功定位
    }

    @Test
    @Order(1300)
    @DisplayName("GNYL_130: 隐藏大纲")
    void test_GNYL_130_HideOutline() {
        // 1. 点击“隐藏大纲”
        // 预期：大纲标签页隐藏，“隐藏大纲”变为“显示大纲”
    }



}
