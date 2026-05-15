package cases;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.TestConfig;

public class TestDataCleaner {

    /**
     * 根据名称智能清理：利用树结构接口，一次性拿到父节点和所有子节点的 ID 并执行清理
     */
    public static void cleanFolderByName(Page page, String targetName, String projectId) {
        System.out.println("\n====== 🚀 开始智能清理: [" + targetName + "] ======");

        // 1. 获取完整的树结构
        String treePayload = """
                {
                    "projectId": "%s",
                    "parentId": "%s",
                    "parentType": "project"
                }
                """.formatted(projectId, projectId);

        APIResponse treeResp = page.request().post(TestConfig.API_PREFIX + "/erm/search/searchReqFolderStructureTree",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(treePayload)
        );

        try {
            JsonObject treeRoot = JsonParser.parseString(treeResp.text()).getAsJsonObject();
            JsonArray dataList = treeRoot.getAsJsonArray("data");

            JsonObject targetFolderNode = null;
            String folderId = null;

            // 2. 遍历第一层，找到 title 为 "自动化测试" 的那个对象
            for (JsonElement el : dataList) {
                JsonObject node = el.getAsJsonObject();
                if (targetName.equals(node.get("title").getAsString())) {
                    targetFolderNode = node;
                    folderId = node.get("objectId").getAsString();
                    break;
                }
            }

            if (targetFolderNode == null || folderId == null) {
                System.out.println("ℹ️ 未找到名为 [" + targetName + "] 的文件夹，无需清理。");
                return;
            }

            System.out.println("✅ 锁定目标文件夹 ID: " + folderId);

            // 3. 核心：直接遍历它自带的 children 数组进行清理
            if (targetFolderNode.has("children") && !targetFolderNode.get("children").isJsonNull()) {
                JsonArray children = targetFolderNode.getAsJsonArray("children");
                for (JsonElement childEl : children) {
                    JsonObject child = childEl.getAsJsonObject();
                    String childId = child.get("objectId").getAsString();
                    String childTitle = child.get("title").getAsString();
                    String childType = child.get("type").getAsString(); // 判断是 reqSpe 还是 reqSpeFolder

                    // 构造通用的删除 Payload
                    String delPayload = """
                            {"objectId": "%s", "parentId": "%s", "parentType": "reqSpeFolder"}
                            """.formatted(childId, folderId);

                    String cleanPayload = """
                            {"objectId": "%s"}
                            """.formatted(childId);

                    // 根据 type 调用不同的删除接口
                    if ("reqSpeFolder".equals(childType)) {
                        page.request().post(TestConfig.API_PREFIX + "/erm/del/delReqSpeFolder",
                                RequestOptions.create().setHeader("Content-Type", "application/json").setData(delPayload));
                        page.request().post(TestConfig.API_PREFIX + "/erm/clean/cleanReqSpeFolder",
                                RequestOptions.create().setHeader("Content-Type", "application/json").setData(cleanPayload));
                        System.out.println("  🗑️ 已清理子文件夹: " + childTitle);
                    } else if ("reqSpe".equals(childType)) {
                        page.request().post(TestConfig.API_PREFIX + "/erm/del/delReqSpe",
                                RequestOptions.create().setHeader("Content-Type", "application/json").setData(delPayload));
                        page.request().post(TestConfig.API_PREFIX + "/erm/clean/cleanReqSpe",
                                RequestOptions.create().setHeader("Content-Type", "application/json").setData(delPayload)); // reqSpe的清除可能也是传全量payload
                        System.out.println("  📄 已清理需求规格: " + childTitle);
                    }
                }
            }

            // 4. 最后把 "自动化测试" 自己也删掉！
            String delSelfPayload = """
                    {"objectId": "%s", "parentId": "%s", "parentType": "project"}
                    """.formatted(folderId, projectId);
            page.request().post(TestConfig.API_PREFIX + "/erm/del/delReqSpeFolder",
                    RequestOptions.create().setHeader("Content-Type", "application/json").setData(delSelfPayload));

            String cleanSelfPayload = """
                    {"objectId": "%s"}
                    """.formatted(folderId);
            page.request().post(TestConfig.API_PREFIX + "/erm/clean/cleanReqSpeFolder",
                    RequestOptions.create().setHeader("Content-Type", "application/json").setData(cleanSelfPayload));

            System.out.println("✅ 父文件夹 [" + targetName + "] 本身已删除！");

        } catch (Exception e) {
            System.out.println("⚠️ 清理过程发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        // 5. 刷新页面，保持干净的环境
        page.reload();
        System.out.println("====== ✨ 环境清理完毕 ✨ ======");
    }
}