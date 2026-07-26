package com.acme.audit.reporter;

import com.acme.audit.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Small JDK-only HTTP reporter.  Application threads only enqueue events; the
 * worker performs bounded JSON batches and therefore remains fail-open.
 */
public final class HttpJsonBatchReporter implements AuditEventReporter {
    private final BlockingQueue<AuditEvent> queue;
    private final ObjectMapper mapper = new ObjectMapper();
    private final URL endpoint;
    private final String token;
    private final int batchSize, connectTimeoutMs, readTimeoutMs;
    private final long lingerMs;
    private final AtomicLong sent = new AtomicLong(), failed = new AtomicLong(), dropped = new AtomicLong();
    private final Thread worker;
    private volatile boolean running = true, healthy = true;

    public HttpJsonBatchReporter(URL endpoint, String token, int capacity, int batchSize,
                                 long lingerMs, int connectTimeoutMs, int readTimeoutMs) {
        if (endpoint == null) throw new IllegalArgumentException("endpoint is required");
        this.endpoint = endpoint; this.token = token;
        this.queue = new ArrayBlockingQueue<AuditEvent>(Math.max(10, capacity));
        this.batchSize = Math.max(1, batchSize); this.lingerMs = Math.max(10, lingerMs);
        this.connectTimeoutMs = Math.max(100, connectTimeoutMs); this.readTimeoutMs = Math.max(100, readTimeoutMs);
        this.worker = new Thread(new Runnable() { public void run() { loop(); } }, "sensitive-audit-http-reporter");
        this.worker.setDaemon(true); this.worker.start();
    }

    public void report(List<AuditEvent> events) {
        if (events == null) return;
        for (AuditEvent event : events) if (event != null && !queue.offer(event)) dropped.incrementAndGet();
    }

    private void loop() {
        List<AuditEvent> batch = new ArrayList<AuditEvent>(batchSize);
        while (running || !queue.isEmpty()) {
            try {
                AuditEvent first = queue.poll(lingerMs, TimeUnit.MILLISECONDS);
                if (first == null) continue;
                batch.add(first); queue.drainTo(batch, batchSize - 1);
                post(batch); sent.addAndGet(batch.size()); healthy = true;
            } catch (InterruptedException e) {
                if (!running) Thread.currentThread().interrupt();
            } catch (Throwable e) {
                failed.addAndGet(Math.max(1, batch.size())); healthy = false;
                System.err.println("[sensitive-audit-agent] HTTP reporter failure: " + e);
            } finally { batch.clear(); }
        }
    }

    private void post(List<AuditEvent> events) throws Exception {
        HttpURLConnection c = (HttpURLConnection) endpoint.openConnection();
        c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(connectTimeoutMs); c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Content-Type", "application/json");
        if (token != null && !token.trim().isEmpty()) c.setRequestProperty("Authorization", "Bearer " + token.trim());
        byte[] body = mapper.writeValueAsString(events).getBytes(StandardCharsets.UTF_8);
        c.setFixedLengthStreamingMode(body.length);
        OutputStream out = c.getOutputStream(); try { out.write(body); } finally { out.close(); }
        int code = c.getResponseCode(); c.disconnect();
        if (code < 200 || code >= 300) throw new IllegalStateException("HTTP status " + code);
    }

    public ReporterHealth health() { return new ReporterHealth(healthy, queue.size(), sent.get(), failed.get(), dropped.get()); }
    public void close() { running = false; worker.interrupt(); try { worker.join(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
