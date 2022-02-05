package com.example.kafka.Objects;

import java.util.List;

public class RecordsToPost {
//    private Object value_schema_id;
//    private Object key_schema_id;
    private List<Record> records;

    public RecordsToPost(List<Record> records) {
//        this.value_schema_id = 1;
//        this.key_schema_id = 3;
        this.records = records;
    }

    public static class Record {
        private Key key;
        private Value value;

        public Record(Key key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class Key {
        String id;

        public Key(String id) {
            this.id = id;
        }
    }

    public static class Value {
        String label;
        String image;

        public Value(String label, String image) {
            this.label = label;
            this.image = image;
        }
    }
}
