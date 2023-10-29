package com.example.autorepairai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.autorepairai.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private ImageView preview;
    private ImageView imageViewForDownload;

    ActivityResultLauncher<Intent> resultLauncher;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    YUVtoRGB translator = new YUVtoRGB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        registerResult();
    }

    public void onGetCameraClick(View view) {
        preview = findViewById(R.id.preview);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            initializeCamera();
        }
    }

    public void onSavePictureClick(View view) {
        Bitmap bitmap = ((BitmapDrawable)preview.getDrawable()).getBitmap();
        saveReceivedImage(bitmap, "some_name");
    }

    public void onDownloadPictureClick(View view) {
        imageViewForDownload = findViewById(R.id.pictureFromFiles);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);

    }

    private void registerResult(){
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result){
                        try{
                            Uri imageUri = result.getData().getData();
                            imageViewForDownload.setImageURI(imageUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Не выбрано", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void saveReceivedImage(Bitmap image, String imageName){
        try {
            //TODO ПОДУМАТЬ КУДА СОХРАНЯТЬ
            File path = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppName" + File.separator + "Images");
            Toast.makeText(this, path.toString(), Toast.LENGTH_SHORT).show();
            if(!path.exists()){
                path.mkdirs();
            }
            File outFile = new File(path, imageName + ".jpeg");
            FileOutputStream outputStream = new FileOutputStream(outFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        }
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1024, 768))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this),
                            new ImageAnalysis.Analyzer() {
                                @Override
                                public void analyze(@NonNull ImageProxy image) {
                                    Image img = image.getImage();
                                    Bitmap bitmap = translator.translateYUV(img, MainActivity.this);

                                    preview.setRotation(image.getImageInfo().getRotationDegrees());
                                    preview.setImageBitmap(bitmap);
                                    image.close();
                                }
                            });

                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, imageAnalysis);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

}