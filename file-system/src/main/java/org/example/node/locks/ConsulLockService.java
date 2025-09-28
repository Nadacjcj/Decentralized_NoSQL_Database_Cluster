package org.example.node.locks;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;
import com.google.common.net.HostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class ConsulLockService {

    public static final String $_SPRING_CLOUD_CONSUL_HOST = "${spring.cloud.consul.host}";
    public static final String $_SPRING_CLOUD_CONSUL_PORT = "${spring.cloud.consul.port}";
    public static final String FAILED_TO_ACQUIRE_LOCK_FOR_KEY = "Failed to acquire lock for key: ";
    private final KeyValueClient kvClient;
    private final SessionClient sessionClient;
    private final Map<String, String> sessionMap;
    private static final int DEFAULT_RETRY_MS = 500;

    public ConsulLockService(
            @Value($_SPRING_CLOUD_CONSUL_HOST) String consulHost,
            @Value($_SPRING_CLOUD_CONSUL_PORT) int consulPort) {

        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                .build();

        sessionMap = new ConcurrentHashMap<>();
        this.kvClient = consul.keyValueClient();
        this.sessionClient = consul.sessionClient();
    }

    public boolean acquireWriteLock(String key) {
        try {
            String sessionId = createConsulSession(key);
            boolean acquired = kvClient.acquireLock(key, sessionId);
            if (acquired) {
                sessionMap.put(key, sessionId);
            } else {
                sessionClient.destroySession(sessionId);
            }
            return acquired;
        } catch (Exception e) {
            throw new IllegalStateException(FAILED_TO_ACQUIRE_LOCK_FOR_KEY + key, e);
        }
    }

    public void releaseWriteLock(String key) {
        String sessionId = sessionMap.remove(key);
        if (sessionId != null) {
            try {
                kvClient.releaseLock(key, sessionId);
            } finally {
                sessionClient.destroySession(sessionId);
            }
        }
    }

    public boolean tryAcquireWithRetry(Supplier<Boolean> lockSupplier, int maxRetries) {
        int retries = 0;
        while (retries < maxRetries) {
            if (lockSupplier.get()) return true;
            retries++;
            try {
                TimeUnit.MILLISECONDS.sleep(DEFAULT_RETRY_MS);
            } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private String createConsulSession(String key) {
        Session session = ImmutableSession.builder()
                .name("lock-session-" + key)
                .ttl("15s")
                .behavior("delete")
                .build();
        return sessionClient.createSession(session).getId();
    }
}
