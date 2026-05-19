package actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 需求管理模块 - 纯 API 操作封装
 * 所有方法通过 HTTP 请求与后端交互，不涉及页面 UI 操作
 */
public class ReqApiActions {

    private static final Logger log = LoggerFactory.getLogger(ReqApiActions.class);
    private final APIRequestContext request;

    public ReqApiActions(APIRequestContext request) {
        this.request = request;
    }


    /** 根据项目名称获取 projectId（GET 请求，无参数） */
    public String getProjectIdByName(String projectName) {
        // 🌟 这个接口是 GET 请求，不需要 body
        APIResponse response = request.get(
                TestConfig.API_PREFIX + "/common/search/searchProjectByUser"
        );

        String resp = response.text();


        JsonObject root = JsonParser.parseString(resp).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");

        if (data == null) {
            throw new RuntimeException("接口返回 data 为 null，响应: " + resp);
        }

        for (JsonElement el : data) {
            JsonObject project = el.getAsJsonObject();
            String name = project.get("projectName").getAsString();
            if (projectName.equals(name)) {
                String projectId = project.get("projectId").getAsString();
                log.info("找到项目 [{}] → projectId: {}", projectName, projectId);
                return projectId;
            }
        }

        throw new RuntimeException("未找到项目: " + projectName);
    }


    // ======================== 创建 ========================

    /** 创建文件夹，返回 objectId */
    public String createFolder(String projectId, String parentId) {
        String payload = """
                {
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "projectId": "%s"
                }
                """.formatted(parentId, projectId);

        String resp = post("/erm/add/addReqSpeFolder", payload);
        String id = extractField(resp, "objectId");
        //log.info("  API 创建文件夹成功, ID: {}", id);
        return id;
    }

    /** 创建需求规格文档，返回 objectId */
    public String createDocument(String projectId, String parentId) {
        String payload = """
                {
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "projectId": "%s"
                }
                """.formatted(parentId, projectId);

        String resp = post("/erm/add/addReqSpe", payload);
        String id = extractField(resp, "objectId");
       // log.info("  API 创建文档成功, ID: {}", id);
        return id;
    }



    // ======================== 重命名 ========================

