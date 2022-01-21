package com.example.kafka;

public class Consumer {
    private String name;
    private String format;
//    private String auto_offset_reset;
//    private String auto_commit_enable;

    public Consumer(String name, String format, String auto_offset_reset, String auto_commit_enable) {
        this.name = name;
        this.format = format;
//        this.auto_offset_reset = auto_offset_reset;
//        this.auto_commit_enable = auto_commit_enable;
    }
}
