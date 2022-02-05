package com.example.kafka.Objects;

import java.util.List;

public class Partitions {
    private List<Partition> partitions;

    public Partitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public static class Partition {
        private String topic;
        private int partition;

        public Partition(String topic, int partition) {
            this.topic = topic;
            this.partition = partition;
        }
    }
}
