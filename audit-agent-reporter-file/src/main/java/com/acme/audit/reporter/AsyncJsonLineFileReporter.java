package com.acme.audit.reporter;

import com.acme.audit.api.*;import com.fasterxml.jackson.databind.ObjectMapper;import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicLong;

/** Bounded, non-blocking JSON Lines reporter. Business threads only call queue.offer. */
public final class AsyncJsonLineFileReporter implements AuditEventReporter {
    private final BlockingQueue<AuditEvent> queue;private final int batchSize;private final long lingerMs;private final Path file;private final ObjectMapper json=new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    private final AtomicLong sent=new AtomicLong(),failed=new AtomicLong(),dropped=new AtomicLong();private final Thread worker;private volatile boolean running=true,healthy=true;
    public AsyncJsonLineFileReporter(Path file,int capacity,int batchSize,long lingerMs)throws IOException{this.file=file;this.queue=new ArrayBlockingQueue<AuditEvent>(Math.max(10,capacity));this.batchSize=Math.max(1,batchSize);this.lingerMs=Math.max(10,lingerMs);Path parent=file.toAbsolutePath().getParent();if(parent!=null)Files.createDirectories(parent);worker=new Thread(new Runnable(){public void run(){loop();}},"sensitive-audit-file-reporter");worker.setDaemon(true);worker.start();}
    @Override public void report(List<AuditEvent> events){for(AuditEvent event:events)if(!queue.offer(event))dropped.incrementAndGet();}
    private void loop(){List<AuditEvent> batch=new ArrayList<AuditEvent>(batchSize);while(running||!queue.isEmpty()){try{AuditEvent first=queue.poll(lingerMs,TimeUnit.MILLISECONDS);if(first==null)continue;batch.add(first);queue.drainTo(batch,batchSize-1);append(batch);sent.addAndGet(batch.size());healthy=true;}catch(InterruptedException e){if(!running)Thread.currentThread().interrupt();}catch(Throwable e){failed.addAndGet(Math.max(1,batch.size()));healthy=false;System.err.println("[sensitive-audit-agent] reporter failure: "+e);}finally{batch.clear();}}}
    private void append(List<AuditEvent> batch)throws IOException{BufferedWriter out=Files.newBufferedWriter(file,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);try{for(AuditEvent event:batch){out.write(json.writeValueAsString(event));out.newLine();}}finally{out.close();}}
    @Override public ReporterHealth health(){return new ReporterHealth(healthy,queue.size(),sent.get(),failed.get(),dropped.get());}
    @Override public void close(){running=false;worker.interrupt();try{worker.join(5000);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
}
