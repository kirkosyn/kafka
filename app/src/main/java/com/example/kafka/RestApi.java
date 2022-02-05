package com.example.kafka;

import com.example.kafka.Objects.ClassificationResult;
import com.example.kafka.Objects.Consumer;
import com.example.kafka.Objects.Partitions;
import com.example.kafka.Objects.RecordsPosted;
import com.example.kafka.Objects.RecordsToPost;
import com.example.kafka.Objects.Subscription;

import java.util.List;

import retrofit2.Call;
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
    String partitionId = "0";

    static String getUploadImagesTopic() {
        return uploadImagesTopic;
    }

    static String getClassificationResultTopic() {
        return classificationResultTopic;
    }

    static String getTrainCNNTopic() {
        return trainCNNTopic;
    }

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/" + uploadImagesTopic + "/partitions/" + partitionId)
    Call<RecordsPosted> postData(@Body RecordsToPost records);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/" + trainCNNTopic)
    Call<RecordsPosted> trainCNN(@Body RecordsToPost records);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName)
    Call<Consumer.CreatedConsumer> createCustomer(@Body Consumer.ConsumerToCreate consumer);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/subscription")
    Call<String> subscribe(@Body Subscription subscription);

    @DELETE("consumers/" + groupName + "/instances/" + instance)
    Call<String> destroyConsumerInstance();

    @DELETE("consumers/" + groupName + "/instances/" + instance + "/subscription")
    Call<String> unsubscribe();

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/assignments")
    Call<String> assignPartition(@Body Partitions partitions);

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

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/positions/beginning")
    Call<String> seekToFirstOffset(@Body Partitions partitions);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/positions/end")
    Call<String> seekToLastOffset(@Body Partitions partitions);
}
