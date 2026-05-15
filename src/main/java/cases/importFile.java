package cases;


import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class importFile {

    @Test
    @Order(340)
    @DisplayName("GNYL_034: 导入excel")
    void test_GNYL_029_importExcel(){

    }

    @Test
    @Order(380)
    @DisplayName("GNYL_038:下载Excel模版")
    void test_GNYL_038_downloadExcel(){

    }


    @Test
    @Order(390)
    @DisplayName("GNYL_039:导入Excel")
    void test_GNYL_039_importExcel(){

    }


    @Test
    @Order(420)
    @DisplayName("GNYL_042:导入excel 填写内容测试")
    void test_GNYL_042_importExcelWhileContent(){

    }


    @Test
    @Order(450)
    @DisplayName("GNYL_045: 导入word")
    void test_GNYL_045_importWord(){    }

    @Test
    @Order(490)
    @DisplayName("GNYL_045: 下载word模版")
    void test_GNYL_049_downloadWord(){}

    @Test
    @Order(500)
    @DisplayName("GNYL_050: 导入word")
    void test_GNYL_050_importWord(){}

    @Test
    @Order(530)
    @DisplayName("GNYL_053: 导入ReqIf")
    void test_NGYL_053_importReqIf(){}

    @Test
    @Order(540)
    @DisplayName("GNYL_054: 下载ReqIf模版")
    void test_NGYL_054_downloadReqIF(){}

    @Test
    @Order(580)
    @DisplayName("GNYL_058: ReqIf必填测试")
    void test_NGYL_054_ReqIFWhileContent(){}

    @Test
    @Order(600)
    @DisplayName("GNYL_060: 导出excel")
    void test_NGYL_060_exportExcel(){

    }

    @Test
    @Order(630)
    @DisplayName("GNYL_063: 导出Word")
    void test_NGYL_063_exportWord(){

    }

    @Test
    @Order(660)
    @DisplayName("GNYL_063: 导出ReqIf")
    void test_NGYL_063_exportReqIf(){

    }
}
