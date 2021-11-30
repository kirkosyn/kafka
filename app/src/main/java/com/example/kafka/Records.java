package com.example.kafka;

import java.util.List;

public class Records {
    private Object value_schema_id;
    private Object key_schema_id;
//    private Object value_schema;
    private List<Record> records;

    public Records(List<Record> records) {
        this.value_schema_id = 1;
        this.key_schema_id = 3;
//        this.value_schema = "{\"type\": \"object\", " +
//                            "\"properties\": {\"name\": {\"type\": \"string\"}, " +
//                            "\"image\": {\"type\": \"string\"}}}";
        this.records = records;
    }

    public List<Record> getRecord() {
        return records;
    }

    public static class Record {
        private int key;
        private Value value;

        public Record(int key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class Value {
        String name;
        String image;

        public Value(String name, String image) {
            this.name = name;
            this.image = image;
        }
    }

//    public Object getValue_schema() {
//        return value_schema;
//    }
//    public Object getValue_schema_id() {
//        return value_schema_id;
//    }

//    public List<Record> getRecords() {
//        return records;
//    }
}



//    Schema schema;
//    Payload payload;
//
//    public static class Schema {
//        Object type = "object";
//        List<Field> fields;
//        Object optional = "false";
//        Object name = "SampleRecord";
//
//        public Schema(List<Field> fields) {
//            this.fields = fields;
//        }
//    }
//
//    public static class Field {
//        Object type = "string";
//        Object optional = "false";
//        Object field;
//
//        public Field(Object field) {
//            this.field = field;
//        }
//    }
//
//    public static class Payload {
//        Object name;
//        Object image;
//
//        public Payload(Object name, Object image) {
//            this.name = name;
//            this.image = image;
//        }
//    }
//
//    public void setSchema(Schema schema) {
//        this.schema = schema;
//    }
//
//    public void setPayload(Payload payload) {
//        this.payload = payload;
//    }