package org.example.node.locks;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;
import com.orbitz.consul.option.PutOptions;
import com.google.common.net.HostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class ConsulLockService {

    private final KeyValueClient kvClient;
    private final SessionClient sessionClient;

    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();
    private static final int DEFAULT_RETRY_MS = 500;

    public ConsulLockService(
            @Value("${spring.cloud.consul.host}") String consulHost,
            @Value("${spring.cloud.consul.port}") int consulPort) {

        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromParts(consulHost, consulPort))
                .build();

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
            throw new IllegalStateException("Failed to acquire lock for key: " + key, e);
        }
    }

    public void releaseWriteLock(String key) {
        String sessionId = sessionMap.remove(key);
        if (sessionId != null) {
            kvClient.releaseLock(key, sessionId);
            sessionClient.destroySession(sessionId);
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
                .ttl("10s")
                .build();
        return sessionClient.createSession(session).getId();
    }
}
