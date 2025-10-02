package metatest.aop;

import metatest.config.SimulatorConfig;
import metatest.http.HTTPFactory;
import metatest.http.Request;
import metatest.http.Response;
import metatest.runner.Runner;
import metatest.runner.TestContext;
import metatest.runner.TestContextManager;
import metatest.schemacoverage.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

@Aspect
public class AspectExecutor {

    @Around("execution(@org.junit.jupiter.api.Test * *(..))")
    public Object interceptTestMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        TestContextManager.setContext(new TestContext());
        Object originalTestResult;

        try {
            System.out.println("Intercepting test method: " + joinPoint.getSignature().getName());
            System.out.println("Executing original test run to capture baseline...");

            originalTestResult = joinPoint.proceed();

            TestContext context = TestContextManager.getContext();

            if (context.getOriginalResponse() == null) {
                System.out.println("No interceptable HTTP response was captured. Skipping fault simulation for this test.");
                return originalTestResult;
            }
            System.out.println("Original test run completed. Baseline response captured.");

            String testName = joinPoint.getSignature().getName();
            String endpointUrl = context.getOriginalRequest() != null ? context.getOriginalRequest().getUrl() : "";

            if (!SimulatorConfig.isTestExcluded(testName) && !SimulatorConfig.isEndpointExcluded(endpointUrl)) {
                Runner.executeTestWithSimulatedFaults(joinPoint, context);
            } else {
                System.out.println("Skipping fault simulation for this test due to exclusion rules.");
            }

        } finally {
            // Clean up the context for the current thread to prevent memory leaks
            // and state bleeding between tests in the same thread.
            TestContextManager.clearContext();
            System.out.println("Test method execution finished: " + joinPoint.getSignature().getName());
        }

        return originalTestResult;
    }

    @Around("execution(* org.apache.http.impl.client.CloseableHttpClient.execute(..))")
    public Object interceptApacheHttpClient(ProceedingJoinPoint joinPoint) throws Throwable {
        TestContext context = TestContextManager.getContext();
        Object[] args = joinPoint.getArgs();

        if (args.length > 0 && args[0] instanceof HttpRequestBase) {
            HttpRequestBase httpRequest = (HttpRequestBase) args[0];

            if (context.getOriginalResponse() == null) {
                Request requestWrapper = HTTPFactory.createRequestFrom(httpRequest);
                context.setOriginalRequest(requestWrapper);
                Logger.parseResponse(httpRequest);
                System.out.println("Original request captured: " + requestWrapper.getUrl());
            }
        }

        // Proceed with the actual HTTP call.
        Object result = joinPoint.proceed(args);

        // Response Interception
        if (result instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) result;

            // If it's the first run, capture the response.
            if (context.getOriginalResponse() == null) {
                Response responseWrapper = HTTPFactory.createResponseFrom(httpResponse);
                context.setOriginalResponse(responseWrapper);
                System.out.println("Original response captured.");


                httpResponse.setEntity(new StringEntity(responseWrapper.getBody()));

            } else if (context.getSimulatedResponse() != null) {
                // If it's a rerun for fault simulation, inject the faulty body.
                String simulatedBody = context.getSimulatedResponse().getBody();
                httpResponse.setEntity(new StringEntity(simulatedBody));
                System.out.printf("    [RESPONSE-INJECTION] Injecting simulated response: %s%n", simulatedBody);
            }
        }

        return result;
    }


    @Around("execution(* okhttp3.Call.execute(..))")
    public Object interceptOkHttpClient(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Intercepted OkHttpClient call (placeholder, no fault injection)");
        Object result = joinPoint.proceed();

        if (result instanceof okhttp3.Response) {
            okhttp3.Response response = (okhttp3.Response) result;
            String originalResponse = response.peekBody(Long.MAX_VALUE).string();
            System.out.println("Response peeked for OkHttpClient: " + originalResponse.substring(0, Math.min(originalResponse.length(), 150)) + "...");
        }

        return result;
    }


    @Around("execution(* java.net.HttpURLConnection.connect(..))")
    public Object interceptHttpURLConnection(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Intercepted HttpURLConnection call (placeholder, no fault injection)");
        Object result = joinPoint.proceed();
        HttpURLConnection connection = (HttpURLConnection) joinPoint.getTarget();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        } catch (Exception e) {
        }

        return result;
    }
}