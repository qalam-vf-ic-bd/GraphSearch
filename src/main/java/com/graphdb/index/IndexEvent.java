package com.graphdb.index;

import com.graphdb.utils.EventType;

import java.util.Collections;
import java.util.Map;

/**
 * Created by mishkat on 8/21/17.
 *
 * @author mishkat, Ashraful Islam
 */
public class IndexEvent {
    private EventType eventType;
    private String id;
    private String type;
    private Map<String, Object> keyValues;

    public void set(EventType eventType, String id, String type, Map<String, Object> keyValues) {
        this.eventType = eventType;
        this.id = id;
        this.type = type;
        this.keyValues = keyValues;
    }

    public void set(EventType eventType) {
        set(eventType, null, null, Collections.emptyMap());
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "IndexEvent{" +
                "eventType=" + eventType +
                ", id=" + id +
                ", type='" + type + '\'' +
                ", keyValues=" + keyValues +
                '}';
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Map<String, Object> keyValues) {
        this.keyValues = keyValues;
    }

}