package com.example.kafka.Temp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.kafka.JsonPlaceHolderApi;
import com.example.kafka.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


//<TextureView
//            android:id="@+id/textureView"
//            android:layout_width="match_parent"
//            android:layout_height="wrap_content"
//            android:layout_above="@+id/button"
//            android:layout_alignParentTop="true"/>
//
//        <Button
//            android:id="@+id/button"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:layout_centerHorizontal="true"
//            android:layout_alignParentBottom="true"
//            android:layout_marginTop="20dp"
//            android:layout_marginBottom="10dp"
//            android:backgroundTint="@color/colorPrimary"
//            android:text="@string/select_video"
//            android:textColor="@android:color/white"
//            android:textSize="18sp" />
//
//        <Button
//            android:id="@+id/videoOnlineImageButton"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:layout_centerHorizontal="true"
//            android:layout_alignParentBottom="true"
//            android:layout_marginTop="32dp"
//            android:layout_marginBottom="10dp"
//            android:backgroundTint="@color/colorPrimary"
//            android:text="@string/send_video"
//            android:textColor="@android:color/white"
//            android:textSize="18sp" />

public class Temp extends AppCompatActivity {
    //    private TextView textViewResult;
    private TextureView textureView;
    Button button;
//    Button SendImage;

//    int SELECT_PICTURE = 200;
//    int VIDEO = 201;
//    File imageFile;

    JsonPlaceHolderApi jsonPlaceHolderApi;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;
    //    CaptureRequest captureRequest;
    CaptureRequest.Builder captureRequestBuilder;
    //    ImageReader imageReader;
    Handler mBackgroundHandler;
    HandlerThread mBackgroundThread;

    //    Surface surface;
    private String cameraId;
    private Size imageDimensions;

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.8.103:8082")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
//        textViewResult = findViewById(R.id.text_view_result);
//        textureView = findViewById(R.id.textureView);
//        button = findViewById(R.id.button);

        textureView.setSurfaceTextureListener(textureListener);
        button.setOnClickListener(v -> {
            try {
                takePic();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        });


//        SendImage = findViewById(R.id.SendImage);
//        SendImage.setOnClickListener(v -> postImage());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Cam perm necessary", Toast.LENGTH_LONG).show();
//                finish();
            }

        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void openCamera() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimensions = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Temp.this, new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, null);
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
        Surface surface = new Surface(texture);
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }
                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                Toast.makeText(getApplicationContext(), "Config changed", Toast.LENGTH_LONG).show();
            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);

    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    protected void stopBackgroundThread() throws InterruptedException {
        mBackgroundThread.quitSafely();
        mBackgroundThread.join();
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }

    private void takePic() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
        Size[] jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
        int width = 640;
        int height = 480;

        if (jpegSizes != null && jpegSizes.length > 0) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        List<Surface> outputSurface = new ArrayList<>(2);
        outputSurface.add(imageReader.getSurface());
        outputSurface.add(new Surface(textureView.getSurfaceTexture()));
        CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));

        ImageReader.OnImageAvailableListener readerListener = reader -> {
            Image image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);

            postImage("aaa", bytes);

            image.close();
        };

        imageReader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                try {
                    createCameraPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, mBackgroundHandler);
    }

//    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            Image image = null;
//            try {
//                image = reader.acquireLatestImage();
//                if (image != null) {
//                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                    Bitmap bitmap = fromByteBuffer(buffer);
//                    image.close();
//                }
//            } catch (Exception e) {
//                textViewResult.setText(e.getMessage());
//            }
//        }
//    };
//
//    Bitmap fromByteBuffer(ByteBuffer buffer) {
//        byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes, 0, bytes.length);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }

//    void takeVideo() {
//        Intent i = new Intent();
//        i.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
//        if (i.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(i, VIDEO);
//        }
//    }

//    void imageChooser() {
//        Intent i = new Intent();
//        i.setType("video/*");
////        i.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//        i.setAction(Intent.ACTION_GET_CONTENT);
//
//        startActivityForResult(Intent.createChooser(i, "Select Video"), SELECT_PICTURE);
//    }

    void sendOneImage(String name, byte[] image) {
        Map<String, Object> values = new HashMap<>();
//        String base64String = Base64.encodeToString(image, Base64.DEFAULT);
//        values.put("name", name);
//        values.put("image", image);
//        Records.Record record_1 = new Records.Record(values);
//        List<Records.Record> recordList = new ArrayList<>();
//        recordList.add(record_1);
//
//        Records records = new Records(recordList);
//        Records records = new Records();
//
//        Call<Records> call = jsonPlaceHolderApi.uploadImage(records);
//        call.enqueue(new Callback<Records>() {
//            @Override
//            public void onResponse(Call<Records> call, Response<Records> response) {
//                if (!response.isSuccessful()) {
//                    try {
//                        System.out.println("Code: " + response.code() + "\n" + response.errorBody().string() + "\n" + response.message());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return;
//                }
//
////                textViewResult.setText("Code: " + response.code() + "\n" + response.body().toString() + "\n" + response.message());
//            }
//
//            @Override
//            public void onFailure(Call<Records> call, Throwable t) {
//                System.out.println(t.getMessage());
//
//            }
//        });
    }

    void postImage(String name, byte[] image) {
//            byte[] images = Utils.getBytesFromFile(imageFile);
//            byte[] images = Files.readAllBytes(imageFile.toPath());

        for (int i = 0; i < image.length; i += 1080) {
            if (i + 1080 > image.length) {
                sendOneImage(name, Arrays.copyOfRange(image, i, image.length));
            } else sendOneImage(name, Arrays.copyOfRange(image, i, i + 1080));
        }
    }

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            if (requestCode == SELECT_PICTURE) {
//                Uri selectedVideo = data.getData();
//                String path = Utils.getPath(MyApplication.getAppContext(), selectedVideo);
//                imageFile = new File(path);
//            } else if (requestCode == VIDEO) {
//
//            }
//        }
//    }


}

