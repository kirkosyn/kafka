package com.example.kafka;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface JsonPlaceHolderApi {

    @GET("topics")
    Call<List<String>> getTopics();

    @Headers({
            "Content-Type:application/vnd.kafka.json.v2+json"
    })
    @POST("topics/images")
    Call<Records> uploadImage(@Body Records records);
//    Call<ResponseBody> uploadImage(@Part("name") RequestBody fullName, @Part MultipartBody.Part image);

}
