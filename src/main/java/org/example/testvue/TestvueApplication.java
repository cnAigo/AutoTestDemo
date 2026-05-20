package org.example.testvue;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

@SpringBootApplication
public class TestvueApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestvueApplication.class, args);

        System.out.println("\n====== 正在初始化自动化测试引擎 (JUnit Launcher) ======");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("cases"))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        System.out.println("====== 正在自动加载并执行所有测试用例 ======\n");
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        printSummary(summary);
    }

    private static void printSummary(TestExecutionSummary summary) {
        System.out.println("\n====== 测试执行总结 ======");
        System.out.println("总测试数: " + summary.getTestsFoundCount());
        System.out.println("执行失败: " + summary.getTestsFailedCount());
        System.out.println("被跳过/中断: " + (summary.getTestsSkippedCount() + summary.getTestsAbortedCount()));
        System.out.println("执行成功: " + summary.getTestsSucceededCount());

        if (!summary.getFailures().isEmpty()) {
            System.out.println("\n====== 失败详情 ======");
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                System.err.println(" 失败位置: " + failure.getTestIdentifier().getDisplayName());
                System.err.println(" 错误原因: " + failure.getException().getMessage());
                failure.getException().printStackTrace();
            }
        } else {
            System.out.println("所有自动化测试通过");
        }
    }
}
