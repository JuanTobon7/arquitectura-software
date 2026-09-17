package com.pipesfilters.dtos;

public record FileFrame(
        String name,
        byte[] content
) {
    public long size() {
        return content.length;
    }
    public static final FileFrame END = new FileFrame(null, null);
}