    /** 重命名文件夹，返回原始响应文本 */
    public String renameFolder(String projectId, String objectId, String parentId, String newTitle) {
        String payload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "title": "%s"
                }
                """.formatted(projectId, objectId, parentId, newTitle);

        return post("/erm/update/updateReqSpeFolderInfo", payload);
    }

    /** 重命名需求规格文档（注意：接口路径和文件夹不同） */
    public String renameDocument(String projectId, String objectId, String parentId, String newTitle) {
        String payload = """
            {
                "projectId": "%s",
                "objectId": "%s",
                "parentId": "%s",
                "parentType": "reqSpeFolder",
                "title": "%s"
            }
            """.formatted(projectId, objectId, parentId, newTitle);

        return post("/erm/update/updateReqSpeInfo", payload);
    }


    // ======================== 描述编辑 ========================

    /** 编辑文件夹描述，返回原始响应文本 */
    public String editDescription(String projectId, String objectId, String parentId, String description) {
        String payload = """
                {
                    "projectId": "%s",
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "reqSpeFolder",
                    "description": "%s"
                }
                """.formatted(projectId, objectId, parentId, description);

        return post("/erm/update/updateReqSpeFolderInfo", payload);
    }

    // ======================== 删除 / 恢复 ========================

    /** 删除文件夹，返回原始响应文本 */
    public String deleteFolder(String objectId, String parentId, String parentType) {
        String payload = """
                {
                    "objectId": "%s",
                    "parentId": "%s",
                    "parentType": "%s"
                }
                """.formatted(objectId, parentId, parentType);

        return post("/erm/del/delReqSpeFolder", payload);
    }

    /** 恢复已删除的文件夹，返回原始响应文本 */
    public String recoverFolder(String objectId, String parentId) {
        String payload = """
                {
                    "objectId": "%s",
                    "parentId": "%s"
                }
                """.formatted(objectId, parentId);

        return post("/erm/recover/recoverReqSpeFolder", payload);
    }

    /** 删除需求规格（和文件夹是不同的接口） */
    public String deleteDocument(String objectId, String parentId) {
        String payload = """
            {
                "objectId": "%s",
                "parentId": "%s",
                "parentType": "reqSpeFolder"
            }
            """.formatted(objectId, parentId);

        return post("/erm/del/delReqSpe", payload);
    }

    /** 恢复已删除的需求规格 */
    public String recoverDocument(String objectId, String parentId) {
        String payload = """
            {
                "objectId": "%s",
                "parentId": "%s",
                "parentType": "reqSpeFolder"
            }
            """.formatted(objectId, parentId);

        return post("/erm/recover/recoverReqSpe", payload);
    }

    /** 彻底清除需求规格 */
    public String cleanDocument(String objectId, String parentId) {
        String payload = """
            {
                "objectId": "%s",
                "parentId": "%s",
                "parentType": "reqSpeFolder"
            }
            """.formatted(objectId, parentId);

        return post("/erm/clean/cleanReqSpe", payload);
    }


    // ======================== 查询 ========================

    /** 查询/刷新树结构，返回原始响应文本 */
    public String getTree(String projectId, String parentId) {
        String payload = """
                {
                    "projectId": "%s",
                    "parentId": "%s",
                    "parentType": "project"
                }
                """.formatted(projectId, parentId);

        return post("/erm/search/searchReqFolderStructureTree", payload);
    }

    /**
     * 在树结构中按 title 递归查找节点，返回 objectId
     * 找不到返回 null
     */
    public String findNodeIdByTitle(String projectId, String targetTitle) {
        String resp = getTree(projectId, projectId);
        JsonObject root = JsonParser.parseString(resp).getAsJsonObject();
        JsonArray dataList = root.getAsJsonArray("data");

        for (JsonElement el : dataList) {
            String found = deepFind(el.getAsJsonObject(), targetTitle);
            if (found != null) return found;
        }
        return null;
    }

    // ======================== 清理 ========================

    /** 智能清理：删除目标文件夹及其所有子节点 */
    public void cleanFolderByName(String projectId, String targetName) {
        log.info("====== 开始清理环境 ======");

        String resp = getTree(projectId, projectId);
        JsonObject root = JsonParser.parseString(resp).getAsJsonObject();
        JsonArray dataList = root.getAsJsonArray("data");

        // 1. 找到目标节点
        JsonObject targetNode = null;
        for (JsonElement el : dataList) {
            JsonObject node = el.getAsJsonObject();
            if (targetName.equals(node.get("title").getAsString())) {
                targetNode = node;
                break;
            }
        }

        if (targetNode == null) {
            log.info("未找到 [{}]，无需清理", targetName);
            return;
        }

        String folderId = targetNode.get("objectId").getAsString();
        log.info("锁定目标文件夹 ID: {}", folderId);

        // 2. 清理子节点（区分 reqSpe 和 reqSpeFolder）
        if (targetNode.has("children") && !targetNode.get("children").isJsonNull()) {
            JsonArray children = targetNode.getAsJsonArray("children");
            for (JsonElement childEl : children) {
                JsonObject child = childEl.getAsJsonObject();
                String childId = child.get("objectId").getAsString();
                String childTitle = child.get("title").getAsString();
                String childType = child.get("type").getAsString();

                if ("reqSpeFolder".equals(childType)) {
                    // 子文件夹：用文件夹的接口
                    deleteFolder(childId, folderId, "reqSpeFolder");
                    forceCleanFolder(childId);
                    log.info("  已清理子文件夹: {}", childTitle);
                } else if ("reqSpe".equals(childType)) {
                    deleteDocument(childId, folderId);
                    forceCleanDocument(childId, folderId);  // 🌟 加上 parentId
                    log.info("  已清理需求规格: {}", childTitle);
                }
            }
        }

        // 3. 删除父文件夹自身
        deleteFolder(folderId, projectId, "project");
        forceCleanFolder(folderId);

        log.info("\n====== 清理环境结束 ======");
    }

// ======================== 强制清理（分文件夹和文档两套） ========================

    /** 强制清理文件夹（恢复 + 彻底删除） */
    private void forceCleanFolder(String objectId) {
        String recoverPayload = """
            {"objectId": "%s", "parentId": "%s"}
            """.formatted(objectId, objectId);
        String cleanPayload = """
            {"objectId": "%s"}
            """.formatted(objectId);

        request.post(TestConfig.API_PREFIX + "/erm/recover/recoverReqSpeFolder",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(recoverPayload));
        request.post(TestConfig.API_PREFIX + "/erm/clean/cleanReqSpeFolder",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(cleanPayload));
    }

    /** 强制清理需求规格（删除 + 彻底清除） */
    private void forceCleanDocument(String objectId, String parentId) {
        String payload = """
            {
                "objectId": "%s",
                "parentId": "%s",
                "parentType": "reqSpeFolder"
            }
            """.formatted(objectId, parentId);

        request.post(TestConfig.API_PREFIX + "/erm/recover/recoverReqSpe",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(payload));
        request.post(TestConfig.API_PREFIX + "/erm/clean/cleanReqSpe",
                RequestOptions.create().setHeader("Content-Type", "application/json").setData(payload));
    }



    // ======================== 内部工具 ========================

    private String post(String endpoint, String payload) {
        APIResponse response = request.post(
                TestConfig.API_PREFIX + endpoint,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(payload)
        );
        return response.text();
    }

    private String extractField(String json, String fieldName) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            return data.get(fieldName).getAsString();
        } catch (Exception e) {
            throw new RuntimeException("解析字段 [" + fieldName + "] 失败: " + json, e);
        }
    }

    private String deepFind(JsonObject node, String targetTitle) {
        if (targetTitle.equals(node.get("title").getAsString())) {
            return node.get("objectId").getAsString();
        }
        if (node.has("children") && !node.get("children").isJsonNull()) {
            for (JsonElement child : node.getAsJsonArray("children")) {
                String found = deepFind(child.getAsJsonObject(), targetTitle);
                if (found != null) return found;
            }
        }
        return null;
    }

    //========================= 需求树，需求表 ================================
    /** 查询需求规格列表（需求表视图） */
    public String getReqSpeList(String projectId) {
        String payload = """
            {
                "projectId": "%s"
            }
            """.formatted(projectId);

        return post("/erm/search/searchReqSpeListFromProject", payload);
    }



    //========================= 合作区管理 ================================

    /** 创建自定义属性 */
    public String addCustomAttribute(String nameEn, String name, String type, String projectId) {
        String payload = """
        {
            "nameEn": "%s",
            "name": "%s",
            "type": "%s",
            "current": "1",
            "valueRange": "",
            "defaultValue": "",
            "isMultiple": false,
            "description": "自动化测试创建",
            "businessDomain": "需求管理",
            "objectType": "req",
            "id": "",
            "createTime": "",
            "creator": "",
            "modifier": "",
            "projectId": "%s",
            "usedColor": "#1e90ff",
            "isUseDefaultValue": true,
            "valueRangeMapping": []
        }
        """.formatted(nameEn, name, type, projectId);

        return post("/erm/customAttribute/addCustomAttribute", payload);
    }


    /** 查询自定义属性列表，按英文名查找返回 id */
    public String[] findCustomAttribute(String nameEn, String projectId) {
        String resp = request.get(
                TestConfig.API_PREFIX + "/erm/customAttribute/selectCustomAttributeList",
                RequestOptions.create()
                        .setQueryParam("projectId", projectId)
                        .setQueryParam("businessDomain", "需求管理")
                        .setQueryParam("objectType", "req")
                        .setQueryParam("name", "")
                        .setQueryParam("type", "")
                        .setQueryParam("current", "")
        ).text();

        JsonObject root = JsonParser.parseString(resp).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");

        for (JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            if (nameEn.equals(obj.get("nameEn").getAsString())) {
                return new String[]{
                        obj.get("id").getAsString(),
                        obj.get("createTime").getAsString(),
                        obj.get("creator").getAsString()
                };
            }
        }
        return null;
    }

    /** 修改自定义属性 */
    public String updateCustomAttribute(String id, String nameEn, String name, String type,
                                        String createTime, String creator, String projectId) {
        String payload = """
        {
            "nameEn": "%s",
            "name": "%s",
            "type": "%s",
            "current": "1",
            "valueRange": "",
            "defaultValue": "",
            "isMultiple": false,
            "description": "update text",
            "businessDomain": "需求管理",
            "objectType": "req",
            "id": "%s",
            "createTime": "%s",
            "creator": "%s",
            "modifier": "admin",
            "projectId": "%s",
            "usedColor": "#1e90ff",
            "isUseDefaultValue": true,
            "valueRangeMapping": []
        }
        """.formatted(nameEn, name, type, id, createTime, creator, projectId);

        return post("/erm/customAttribute/updateCustomAttribute", payload);
    }



}
