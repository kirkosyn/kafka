package com.example.kafka;


import static java.util.Optional.ofNullable;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.kafka.Objects.ClassificationResult;
import com.example.kafka.Objects.Consumer;
import com.example.kafka.Objects.Partitions;
import com.example.kafka.Objects.RecordsPosted;
import com.example.kafka.Objects.RecordsToPost;
import com.example.kafka.Objects.Subscription;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
    Button trainCNN;
    Spinner label;
    PreviewView previewView;
    private boolean isClassifying;
    private String labelText;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private RestApi restApi;
    private java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
    private int numberOfPhotos;
    private int numberOfPhotosToTrain;
    ArrayAdapter<String> adapter;
    String timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Detection App");

        setRESTConnection();

        takeShot = findViewById(R.id.takeShot);
        previewView = findViewById(R.id.previewView);
        trainCNN = findViewById(R.id.trainCNN);
        label = findViewById(R.id.label);
        labelText = "";
        numberOfPhotos = 1;
        numberOfPhotosToTrain = 10;

        takeShot.setOnClickListener(takeShotBtnClick);
        trainCNN.setOnClickListener(trainCNNBtnClick);
        populateSpinner();

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

    private void populateSpinner() {
        List<String> str = new ArrayList<>();
        int id = getResources().getIdentifier("labels", "raw", getPackageName());
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(id)));
            String line = in.readLine().toLowerCase();
            while (line != null) {
                str.add(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        str = str.stream().map(x -> x.toLowerCase()).collect(Collectors.toList());
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, str);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        label.setAdapter(adapter);
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
        createConsumer();
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
//                .setTargetResolution(new Size(224, 224))
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    private View.OnClickListener takeShotBtnClick = view -> {
        isClassifying = true;

        Handler mHandler = new Handler();
        for (int i = 0; i < numberOfPhotos; i++) {
            mHandler.postDelayed(() -> capturePhoto(), 1000);
        }
    };

    private View.OnClickListener trainCNNBtnClick = view -> {
        isClassifying = false;
        Handler mHandler = new Handler();
        for (int i = 0; i < numberOfPhotosToTrain; i++) {
            mHandler.postDelayed(() -> capturePhoto(), 1500);
        }
    };

    private void createConsumer() {
        logger.info("CREATING CONSUMER");
        Consumer.ConsumerToCreate consumer = new Consumer.ConsumerToCreate("testConsumer", "json", "earliest", "false");
        Call<Consumer.CreatedConsumer> call = restApi.createCustomer(consumer);
        call.enqueue(new Callback<Consumer.CreatedConsumer>() {
            @Override
            public void onResponse(Call<Consumer.CreatedConsumer> call, Response<Consumer.CreatedConsumer> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
//                        subscribe();
                        assignPartition();
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                    return;
                }
                logger.info("CREATED CONSUMER");
                logger.info("instance_id: " + ofNullable(response.body().getInstance_id()).orElse(""));
                logger.info("base_uri: " + ofNullable(response.body().getBase_uri()).orElse(""));
//                subscribe();
                assignPartition();
            }

            @Override
            public void onFailure(Call<Consumer.CreatedConsumer> call, Throwable t) {
                logger.severe(t.getMessage());

            }
        });
    }

    private void subscribe() {
        logger.info("SUBSCRIBING");
        List<String> topics = new ArrayList<>();
        topics.add(RestApi.getClassificationResultTopic());
        topics.add(RestApi.getTrainCNNTopic());
        Subscription subscription = new Subscription(topics);
        Call<String> call = restApi.subscribe(subscription);
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
                logger.info("SUBSCRIBED");
                logger.info(response.message());
                seekToLastOffset();
//                seekToFirstOffset();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });
    }

    private void seekToFirstOffset() {
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
        Call<String> call = restApi.seekToFirstOffset(partitions);
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

    private void seekToLastOffset() {
        logger.info("SEEKING TO LAST OFFSET");
        List<Partitions.Partition> listOfPartitions = new ArrayList<>();
        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 0));
        listOfPartitions.add(new Partitions.Partition(RestApi.getTrainCNNTopic(), 0));
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
        Call<String> call = restApi.seekToLastOffset(partitions);
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

    private void assignPartition() {
        logger.info("ASSIGNING PARTITION");
        List<Partitions.Partition> listOfPartitions = new ArrayList<>();
        listOfPartitions.add(new Partitions.Partition(RestApi.getClassificationResultTopic(), 0));
        listOfPartitions.add(new Partitions.Partition(RestApi.getTrainCNNTopic(), 0));
        Partitions partitions = new Partitions(listOfPartitions);
        Call<String> call = restApi.assignPartition(partitions);
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

    private void setLabel(String text) {
        logger.info(text);
        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> label.setSelection(adapter.getPosition(text.toLowerCase())), 1000);
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
                logger.info("FETCHED DATA");

                List<String> results = response.body()
                        .stream()
                        .map(x -> x.getValue().getLabel())
                        .collect(Collectors.toList());
                results.stream().map(x -> ofNullable(x).orElse("")).forEach(x -> setLabel(x));
                if (!results.isEmpty()) {
                    DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String timestamp2 = sdf.format(Calendar.getInstance().getTime());
                    logger.info("ANSWER: \npre: " + timestamp + ", aft: " + timestamp2);
                }
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
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bb = toByteArray(buffer);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(src)));

        Gson gson = builder.create();
        String json = gson.toJson(bb);
        labelText = label.getSelectedItem().toString();
        logger.info(labelText);
        sendOneImage(json, labelText);

        buffer.clear();
        image.close();
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            labelText = (String) parent.getItemAtPosition(pos);
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .setCameraExecutor(getExecutor())
                .build();
    }

    private void sendOneImage(String image, String name) {
        logger.info("SENDING IMAGE");
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        timestamp = sdf.format(Calendar.getInstance().getTime());
        RecordsToPost.Key key = new RecordsToPost.Key(timestamp);
        RecordsToPost.Value value = new RecordsToPost.Value(name, image);

        List<RecordsToPost.Record> recordsList = new ArrayList<>();
        recordsList.add(new RecordsToPost.Record(key, value));
        RecordsToPost records = new RecordsToPost(recordsList);

        Call<RecordsPosted> call;
        if (isClassifying) {
            call = restApi.postData(records);
        } else {
            call = restApi.trainCNN(records);
        }

        call.enqueue(new Callback<RecordsPosted>() {
            @Override
            public void onResponse(Call<RecordsPosted> call, Response<RecordsPosted> response) {
                if (!response.isSuccessful()) {
                    try {
                        logger.severe("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
                    } catch (IOException e) {
                        logger.severe(e.getMessage());
                    }
                }
                logger.info("IMAGE SENT");
                logger.info(ofNullable(response.body().getKey_schema_id()).orElse("").toString());
                logger.info(ofNullable(response.body().getValue_schema_id()).orElse("").toString());
                logger.info(String.valueOf(response.body().getOffsets().get(0).getOffset()));
                logger.info(String.valueOf(response.body().getOffsets().get(0).getPartition()));
            }

            @Override
            public void onFailure(Call<RecordsPosted> call, Throwable t) {
                logger.severe(t.getMessage());
            }
        });

        if (isClassifying) {
            Handler mHandler = new Handler();
            for (int i = 0; i < 100; i++) {
                mHandler.postDelayed(() -> fetchData(), 500);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        unsubscribe();
        super.onBackPressed();
    }
}