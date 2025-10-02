package metatest.runner;

public class TestContextManager {
    private static final ThreadLocal<TestContext> contextHolder = new ThreadLocal<>();

    private TestContextManager() {
    }

    public static void setContext(TestContext context) {
        contextHolder.set(context);
    }

    public static TestContext getContext() {
        TestContext context = contextHolder.get();
        if (context == null) {
            throw new IllegalStateException("TestContext is not initialized for the current thread. " +
                    "Ensure the test execution is wrapped by the Metatest aspect.");
        }
        return context;
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
