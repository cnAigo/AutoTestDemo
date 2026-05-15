package cases;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.TestConfig;

/**
 * 专门用于测试数据清理的工具类
 */
public class TestDataCleaner {

    /**
     * 清理指定文件夹下的所有子节点（需求规格、子文件夹），但保留该文件夹本身
     *
     * @param page     Playwright的Page对象（用于发送API请求）
     * @param folderId 需要清理的父节点ID（例如“自动化测试”文件夹的ID）
     */

    public static void cleanAutoTestFolder(Page page, String folderId) {
        if (folderId == null || folderId.isEmpty()) {
            System.out.println("没有找到目标节点ID，跳过数据清理。");
            return;
        }

        System.out.println("\n====== 开始清理目录 [" + folderId + "] 下的所有子节点 ======");

        // 1. 调用查询接口，获取该节点下所有的子元素
        String searchPayload = "{\"objectId\": \"%s\"}".formatted(folderId);
        APIResponse searchResp = page.request().post(TestConfig.API_PREFIX+"/dev-api/erm/search/searchReqFolderChildrenList",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(searchPayload)
        );

        try {
            JsonObject root = JsonParser.parseString(searchResp.text()).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");

            // --- 2. 遍历清理【需求规格】(reqSpeList) ---
            if (data.has("reqSpeList") && !data.get("reqSpeList").isJsonNull()) {
                JsonArray reqSpeList = data.getAsJsonArray("reqSpeList");
                for (JsonElement element : reqSpeList) {
                    String docId = element.getAsJsonObject().get("objectId").getAsString();
                    String docTitle = element.getAsJsonObject().get("title").getAsString();

                    String docPayload = """
                        {"objectId": "%s", "parentId": "%s", "parentType": "reqSpeFolder"}
                        """.formatted(docId, folderId);

                    page.request().post(TestConfig.API_PREFIX+"/dev-api/erm/del/delReqSpe",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(docPayload));
                    page.request().post(TestConfig.API_PREFIX+"/dev-api/erm/clean/cleanReqSpe",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(docPayload));

                    System.out.println("已通过 API 清理需求规格: [" + docTitle + "]");
                }
            }

            // --- 3. 遍历清理【子文件夹】(reqSpeFolderList) ---
            if (data.has("reqSpeFolderList") && !data.get("reqSpeFolderList").isJsonNull()) {
                JsonArray reqSpeFolderList = data.getAsJsonArray("reqSpeFolderList");
                for (JsonElement element : reqSpeFolderList) {
                    String subFolderId = element.getAsJsonObject().get("objectId").getAsString();
                    String subFolderTitle = element.getAsJsonObject().get("title").getAsString();

                    String delFolderPayload = """
                        {"objectId": "%s", "parentId": "%s", "parentType": "reqSpeFolder"}
                        """.formatted(subFolderId, folderId);

                    String cleanFolderPayload = """
                        {"objectId": "%s"}
                        """.formatted(subFolderId);

                    page.request().post(TestConfig.API_PREFIX+"/dev-api/erm/del/delReqSpeFolder",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(delFolderPayload));
                    page.request().post(TestConfig.API_PREFIX+"/dev-api/erm/clean/cleanReqSpeFolder",
                            RequestOptions.create().setHeader("Content-Type", "application/json").setData(cleanFolderPayload));

                    System.out.println("已通过 API 清理子文件夹: [" + subFolderTitle + "]");
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ API 清理子节点出现异常: " + e.getMessage());
        }

        // 刷新一下页面，让前端树状图同步最新的干净状态
        page.reload();
        System.out.println("====== 目标目录清理完毕！ ======");
    }
}