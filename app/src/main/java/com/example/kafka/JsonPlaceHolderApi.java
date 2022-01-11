package com.example.kafka;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface JsonPlaceHolderApi {
    String uploadImagesTopic = "images";
    String classificationResultTopic = "classificationResult";
    String trainCNNTopic = "trainCNN";
    String groupName = "";
    String instance = "";


    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/" + uploadImagesTopic)
    Call<Records> uploadImage(@Body Records records);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/subscription")
    Call<Subscription> subscribe(@Body Subscription subscription);

    @DELETE("consumers/" + groupName + "/instances/" + instance + "/subscription")
    Call<Response> unsubscribe();

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("consumers/" + groupName + "/instances/" + instance + "/assignments")
    Call<Partitions> assignPartition(@Body Partitions partitions);

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @GET("consumers/" + groupName + "/instances/" + instance + "/records")
    Call<Response> fetchDataFromTopic();
}
