package com.example.kafka.Objects;

import java.util.List;

public class RecordsPosted {
    private Object value_schema_id;
    private Object key_schema_id;
    private List<Offset> offsets;
//        private String cluster_id;
//        private String topic_name;
//        private int partition_id;
//        private int offset;
//        private String timestamp;
//        private Key key;
//        private Value value;

//        public RecordsPosted(String cluster_id, String topic_name, int partition_id, int offset, String timestamp, Key key, Value value) {
//            this.cluster_id = cluster_id;
//            this.topic_name = topic_name;
//            this.partition_id = partition_id;
//            this.offset = offset;
//            this.timestamp = timestamp;
//            this.key = key;
//            this.value = value;
//        }


    public Object getValue_schema_id() {
        return value_schema_id;
    }

    public Object getKey_schema_id() {
        return key_schema_id;
    }

    public List<Offset> getOffsets() {
        return offsets;
    }

    public RecordsPosted(Object value_schema_id, Object key_schema_id, List<Offset> offsets) {
        this.value_schema_id = value_schema_id;
        this.key_schema_id = key_schema_id;
        this.offsets = offsets;
    }

    public static class Offset {
        private int partition;
        private int offset;

        public Offset(int partition, int offset) {
            this.partition = partition;
            this.offset = offset;
        }

        public int getPartition() {
            return partition;
        }

        public int getOffset() {
            return offset;
        }
    }

    public static class Key {
        private String type;
        private int size;

        public Key(String type, int size) {
            this.type = type;
            this.size = size;
        }
    }

    public static class Value {
        private String type;
        private String subject;
        private int schema_id;
        private int schema_version;
        private int size;

        public Value(String type, String subject, int schema_id, int schema_version, int size) {
            this.type = type;
            this.subject = subject;
            this.schema_id = schema_id;
            this.schema_version = schema_version;
            this.size = size;
        }
    }
}
