package cases;

import pages.RequirementPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class ReqDocumentTest {


    private RequirementPage reqPage;


    @Test
    @Order(45)
    @DisplayName("测试用例 GNYL_072：新建需求规格(需求树)")
    void test_GNYL_072_createDocument_01(){
        reqPage.rightClickTreeNode("自动化测试");
        reqPage.clickContextMenu("新建");
        String OrigenName = reqPage.createDocumentAndGetName();
        reqPage.ensureNodeExpanded("自动化测试");

    }
    @Test
    @Order(46)
    @DisplayName("测试用例 GNYL_073：新建需求规格（）")
    void test_GNYL_072_createDocument_02(){

    }
    @Test
    @Order(47)
    @DisplayName("测试用例 GNYL_074：新建需求规格")
    void test_GNYL_072_createDocument_03(){

    }
}
