package com.example.kafka;

import java.util.List;

public class Subscription {
    private List<String> topics;

    public Subscription(List<String> topics) {
        this.topics = topics;
    }
}
