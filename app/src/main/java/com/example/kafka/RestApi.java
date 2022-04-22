package com.example.kafka;

import com.example.kafka.Objects.ClassificationResult;
import com.example.kafka.Objects.Consumer;
import com.example.kafka.Objects.Partitions;
import com.example.kafka.Objects.RecordsPosted;
import com.example.kafka.Objects.RecordsToPost;
import com.example.kafka.Objects.Subscription;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RestApi {
    enum GroupNamesEnum {
        GROUP_1,
        GROUP_2,
        GROUP_3,
        GROUP_4,
        GROUP_5,
        GROUP_6,
        GROUP_7,
        GROUP_8,
        GROUP_9,
        GROUP_10
    }

    String uploadImagesTopic = "images";
    String classificationResultTopic = "classificationResult";
    String trainCNNTopic = "trainCNN";
    String groupName = generateGroupName();
    String instance = generateUUID();
    String clusterId = "1K-BtNAuQ-CniFE38E2WjA";
    String consumerGroupId = "testGroup";
//    String partitionId = "0";

    static String generateUUID() {
        final String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    static String generateGroupName() {
        Random rnd = new Random();
        List<GroupNamesEnum> groupNames = Collections.unmodifiableList(Arrays.asList(GroupNamesEnum.values()));
        int size = groupNames.size();
        return groupNames.get(rnd.nextInt(size)).toString();
    }

    static String getGroupName() {
        return groupName;
    }

    static String getInstance() {
        return instance;
    }

    static String getConsumerGroupId() {
        return consumerGroupId;
    }

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
    @POST("topics/" + uploadImagesTopic + "/partitions/{partitionId}")
    Call<RecordsPosted> postData(@Body RecordsToPost records, @Path("partitionId") String partitionId);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/" + trainCNNTopic)
    Call<RecordsPosted> trainCNN(@Body RecordsToPost records);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/{groupName}")
    Call<Consumer.CreatedConsumer> createCustomer(@Body Consumer.ConsumerToCreate consumer, @Path("groupName") String groupName);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/{groupName}/instances/{instance}/subscription")
    Call<String> subscribe(@Body Subscription subscription, @Path("groupName") String groupName, @Path("instance") String instance);

    @DELETE("consumers/{groupName}/instances/{instance}")
    Call<String> destroyConsumerInstance(@Path("groupName") String groupName, @Path("instance") String instance);

    @DELETE("consumers/{groupName}/instances/{instance}/subscription")
    Call<String> unsubscribe(@Path("groupName") String groupName, @Path("instance") String instance);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/{groupName}/instances/{instance}/assignments")
    Call<String> assignPartition(@Body Partitions partitions, @Path("groupName") String groupName, @Path("instance") String instance);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @GET("consumers/{groupName}/instances/{instance}/assignments")
    Call<Partitions> getPartitions(@Path("groupName") String groupName, @Path("instance") String instance);

    @Headers({
            "Accept:application/vnd.kafka.json.v2+json"
    })
    @GET("consumers/{groupName}/instances/{instance}/records?timeout=3000")
    Call<List<ClassificationResult>> fetchDataFromTopic(@Path("groupName") String groupName, @Path("instance") String instance);

    @Headers({
            "Accept:application/vnd.kafka.json.v2+json"
    })
    @GET("clusters/" + clusterId + "/consumer-groups")
    Call<String> getListConsumerGroups();

    @Headers({
            "Accept:application/vnd.kafka.json.v2+json"
    })
    @GET("clusters/" + clusterId + "/consumer-groups/{consumerGroupId}")
    Call<String> getConsumerGroup(@Path("consumerGroupId") String consumerGroupId);

    @Headers({
            "Accept:application/json"
    })
    @GET("v3/clusters/" + clusterId + "/consumer-groups/{consumerGroupId}/consumers")
    Call<Consumer.ConsumersList> getListConsumers(@Path("consumerGroupId") String consumerGroupId);

    @Headers({
            "Accept:application/json"
    })
    @GET("v3/clusters/" + clusterId + "/consumer-groups/{consumerGroupId}/consumers/{instance}")
    Call<String> getConsumer(@Path("consumerGroupId") String consumerGroupId, @Path("instance") String instance);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/{groupName}/instances/{instance}/positions/beginning")
    Call<String> seekToFirstOffset(@Body Partitions partitions, @Path("groupName") String groupName, @Path("instance") String instance);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/{groupName}/instances/{instance}/positions/end")
    Call<String> seekToLastOffset(@Body Partitions partitions, @Path("groupName") String groupName, @Path("instance") String instance);
}
