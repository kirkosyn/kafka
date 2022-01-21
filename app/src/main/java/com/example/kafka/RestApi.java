package com.example.kafka;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RestApi {
    String uploadImagesTopic = "images";
    String classificationResultTopic = "classificationResult";
    String trainCNNTopic = "trainCNN";
    String groupName = "testGroup";
    String instance = "testConsumer";
    String clusterId = "1K-BtNAuQ-CniFE38E2WjA";
    String consumerGroupId = "testGroup";

    static String getUploadImagesTopic() {
        return uploadImagesTopic;
    }

    static String getClassificationResultTopic() {
        return classificationResultTopic;
    }

    static String getTrainCNNTopic() {
        return uploadImagesTopic;
    }

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/" + uploadImagesTopic)
    Call<Records> uploadImage(@Body Records records);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/" + trainCNNTopic)
    Call<Records> trainCNN(@Body Records records);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName)
    Call<Consumer> createCustomer(@Body Consumer consumer);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/subscription")
    Call<Subscription> subscribe(@Body Subscription subscription);

    @DELETE("consumers/" + groupName + "/instances/" + instance)
    Call<String> destroyConsumerInstance();

    @DELETE("consumers/" + groupName + "/instances/" + instance + "/subscription")
    Call<String> unsubscribe();

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/assignments")
    Call<Partitions> assignPartition(@Body Partitions partitions);

    @Headers({
            "Accept:application/vnd.kafka.json.v2+json"
    })
    @GET("consumers/" + groupName + "/instances/" + instance + "/records?timeout=3000")
    Call<List<ClassificationResult>> fetchDataFromTopic();

    @Headers({
            "Accept:application/vnd.kafka.json.v2+json"
    })
    @GET("clusters/" + clusterId + "/consumer-groups")
    Call<String> getListConsumerGroups();

    @Headers({
            "Accept:application/vnd.kafka.json.v2+json"
    })
    @GET("clusters/" + clusterId + "/consumer-groups/" + consumerGroupId)
    Call<String> getConsumerGroup();

    @Headers({
            "Accept:application/json"
    })
    @GET("v3/clusters/" + clusterId + "/consumer-groups/" + consumerGroupId + "/consumers")
    Call<String> getListConsumers();

    @Headers({
            "Accept:application/json"
    })
    @GET("v3/clusters/" + clusterId + "/consumer-groups/" + consumerGroupId + "/consumers/" + instance)
    Call<String> getConsumer();
}
