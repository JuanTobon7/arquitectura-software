package com.plugandplay.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProcessingContext {

    private final String name;
    private byte[] payload;
    private final Map<String, Object> metadata;

    public ProcessingContext(String name, byte[] payload) {
        this.name = name;
        this.payload = payload;
        this.metadata = new ConcurrentHashMap<>();
        this.metadata.put("originalSize", (long) payload.length);
    }

    public String name() {
        return name;
    }

    public byte[] payload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long originalSize() {
        return (long) metadata.get("originalSize");
    }

    public <T> Optional<T> getMetadata(String key, Class<T> type) {
        return Optional.ofNullable(metadata.get(key)).map(type::cast);
    }

    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Copia defensiva: cada rama paralela trabaja sobre su propia copia.
     */
    public ProcessingContext copy() {
        ProcessingContext copy = new ProcessingContext(this.name, this.payload.clone());
        copy.metadata.putAll(this.metadata);
        return copy;
    }

    /**
     * Vuelca la metadata generada por otra rama sobre este contexto.
     */
    public void mergeMetadataFrom(ProcessingContext other) {
        this.metadata.putAll(other.metadata);
        String otherPlugin = (String) other.metadata.get("appliedPlugin");
        if (otherPlugin != null) {
            String existing = (String) this.metadata.get("appliedPlugins");
            if (existing == null || existing.isBlank()) {
                this.metadata.put("appliedPlugins", otherPlugin);
            } else if (!existing.contains(otherPlugin)) {
                this.metadata.put("appliedPlugins", existing + "," + otherPlugin);
            }
        }
    }
}
