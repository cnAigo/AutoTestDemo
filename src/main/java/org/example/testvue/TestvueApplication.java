package org.example.testvue;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@SpringBootApplication
public class TestvueApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestvueApplication.class, args);

        System.out.println("\n====== 正在初始化自动化测试引擎 (JUnit Launcher) ======");

        System.out.println("====== 正在按顺序执行测试用例 ======");
        System.out.println("  1. RequirementTest - 需求结构树");
        System.out.println("  2. ReqFolderTest - 文件夹操作");
        System.out.println("  3. ImportFileTest - 导入/导出");
        System.out.println("  4. ReqExportFullTest - 导出功能");
        System.out.println("  5. ReqTest - 需求条目");
        System.out.println("  6. ReqSpecTest - 需求规格");
        System.out.println("  7. AttributeTest - 属性配置");
        System.out.println("  8. CollaborativeEditTest - 协同编辑");
        System.out.println("  9. ReviewProcessTest - 审签流程");
        System.out.println(" 10. VersionTraceTest - 版本追溯");
        System.out.println(" 11. OtherFunctionsTest - 其他功能");
        System.out.println(" 12. CommonTest - 通用/安全/性能");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectClass("cases.RequirementTest"),
                        selectClass("cases.ReqFolderTest"),
                        selectClass("cases.ImportFileTest"),
                        selectClass("cases.ReqExportFullTest"),
                        selectClass("cases.ReqTest"),
                        selectClass("cases.ReqSpecTest"),
                        selectClass("cases.AttributeTest"),
                        selectClass("cases.CollaborativeEditTest"),
                        selectClass("cases.ReviewProcessTest"),
                        selectClass("cases.VersionTraceTest"),
                        selectClass("cases.OtherFunctionsTest"),
                        selectClass("cases.CommonTest")
                )
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        SkippedTrackingListener skippedListener = new SkippedTrackingListener();
        launcher.registerTestExecutionListeners(summaryListener, skippedListener);

        System.out.println("====== 正在自动加载并执行所有测试用例 ======\n");
        launcher.execute(request);

        TestExecutionSummary summary = summaryListener.getSummary();
        printSummary(summary, skippedListener.getSkippedReasons());
    }
    private static void printSummary(TestExecutionSummary summary, List<String> skippedReasons) {
        System.out.println("\n====== 测试执行总结 ======");
        System.out.println("总测试数: " + summary.getTestsFoundCount());
        System.out.println("执行失败: " + summary.getTestsFailedCount());
        System.out.println("被跳过/中断: " + (summary.getTestsSkippedCount() + summary.getTestsAbortedCount()));
        System.out.println("执行成功: " + summary.getTestsSucceededCount());
        System.out.flush();

        if (!summary.getFailures().isEmpty()) {
            System.out.println("\n====== 失败详情 ======");
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                System.err.println(" 失败位置: " + failure.getTestIdentifier().getDisplayName());
                System.err.println(" 错误原因: " + failure.getException().getMessage());
                failure.getException().printStackTrace();
            }
        }

        if (!skippedReasons.isEmpty()) {
            System.out.println("\n====== 跳过详情 ======");
            for (String reason : skippedReasons) {
                System.out.println(" " + reason);
            }
        }

        if (summary.getFailures().isEmpty()) {
            System.out.println("所有自动化测试通过");
        }
        System.out.flush();
    }

    /** 记录被跳过的测试及其原因 */
    static class SkippedTrackingListener implements TestExecutionListener {
        private final List<String> skippedReasons = new ArrayList<>();

        @Override
        public void executionSkipped(TestIdentifier testIdentifier, String reason) {
            if (reason != null && !reason.isEmpty()) {
                skippedReasons.add(reason);
            } else {
                skippedReasons.add("跳过: [" + testIdentifier.getDisplayName() + "] 未知原因");
            }
        }

        List<String> getSkippedReasons() {
            return skippedReasons;
        }
    }
}