package com.example.kafka;


import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Logger;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import kotlin.jvm.internal.TypeReference;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, CameraXConfig.Provider {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    Button takeShot;
    Button classify;
    Button trainCNN;
    TextView label;
    PreviewView previewView;
    private boolean isTakingShot;
    private String labelText;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private RestApi restApi;
    private java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRESTConnection();

        takeShot = findViewById(R.id.takeShot);
        previewView = findViewById(R.id.previewView);
        classify = findViewById(R.id.classify);
        trainCNN = findViewById(R.id.trainCNN);
        label = findViewById(R.id.label);
        labelText = "example";

        takeShot.setOnClickListener(takeShotBtnClick);
        classify.setOnClickListener(classifyBtnClick);
        trainCNN.setOnClickListener(trainCNNBtnClick);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    private void setRESTConnection() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.8.103:9002")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        restApi = retrofit.create(RestApi.class);
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    private View.OnClickListener takeShotBtnClick = view -> {
        isTakingShot = true;
        if (label.getVisibility() == View.VISIBLE)
            label.setVisibility(View.INVISIBLE);
        Handler mHandler = new Handler();
        for (int i = 0; i < 1; i++) {
            mHandler.postDelayed(() -> capturePhoto(), 1500);
        }
    };

    private View.OnClickListener classifyBtnClick = view -> {
        isTakingShot = false;
        if (label.getVisibility() == View.INVISIBLE)
            label.setVisibility(View.VISIBLE);
        createConsumer();
//        unsubscribe();
    };

    private View.OnClickListener trainCNNBtnClick = view -> {
        isTakingShot = false;
        if (label.getVisibility() == View.VISIBLE)
            label.setVisibility(View.INVISIBLE);
        Handler mHandler = new Handler();
        for (int i = 0; i < 1; i++) {
            mHandler.postDelayed(() -> capturePhoto(), 1500);
        }
    };

    private void createConsumer() {
        logger.info("CREATING CONSUMER");
        Consumer consumer = new Consumer("testConsumer", "json", "earliest", "false");
        Call<Consumer> call = restApi.createCustomer(consumer);
        call.enqueue(new Callback<Consumer>() {
            @Override
            public void onResponse(Call<Consumer> call, Response<Consumer> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
//                        assignPartition();
                        subscribe();
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("CREATED CONSUMER");
                logger.info(response.message());
//                assignPartition();
                subscribe();
            }

            @Override
            public void onFailure(Call<Consumer> call, Throwable t) {
                logger.severe(t.getMessage());

            }
        });
    }

    private void subscribe() {
        logger.info("SUBSCRIBING");
        List<String> topics = new ArrayList<>();
        topics.add(RestApi.getClassificationResultTopic());
        Subscription subscription = new Subscription(topics);
        Call<Subscription> call = restApi.subscribe(subscription);
        call.enqueue(new Callback<Subscription>() {
            @Override
            public void onResponse(Call<Subscription> call, Response<Subscription> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                        fetchData();
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("SUBSCRIBED");
                logger.info(response.message());
                fetchData();
            }

            @Override
            public void onFailure(Call<Subscription> call, Throwable t) {
                logger.severe(t.getMessage());

            }
        });
    }

    private void assignPartition() {
        logger.info("ASSIGNING PARTITION");
        List<Partitions.Partition> listOfPartitions = new ArrayList<>();
        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 0));
        Partitions partitions = new Partitions(listOfPartitions);
        Call<Partitions> call = restApi.assignPartition(partitions);
        call.enqueue(new Callback<Partitions>() {
            @Override
            public void onResponse(Call<Partitions> call, Response<Partitions> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                        fetchData();
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("ASSIGNED PARTITION");
                logger.info(response.message());
                fetchData();
            }

            @Override
            public void onFailure(Call<Partitions> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    private void setLabel(String text) {
        label.setText(text);
    }

    private void fetchData() {
        logger.info("FETCHING DATA");
        Call<List<ClassificationResult>> call = restApi.fetchDataFromTopic();
        call.enqueue(new Callback<List<ClassificationResult>>() {
            @Override
            public void onResponse(Call<List<ClassificationResult>> call, Response<List<ClassificationResult>> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                List<String> results = response.body()
                        .stream()
                        .map(x -> x.getValue().getLabel())
                        .collect(Collectors.toList());

                logger.info("FETCHED DATA");
                Handler mHandler = new Handler();
                for (String result : results) {
                    logger.info(result);
                    mHandler.postDelayed(() -> setLabel(result), 1000);
                }
                unsubscribe();
            }

            @Override
            public void onFailure(Call<List<ClassificationResult>> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    private void unsubscribe() {
        logger.info("UNSUBSCRIBING");
        Call<String> call = restApi.unsubscribe();
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

    private void destroyCustomerInstance() {
        logger.info("DESTROYING CUSTOMER INSTANCE");
        Call<String> call = restApi.destroyConsumerInstance();
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

    private void capturePhoto() {
        imageCapture.takePicture(
                getExecutor(),
                new ImageCapture.OnImageCapturedCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        analyze(image);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                    }
                }
        );
    }

    private byte[] toByteArray(ByteBuffer buffer) {
        buffer.rewind();
        byte[] bb = new byte[buffer.remaining()];
        buffer.get(bb);
        return bb;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("kafka_analyze", "analyze: got frame at: " + image.getImageInfo().getTimestamp());

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bb = toByteArray(buffer);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(src)));

        Gson gson = builder.create();
        String json = gson.toJson(bb);

        sendOneImage(json, labelText);

        buffer.clear();
        image.close();
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .setCameraExecutor(getExecutor())
                .build();
    }

    private void sendOneImage(String image, String name) {
//        Records.Key key = new Records.Key(3, new Records.Key.Data(1));
//        Records.Value value = new Records.Value(1, new Records.Value.Data(image, name));
        logger.info("SENDING IMAGE");
        Records.Key key = new Records.Key(Calendar.getInstance().getTime().toString());
        Records.Value value = new Records.Value(name, image);

        List<Records.Record> recordsList = new ArrayList<>();
        recordsList.add(new Records.Record(key, value));
        Records records = new Records(recordsList);

        Call<Records> call;
        if (isTakingShot) {
            call = restApi.uploadImage(records);
        } else {
            call = restApi.trainCNN(records);
        }

        call.enqueue(new Callback<Records>() {
            @Override
            public void onResponse(Call<Records> call, Response<Records> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    logger.info("IMAGE SENT");
                    logger.info(response.message());
                    return;
                }
            }

            @Override
            public void onFailure(Call<Records> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }
}