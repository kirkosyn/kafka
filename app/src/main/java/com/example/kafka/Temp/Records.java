package com.example.kafka.Temp;

import java.util.List;

public class Records {

    public static class RecordsToPost {
        //        private Object value_schema_id;
//        private Object key_schema_id;
        private List<Record> records;

        public RecordsToPost(List<Record> records) {
//            this.value_schema_id = 1;
//            this.key_schema_id = 3;
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
            String name;
            String image;

            public Value(String name, String image) {
                this.name = name;
                this.image = image;
            }
        }
    }

    public static class RecordsPosted {
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

}

//
//    List<Record> records;
//
//    public Records(List<Record> records) {
//        this.records = records;
//    }
//
//    public static class Record {
//        Key key;
//        Value value;
//
//        public Record(Key key, Value value) {
//            this.key = key;
//            this.value = value;
//        }
//
//    }
//
//    public static class Key {
//        private int key_schema_id;
//        private Data data;
//
//        public Key(int key_schema_id, Data data) {
//            this.key_schema_id = key_schema_id;
//            this.data = data;
//        }
//
//        public static class Data {
//            int id;
//
//            public Data(int id) {
//                this.id = id;
//            }
//        }
//    }
//
//    public static class Value {
//        private int value_schema_id;
//        Data data;
//
//        public Value(int value_schema_id, Data data) {
//            this.value_schema_id = value_schema_id;
//            this.data = data;
//        }
//
//        public static class Data {
//            String image;
//            String name;
//
//            public Data(String image, String name) {
//                this.image = image;
//                this.name = name;
//            }
//        }
//    }

////////////////////////

//    private Object value_schema_id;
//    private Object key_schema_id;
////    private Object value_schema;
//    private List<Record> records;
//
//    public Records(List<Record> records) {
//        this.value_schema_id = 1;
//        this.key_schema_id = 3;
////        this.value_schema = "{\"type\": \"object\", " +
////                            "\"properties\": {\"name\": {\"type\": \"string\"}, " +
////                            "\"image\": {\"type\": \"string\"}}}";
//        this.records = records;
//    }
//
//    public List<Record> getRecord() {
//        return records;
//    }
//
//    public static class Record {
//        private int key;
//        private Value value;
//
//        public Record(int key, Value value) {
//            this.key = key;
//            this.value = value;
//        }
//    }
//
//    public static class Value {
//        String name;
//        String image;
//
//        public Value(String name, String image) {
//            this.name = name;
//            this.image = image;
//        }
//    }

/////////////////////

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