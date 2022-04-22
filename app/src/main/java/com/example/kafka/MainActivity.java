package com.example.kafka;


import static java.util.Optional.ofNullable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, CameraXConfig.Provider {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    Context context;
    Button takeShot;
    Button trainCNN;
    com.toptoche.searchablespinnerlibrary.SearchableSpinner label;
    PreviewView previewView;
    private boolean isClassifying;
    private String labelText;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private RestApi restApi;
    private RestApiDefinitions restApiDef;
    private java.util.logging.Logger logger;
    private int numberOfPhotos;
    private int numberOfPhotosToTrain;
    ArrayAdapter<String> adapter;
    String timestamp;
    private int partitionId;
    private String logFilePath;
    OkHttpClient.Builder httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Detection App");
        context = this;
        logger = java.util.logging.Logger.getGlobal();

        takeShot = findViewById(R.id.takeShot);
        previewView = findViewById(R.id.previewView);
        trainCNN = findViewById(R.id.trainCNN);
        label = findViewById(R.id.label);
        labelText = "";
        numberOfPhotos = 1;
        numberOfPhotosToTrain = 10;
        label.setTitle("Select Label");
        label.setPositiveButton("OK");
        Random random = new Random();
        partitionId = random.nextInt(10);
        logFilePath = getExternalFilesDir(null) + "/logFile.txt";

        setRESTConnection();

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
        httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.8.161:9002")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        restApi = retrofit.create(RestApi.class);
        restApiDef = new RestApiDefinitions(restApi);
        restApiDef.setPartitionId(partitionId);
//        restApiDef.fetchConsumersList();
        restApiDef.createConsumer();
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
        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(new Size(224, 224))
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

    private void setLabel(String text) {
        logger.info(text);
        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> label.setSelection(adapter.getPosition(text.toLowerCase())), 1000);
    }

    private void fetchData() {
        logger.info("FETCHING DATA");
        Call<List<ClassificationResult>> call = restApi.fetchDataFromTopic(RestApi.getGroupName(), RestApi.getInstance());
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

                if (!response.body().isEmpty()) {
                    List<String> results = response.body()
                            .stream()
                            .filter(x -> ofNullable(x).map(ClassificationResult::getKey).map(ClassificationResult.Key::getId).orElse("").equals(RestApi.getInstance()))
                            .map(x -> ofNullable(x).map(ClassificationResult::getValue).map(ClassificationResult.Value::getLabel).orElse("tench"))
                            .collect(Collectors.toList());
                    results.stream().map(x -> ofNullable(x).orElse("tench")).forEach(x -> setLabel(x));
                    if (!results.isEmpty()) {
                        @SuppressLint("SimpleDateFormat") DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        String timestamp2 = sdf.format(Calendar.getInstance().getTime());
                        logger.info("ANSWER: " + results.get(0) + "\npre: " + timestamp + ", aft: " + timestamp2);
                        appendToFile("ANSWER: " + results.get(0) + "\npre: " + timestamp + ", aft: " + timestamp2 + "\n");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ClassificationResult>> call, Throwable t) {
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

    private byte[] compress(byte[] in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            defl.write(in);
            defl.flush();
            defl.close();

            return out.toByteArray();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String toByteArray(ByteBuffer buffer) {
        buffer.rewind();
        byte[] bb = new byte[buffer.remaining()];
        buffer.get(bb);
        logger.info(String.valueOf(bb.length));
        bb = compress(bb);
        logger.info(String.valueOf(bb.length));
        return Base64.getEncoder().encodeToString(bb);
    }

    private Bitmap imageProxyToBitmap(ByteBuffer buffer) {
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void analyze(@NonNull ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        String bb = toByteArray(buffer);
//        byte[] bb = toByteArray(buffer);
//        Bitmap bitmap = imageProxyToBitmap(buffer);
//        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        resized.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//        logger.info(String.valueOf(byteArray.length));
//        byte[] ba = compress(byteArray);
//        logger.info(String.valueOf(ba.length));
//        String bb =Base64.getEncoder().encodeToString(ba);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(src)));

//        Gson gson = builder.create();
//        String json = gson.toJson(bb);
        logger.info(String.valueOf(bb.length()));
        labelText = ofNullable(label.getSelectedItem()).orElse("").toString();
        logger.info(labelText);
        sendOneImage(bb, labelText);

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
        @SuppressLint("SimpleDateFormat") DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        timestamp = sdf.format(Calendar.getInstance().getTime());
        logger.info("timestamp:" + timestamp);
        RecordsToPost.Key key = new RecordsToPost.Key(RestApi.getInstance());
        RecordsToPost.Value value = new RecordsToPost.Value(name, image);

        List<RecordsToPost.Record> recordsList = new ArrayList<>();
        recordsList.add(new RecordsToPost.Record(key, value));
        RecordsToPost records = new RecordsToPost(recordsList);
        Call<RecordsPosted> call;
        if (isClassifying) {
            call = restApi.postData(records, String.valueOf(partitionId));
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
//                logger.info(ofNullable(response.body().getKey_schema_id()).orElse("").toString());
//                logger.info(ofNullable(response.body().getValue_schema_id()).orElse("").toString());
//                logger.info(String.valueOf(response.body().getOffsets().get(0).getOffset()));
//                logger.info(String.valueOf(response.body().getOffsets().get(0).getPartition()));
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

    private void appendToFile(String data) {
        File file = new File(logFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                logger.severe(ioe.getMessage());
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(data);
            writer.close();
            fileOutputStream.close();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        httpClient.build().dispatcher().cancelAll();
        restApiDef.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        httpClient.build().dispatcher().cancelAll();
        restApiDef.unsubscribe();
        super.onBackPressed();
    }
}