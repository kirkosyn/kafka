package com.example.kafka.Objects;

public class Consumer {
    public static class ConsumerToCreate {
        private String name;
        private String format;
//    private String auto_offset_reset;
//    private String auto_commit_enable;

        public ConsumerToCreate(String name, String format, String auto_offset_reset, String auto_commit_enable) {
            this.name = name;
            this.format = format;
//        this.auto_offset_reset = auto_offset_reset;
//        this.auto_commit_enable = auto_commit_enable;
        }

    }

    public static class CreatedConsumer {
        private String instance_id;
        private String base_uri;

        public CreatedConsumer(String instance_id, String base_uri) {
            this.instance_id = instance_id;
            this.base_uri = base_uri;
        }

        public String getInstance_id() {
            return instance_id;
        }

        public String getBase_uri() {
            return base_uri;
        }
    }
}
