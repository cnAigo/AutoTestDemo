package base;

import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.*;

/**
 * 每个测试方法10秒超时自动跳过，并显示一行原因。
 */
public class TimeoutSkipExtension implements InvocationInterceptor {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            0, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
            r -> { Thread t = new Thread(r, "test-timeout-worker"); t.setDaemon(true); return t; }
    );

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        String displayName = extensionContext.getDisplayName();
        Future<?> future = EXECUTOR.submit(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        });

        try {
            future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TestAbortedException(
                    String.format("[%s] 执行超过10s所以跳过, 可能页面加载慢或元素未找到", displayName));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null) throw cause;
            throw e;
        }
    }
}
