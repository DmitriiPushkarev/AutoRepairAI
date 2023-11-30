package com.example.autorepairai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.autorepairai.api.RestApi;
import com.example.autorepairai.ui.dashboard.DashboardViewModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.autorepairai.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ImageView imageViewForDownload;

    private DashboardViewModel dashboardViewModel;

    ActivityResultLauncher<Intent> resultLauncher;

    RequestQueue mRequestQueue;

    static final String SAVE_EMAIL = "save_email";

    static final String SAVE_PASSWORD = "save_password";

    //static final String SAVE_FIRST_NAME = "save_first_name";

    //static final String SAVE_SECOND_NAME = "save_second_name";

    static final String SAVE_USERID = "save_userid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        deleteUserData();
        if (isNewUser()){
            setContentView(R.layout.fragment_enter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        imageViewForDownload = findViewById(R.id.pickImage);
        imageViewForDownload.setImageURI(uri);

        Button buttonForSend = findViewById(R.id.buttonForSend);
        buttonForSend.setVisibility(View.VISIBLE);
    }

    public void onSendPictureClick(View view) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        TextView notification = findViewById(R.id.notification);
        notification.setText(" Внимание, фото обрабатывается, это займет \n несколько секунд");
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        ImageView imageViewForDownload = findViewById(R.id.pickImage);
        imageViewForDownload.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
        imageViewForDownload.setClickable(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                ImageView imageViewForDownload = findViewById(R.id.pickImage);
                //String apiKey = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_USERID, "");
                String apiKey = "210ffaa5b1ce4425302594cc9f7283c2cf930af7";
                String id = RestApi.createAppId(apiKey);
                RestApi.uploadFile(id, imageViewForDownload,apiKey);
                RestApi.sendFile(id,apiKey);
                dashboardViewModel.setCurrentId(id);
                String responseString = "1";

                int i = 0;
                while (responseString.contains("1") && i != 20){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responseString = RestApi.getStatus(id,apiKey);
                    System.out.println("Response status: "+responseString);
                    i++;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.fragment_step2);
                    }
                });
            }
        });
        thread.start();
    }

    public void onGetReportClick(View view) {
        //Айди всеравно остается в модели, была теория, что при переключении меню объект обнуляется
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
//                TextView tv1 = (TextView) findViewById(R.id.textView6);
//                String id = dashboardViewModel.getCurrentId();
//                RestApi.getResult(id, tv1);
            }
        }).start();
    }

    public void showRegistrationForm(View view){
        setContentView(R.layout.fragment_registration);
    }

    public void showSecondStep(View view){
        setContentView(R.layout.fragment_step2);
    }

    public void showThirdStep(View view){
        setContentView(R.layout.fragment_step3);
    }

    public void showFourthStep(View view){
        setContentView(R.layout.fragment_step4);
    }

    public void closeFragment(View view){
        setContentView(binding.getRoot());
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        imageViewForDownload.clearColorFilter();
        imageViewForDownload.setClickable(true);
    }

    public void onDownloadPictureClick(View view) {
        ImagePicker.with(MainActivity.this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    //Add connection to api
    public void saveUserData(View view) {
        SharedPreferences.Editor ed = getSharedPreferences("setting",MODE_PRIVATE).edit();

        EditText tvEmail = (EditText) findViewById(R.id.editTextTextEmailAddressEnter);
        EditText tvPassword = (EditText) findViewById(R.id.editTextTextPasswordEnter);
        //EditText tvFirstName = (EditText) findViewById(R.id.editTextTextPersonFirstName);
        //EditText tvSecondName = (EditText) findViewById(R.id.editTextTextPersonSecondName);

        ed.putString(SAVE_EMAIL, tvEmail.getText().toString());
        ed.putString(SAVE_PASSWORD, tvPassword.getText().toString());
        //ed.putString(SAVE_FIRST_NAME, tvFirstName.getText().toString());
        //ed.putString(SAVE_SECOND_NAME, tvSecondName.getText().toString());
        //ed.putString(SAVE_USERID, UUID.randomUUID().toString());

        ed.apply();

        setContentView(binding.getRoot());
    }

    public void registration(View view){
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                EditText tvEmail = (EditText) findViewById(R.id.editTextTextEmailAddressReg);
                EditText tvPassword = (EditText) findViewById(R.id.editTextTextPasswordReg);
                SharedPreferences.Editor ed = getSharedPreferences("setting",MODE_PRIVATE).edit();
                ed.putString(SAVE_EMAIL, tvEmail.getText().toString());
                ed.putString(SAVE_PASSWORD, tvPassword.getText().toString());

                String strEmail = tvEmail.getText().toString();
                String strPassword = tvPassword.getText().toString();
                String apiKey = RestApi.registration(strEmail, strPassword, view);
                System.out.println("ApiKey: " + apiKey);
                ed.putString(SAVE_USERID, apiKey);

                ed.apply();

                if(apiKey!=null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(binding.getRoot());
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public void authorization(View view){
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                EditText tvEmail = (EditText) findViewById(R.id.editTextTextEmailAddressEnter);
                EditText tvPassword = (EditText) findViewById(R.id.editTextTextPasswordEnter);
                SharedPreferences.Editor ed = getSharedPreferences("setting",MODE_PRIVATE).edit();
                ed.putString(SAVE_EMAIL, tvEmail.getText().toString());
                ed.putString(SAVE_PASSWORD, tvPassword.getText().toString());

                String strEmail = tvEmail.getText().toString();
                String strPassword = tvPassword.getText().toString();
                String apiKey = RestApi.authorization(strEmail, strPassword);
                ed.putString(SAVE_USERID, apiKey);

                ed.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(binding.getRoot());
                    }
                });
            }
        });
        thread.start();
    }

    private boolean isNewUser(){
        String email = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_EMAIL, "");
        String password = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_PASSWORD, "");
        //String firstName = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_FIRST_NAME, "");
        //String secondName = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_SECOND_NAME, "");
        String uuid = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_USERID, "");

        return email.isEmpty() || password.isEmpty() || uuid.isEmpty();
    }

    private void deleteUserData(){
        SharedPreferences prefs = getSharedPreferences("setting", MODE_PRIVATE);

        prefs.edit().remove(SAVE_EMAIL).apply();
        prefs.edit().remove(SAVE_PASSWORD).apply();
        //prefs.edit().remove(SAVE_FIRST_NAME).apply();
        //prefs.edit().remove(SAVE_SECOND_NAME).apply();
        prefs.edit().remove(SAVE_USERID).apply();
    }
}