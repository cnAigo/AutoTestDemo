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
        // 1. 正常启动 Spring 环境（如果有后端服务的话）
        SpringApplication.run(TestvueApplication.class, args);

        System.out.println("\n====== 正在初始化自动化测试引擎 (JUnit Launcher) ======");

        // 2. 配置测试发现请求：自动扫描 "cases" 包下的所有测试类
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("cases"))
                .build();

        Launcher launcher = LauncherFactory.create();

        // 3. 注册一个监听器来收集测试结果，这样你能在控制台看到总结
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        // 4. 正式执行测试
        System.out.println("====== 正在自动加载并执行所有测试用例 ======\n");
        launcher.execute(request);

        // 5. 打印测试结果总结
        TestExecutionSummary summary = listener.getSummary();
        System.out.println("\n ====== 测试执行总结 ======");
        System.out.println("总测试数: " + summary.getTestsFoundCount());

        System.out.println("执行失败: " + summary.getTestsFailedCount());

        System.out.println("被跳过/中断: " + (summary.getTestsSkippedCount() + summary.getTestsAbortedCount()));
        System.out.println("执行成功: " + summary.getTestsSucceededCount());
        if (!summary.getFailures().isEmpty()) {
            System.out.println("\n ====== 致命错误详情 ======");
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                System.err.println(" 失败位置: " + failure.getTestIdentifier().getDisplayName());
                System.err.println(" 错误原因: " + failure.getException().getMessage());
                failure.getException().printStackTrace(); // 打印完整的红色报错堆栈
            }
        } else {
            System.out.println("所有自动化测试通过");
        }
    }
}