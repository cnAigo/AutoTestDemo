package cases;


import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReqTest {

    @Test
    @Order(720)
    @DisplayName("GNYL_72:新建需求规格")
    void test_NGYL072_newReq(){

    }

    @Test
    @Order(780)
    @DisplayName("GNYL_78:新建需求规格")
    void test_NGYL078_modifyReq(){

    }

    @Test
    @Order(840)
    @DisplayName("GNYL_84:删除需求规格")
    void test_NGYL084_deleteReq(){

    }


}
