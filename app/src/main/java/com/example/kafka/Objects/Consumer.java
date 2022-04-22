package com.example.kafka.Objects;

import java.util.List;

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

    public static class ConsumersList {
        private String kind;
        private MetadataGroup metadata;
        private List<ConsumerObject> data;

        public ConsumersList(String kind, MetadataGroup metadata, List<ConsumerObject> data) {
            this.kind = kind;
            this.metadata = metadata;
            this.data = data;
        }

        public String getKind() {
            return kind;
        }

        public MetadataGroup getMetadata() {
            return metadata;
        }

        public List<ConsumerObject> getData() {
            return data;
        }
    }

    public static class ConsumerObject {
        private String kind;
        private MetadataConsumer metadata;
        private String cluster_id;
        private String consumer_group_id;
        private String consumer_id;
        private String instance_id;
        private String client_id;
        private Assignments assignments;

        public ConsumerObject(String kind, MetadataConsumer metadata, String cluster_id,
                              String consumer_group_id, String consumer_id, String instance_id,
                              String client_id, Assignments assignments) {
            this.kind = kind;
            this.metadata = metadata;
            this.cluster_id = cluster_id;
            this.consumer_group_id = consumer_group_id;
            this.consumer_id = consumer_id;
            this.instance_id = instance_id;
            this.client_id = client_id;
            this.assignments = assignments;
        }

        public String getKind() {
            return kind;
        }

        public MetadataConsumer getMetadata() {
            return metadata;
        }

        public String getCluster_id() {
            return cluster_id;
        }

        public String getConsumer_group_id() {
            return consumer_group_id;
        }

        public String getConsumer_id() {
            return consumer_id;
        }

        public String getInstance_id() {
            return instance_id;
        }

        public String getClient_id() {
            return client_id;
        }

        public Assignments getAssignments() {
            return assignments;
        }
    }

    public static class MetadataGroup {
        private String kind;
        private String next;

        public MetadataGroup(String kind, String next) {
            this.kind = kind;
            this.next = next;
        }

        public String getKind() {
            return kind;
        }

        public String getNext() {
            return next;
        }
    }

    public static class MetadataConsumer {
        private String self;
        private String resource_name;

        public MetadataConsumer(String self, String resource_name) {
            this.self = self;
            this.resource_name = resource_name;
        }

        public String getSelf() {
            return self;
        }

        public String getResource_name() {
            return resource_name;
        }
    }

    public static class Assignments {
        private String related;

        public Assignments(String related) {
            this.related = related;
        }

        public String getRelated() {
            return related;
        }
    }
}
