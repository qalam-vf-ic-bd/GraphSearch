package com.graphdb.index;

import com.graphdb.utils.EventType;
import com.graphdb.utils.Utils;
import com.lmax.disruptor.RingBuffer;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.graphdb.utils.Utils.shutdownAndAwaitTermination;

/**
 * Created by mishkat on 8/21/17.
 *
 * @author mishkat, Ashraful Islam
 */
public class IndexEventProducer {
    private final RingBuffer<IndexEvent> ringBuffer;
    private final ExecutorService service;

    public IndexEventProducer(RingBuffer<IndexEvent> ringBuffer, int maxThread) {
        this.ringBuffer = ringBuffer;
        this.service = Executors.newFixedThreadPool(maxThread);
    }

    public void sendDataAsync(String id, String type, Map<String, Object> keyValues) {
        if (!ringBuffer.tryPublishEvent(Utils.DATA_TRANSLATOR, id, type, keyValues)) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    ringBuffer.publishEvent(Utils.DATA_TRANSLATOR, id, type, keyValues);
                }
            });
        }
    }

    public void sendEventAsync(EventType eventType) {
        if (!ringBuffer.tryPublishEvent(Utils.EVENT_TRANSLATOR, eventType)) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    ringBuffer.publishEvent(Utils.EVENT_TRANSLATOR, eventType);
                }
            });
        }
    }

    public void sendDataSync(String id, String type, Map<String, Object> keyValues) {
        ringBuffer.publishEvent(Utils.DATA_TRANSLATOR, id, type, keyValues);
    }

    public void sendEventSync(EventType eventType) {
        ringBuffer.publishEvent(Utils.EVENT_TRANSLATOR, eventType);
    }

    public void stopService() {
        shutdownAndAwaitTermination(service);
    }
}
