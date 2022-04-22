package com.example.kafka;

import static java.util.Optional.ofNullable;

import com.example.kafka.Objects.Consumer;
import com.example.kafka.Objects.Partitions;
import com.example.kafka.Objects.Subscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestApiDefinitions {
    private java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
    private RestApi restApi;
    private int partitionId;

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public RestApiDefinitions(RestApi restApi) {
        this.restApi = restApi;
    }

    public void fetchConsumersList() {
        logger.info("FETCHING CONSUMERS LIST");
        Call<Consumer.ConsumersList> call = restApi.getListConsumers(RestApi.getConsumerGroupId());

        call.enqueue(new Callback<Consumer.ConsumersList>() {
            @Override
            public void onResponse(Call<Consumer.ConsumersList> call, Response<Consumer.ConsumersList> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());

                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("FETCHED CONSUMERS LIST");
                response.body().getData().stream().forEach(x -> logger.info(x.getAssignments().getRelated()));
                createConsumer();
            }

            @Override
            public void onFailure(Call<Consumer.ConsumersList> call, Throwable t) {
                logger.severe(t.getMessage());

            }
        });
    }

    public void createConsumer() {
        logger.info("CREATING CONSUMER");
        Consumer.ConsumerToCreate consumer = new Consumer.ConsumerToCreate(RestApi.getInstance(), "json", "earliest", "false");
        Call<Consumer.CreatedConsumer> call = restApi.createCustomer(consumer, RestApi.getGroupName());
        call.enqueue(new Callback<Consumer.CreatedConsumer>() {
            @Override
            public void onResponse(Call<Consumer.CreatedConsumer> call, Response<Consumer.CreatedConsumer> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                        subscribe();
//                        assignPartition();
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("CREATED CONSUMER");
                logger.info("instance_id: " + ofNullable(response.body().getInstance_id()).orElse(""));
                logger.info("base_uri: " + ofNullable(response.body().getBase_uri()).orElse(""));
                subscribe();
//                assignPartition();
            }

            @Override
            public void onFailure(Call<Consumer.CreatedConsumer> call, Throwable t) {
                logger.severe(t.getMessage());

            }
        });
    }

    public void subscribe() {
        logger.info("SUBSCRIBING");
        List<String> topics = new ArrayList<>();
        topics.add(RestApi.getClassificationResultTopic());
//        topics.add(RestApi.getTrainCNNTopic());
        Subscription subscription = new Subscription(topics);
        Call<String> call = restApi.subscribe(subscription, RestApi.getGroupName(), RestApi.getInstance());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
//                        getAssignedPartitions();
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("SUBSCRIBED");
                logger.info(response.message());
//                getAssignedPartitions();
//                seekToLastOffset();
//                seekToFirstOffset();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    public void getAssignedPartitions() {
        logger.info("GETTING ASSIGNED PARTITIONS");
        Call<Partitions> call = restApi.getPartitions(RestApi.getGroupName(), RestApi.getInstance());

        call.enqueue(new Callback<Partitions>() {
            @Override
            public void onResponse(Call<Partitions> call, Response<Partitions> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("GOT ASSIGNED PARTITIONS");
                logger.info(response.message());
            }

            @Override
            public void onFailure(Call<Partitions> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    public void seekToFirstOffset() {
        logger.info("SEEKING TO FIRST OFFSET");
        List<Partitions.Partition> listOfPartitions = new ArrayList<>();
        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 0));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 1));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 2));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 3));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 4));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 5));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 6));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 7));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 8));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 9));
        Partitions partitions = new Partitions(listOfPartitions);
        Call<String> call = restApi.seekToFirstOffset(partitions, RestApi.getGroupName(), RestApi.getInstance());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("SOUGHT TO FIRST OFFSET");
                logger.info(response.message());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    public void seekToLastOffset() {
        logger.info("SEEKING TO LAST OFFSET");
        List<Partitions.Partition> listOfPartitions = new ArrayList<>();
        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), partitionId));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getTrainCNNTopic(), partitionId));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 1));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 2));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 3));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 4));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 5));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 6));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 7));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 8));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 9));
        Partitions partitions = new Partitions(listOfPartitions);
        Call<String> call = restApi.seekToLastOffset(partitions, RestApi.getGroupName(), RestApi.getInstance());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("SOUGHT TO LAST OFFSET");
                logger.info(response.message());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    public void assignPartition() {
        logger.info("ASSIGNING PARTITION");
        List<Partitions.Partition> listOfPartitions = new ArrayList<>();
        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), partitionId));
//        listOfPartitions.add(new Partitions.Partition(RestApi.getTrainCNNTopic(), 1));
        Partitions partitions = new Partitions(listOfPartitions);
        Call<String> call = restApi.assignPartition(partitions, RestApi.getGroupName(), RestApi.getInstance());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("ASSIGNED PARTITION");
                logger.info(response.message());
                seekToLastOffset();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    public void unsubscribe() {
        logger.info("UNSUBSCRIBING");
        Call<String> call = restApi.unsubscribe(RestApi.getGroupName(), RestApi.getInstance());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("UNSUBSCRIBED");
                logger.info(response.message());
                destroyCustomerInstance();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    public void destroyCustomerInstance() {
        logger.info("DESTROYING CUSTOMER INSTANCE");
        Call<String> call = restApi.destroyConsumerInstance(RestApi.getGroupName(), RestApi.getInstance());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("DESTROYED CUSTOMER INSTANCE");
                logger.info(response.message());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }
}
