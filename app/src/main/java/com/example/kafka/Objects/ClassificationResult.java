package com.example.kafka.Objects;

public class ClassificationResult {
    private Object topic;
    private Key key;
    private Value value;
    private Object partition;
    private Object offset;

    public Object getTopic() {
        return topic;
    }

    public Key getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }

    public Object getPartition() {
        return partition;
    }

    public Object getOffset() {
        return offset;
    }

    public static class Value {
        private String label;

        public Value(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class Key {
        private String id;

        public Key(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
