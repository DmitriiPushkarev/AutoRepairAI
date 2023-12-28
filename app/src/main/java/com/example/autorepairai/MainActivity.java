package com.example.autorepairai;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autorepairai.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    static List<String> checkBoxes = new ArrayList<>();

    static final String[] models = {"Granta", "Largus",
            "Priora",
            "Kalina",
            "LADA 4x4",
            "ВАЗ-21213-214i (NIVA)",
            "ВАЗ-21213 (NIVA)",
            "ВАЗ-2131 (NIVA)",
            "ВАЗ-2121 (NIVA)",
            "ВАЗ-2120 (Надежда)",
            "Vesta",
            "ВАЗ-1111 \"ОКА\"",
            "Niva",
            "2123",
            "GRANTA",
            "VIS",
            "21214",
            "21310",
            "21314",
            "Urban",
            "Bronto",
            "XRAY",
            "ВАЗ-2170",
            "ВАЗ-1118",
            "ВАЗ-1111 (ОКА)",
            "LADA Granta 2191",
            "Chevrolet Niva 1.7",
            "LADA XRAY",
            "LADA Vesta",
            "LADA Largus 4601",
            "LADA Granta 2190",
            "LADA Priora FL (ВАЗ-2170)",
            "LADA Priora Coupe",
            "LADA Kalina 2194",
            "LADA Kalina 1119",
            "ВАЗ-2123",
            "LADA 4x4","Chevrolet Niva",
            "LADA 21214",
            "LADA Granta FL",
            "LADA Vesta SW",
            "LADA Largus",
            "LADA Granta Cross",
            "LADA Vesta Cross",
            "LADA XRAY Cross",
            "LADA Vesta Sport",
            "LADA Granta Sport",
            "LADA Kalina Sport",
            "LADA Niva Travel",
            "ВАЗ-2115",
            "ВАЗ-2114",
            "ВАЗ-2113","ВАЗ-2112","ВАЗ-2111","ВАЗ-2110","ВАЗ-2109","ВАЗ-2109","ВАЗ-2108","ВАЗ-2107","ВАЗ-1111"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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
        if (isNewUser()) {
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
        ImageView alert = findViewById(R.id.alertCarNotFind);
        alert.setVisibility(View.INVISIBLE);
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
                String apiKey = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_USERID, "");
                String id = RestApi.createAppId(apiKey);
                RestApi.uploadFile(id, imageViewForDownload, apiKey);
                RestApi.sendFile(id, apiKey);
                dashboardViewModel.setCurrentId(id);
                String responseString = "1";

                int i = 0;
                while (responseString.contains("1") && i != 20) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responseString = RestApi.getStatus(id, apiKey);
                    i++;
                }

                if (responseString.contains("2") && responseString!=null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notification.setText("Внимание, выберите только одно фото");
                            setContentView(R.layout.fragment_step2);

                            TextView carBrand = findViewById(R.id.editTextTextCarBrand);

                            Spinner spinner = findViewById(R.id.spinnerModel);
                            // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, models);
                            // Определяем разметку для использования при выборе элемента
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            // Применяем адаптер к элементу spinner
                            spinner.setAdapter(adapter);
                        }
                    });
                }

                if (responseString.contains("3") && responseString!=null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView alert = findViewById(R.id.alertCarNotFind);
                            alert.setVisibility(View.VISIBLE);

                            ProgressBar progressBar = findViewById(R.id.progressBar);
                            progressBar.setVisibility(View.INVISIBLE);
                            imageViewForDownload.clearColorFilter();
                            imageViewForDownload.setClickable(true);

                            notification.setText("Внимание, выберите только одно фото");
                        }
                    });
                }

            }
        });
        thread.start();
    }

    public void showRegistrationForm(View view) {
        setContentView(R.layout.fragment_registration);
    }

    public void showSecondStep(View view) {
        setContentView(R.layout.fragment_step2);

        Spinner spinner = findViewById(R.id.spinnerModel);
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, models);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapter);
    }

    public void closeFragment(View view) {
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
        SharedPreferences.Editor ed = getSharedPreferences("setting", MODE_PRIVATE).edit();

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

    public void registration(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                EditText tvEmail = (EditText) findViewById(R.id.editTextTextEmailAddressReg);
                EditText tvPassword = (EditText) findViewById(R.id.editTextTextPasswordReg);
                SharedPreferences.Editor ed = getSharedPreferences("setting", MODE_PRIVATE).edit();
                ed.putString(SAVE_EMAIL, tvEmail.getText().toString());
                ed.putString(SAVE_PASSWORD, tvPassword.getText().toString());

                String strEmail = tvEmail.getText().toString();
                String strPassword = tvPassword.getText().toString();
                String apiKey = RestApi.registration(strEmail, strPassword, view);

                if (apiKey != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(apiKey);
                        ed.putString(SAVE_USERID, jsonObject.getString("userGuid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ed.apply();

                if (apiKey != null) {
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

    public void authorization(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                EditText tvEmail = (EditText) findViewById(R.id.editTextTextEmailAddressEnter);
                EditText tvPassword = (EditText) findViewById(R.id.editTextTextPasswordEnter);
                SharedPreferences.Editor ed = getSharedPreferences("setting", MODE_PRIVATE).edit();
                ed.putString(SAVE_EMAIL, tvEmail.getText().toString());
                ed.putString(SAVE_PASSWORD, tvPassword.getText().toString());

                String strEmail = tvEmail.getText().toString();
                String strPassword = tvPassword.getText().toString();
                String apiKey = RestApi.authorization(strEmail, strPassword, view);

                if (apiKey != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(apiKey);
                        ed.putString(SAVE_USERID, jsonObject.getString("userGuid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ed.apply();

                if (apiKey != null) {
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

    public void updateInfoAuto(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                EditText mark = (EditText) findViewById(R.id.editTextTextCarBrand);
                Spinner model = (Spinner) findViewById(R.id.spinnerModel);
                EditText year = (EditText) findViewById(R.id.editTextTextReleaseYear);

                String strMark = mark.getText().toString();
                String strModel = model.getSelectedItem().toString();
                String strYear = year.getText().toString();

                Log.i("UpdateInfo step 2", "Mark: " + strMark + " Model: " + strModel + " Year: " + strYear);

                String response = null;
                try {
                    String apiKey = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_USERID, "");
                    response = RestApi.updateAutoInfo(apiKey, strMark, strModel, strYear, dashboardViewModel.getCurrentId());
                    JSONObject jsonObject = new JSONObject(response);
                    Log.i("PATCH Request:", response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String apiKey = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_USERID, "");
                String response1 = RestApi.getResult(dashboardViewModel.getCurrentId(), apiKey);

                if (response != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.fragment_step3);

                            try {
                                JSONObject jsonObject = new JSONObject(response1);
                                JSONObject jsonObjectBoxes = new JSONObject(jsonObject.getString("boxes"));
                                JSONArray classes = jsonObjectBoxes.getJSONArray("classes");
                                List<String> classesList = new ArrayList<>();
                                for (int i = 0; i < classes.length(); i++) {
                                    classesList.add(classes.getString(i));
                                }

                                String boxes = jsonObjectBoxes.getString("boxes");
                                String prices = jsonObject.getString("prices");
                                Log.i("Result request, boxes:", boxes);
                                Log.i("Result request, classes:", classesList.toString());
                                Log.i("Result request, prices:", prices);

                                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                                View fragment_step3 = inflater.inflate(R.layout.fragment_step3, null, false);

                                CheckBox checkBox = findViewById(R.id.checkBox);
                                CheckBox checkBox2 = findViewById(R.id.checkBox2);
                                CheckBox checkBox3 = findViewById(R.id.checkBox3);
                                CheckBox checkBox4 = findViewById(R.id.checkBox4);
                                CheckBox checkBox5 = findViewById(R.id.checkBox5);
                                CheckBox checkBox6 = findViewById(R.id.checkBox6);
                                CheckBox checkBox7 = findViewById(R.id.checkBox7);

                                List<CheckBox> checkBoxes = new ArrayList<>();
                                checkBoxes.add(checkBox);
                                checkBoxes.add(checkBox2);
                                checkBoxes.add(checkBox3);
                                checkBoxes.add(checkBox4);
                                checkBoxes.add(checkBox5);
                                checkBoxes.add(checkBox6);
                                checkBoxes.add(checkBox7);

                                for (int i = 0; i < checkBoxes.size(); i++) {
                                    for (int j = 0; j < classesList.size(); j++) {
                                        if (Objects.equals(classesList.get(j), checkBoxes.get(i).getText().toString())) {
                                            checkBoxes.get(i).setChecked(true);
                                        }
                                    }
                                }

//                                RecyclerView recyclerView = findViewById(R.id.recyclerViewStep3);
//                                recyclerView.setHasFixedSize(true);
//                                // создаем адаптер
//                                LinearLayoutManager llm = new LinearLayoutManager(fragment_step3.getContext());
//                                llm.setOrientation(LinearLayoutManager.VERTICAL);
//                                recyclerView.setLayoutManager(llm);
//                                // устанавливаем для списка адаптер
//                                recyclerView.setAdapter(new StateAdapter(fragment_step3.getContext(), new ArrayList<>(classesList)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public void drawBoxes(View view) {
        ImageView imageViewForDownload = findViewById(R.id.pickImage);
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageViewForDownload.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Bitmap tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(15);
        canvas.drawRect(95, 137, 550, 107, paint);
        imageViewForDownload.setImageBitmap(tempBitmap);
    }

    public void getAdvice(View view) {
        Snackbar.make(view, "Вспомните!", Snackbar.LENGTH_SHORT).show();
    }

    public void showThirdStep(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                runOnUiThread(new Runnable() {
                    String apiKey = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_USERID, "");
                    String response1 = RestApi.getResult(dashboardViewModel.getCurrentId(), apiKey);

                    @Override
                    public void run() {
                        setContentView(R.layout.fragment_step3);

                        try {
                            JSONObject jsonObject = new JSONObject(response1);
                            JSONObject jsonObjectBoxes = new JSONObject(jsonObject.getString("boxes"));
                            JSONArray classes = jsonObjectBoxes.getJSONArray("classes");
                            List<String> classesList = new ArrayList<>();
                            for (int i = 0; i < classes.length(); i++) {
                                classesList.add(classes.getString(i));
                            }

                            String boxes = jsonObjectBoxes.getString("boxes");
                            String prices = jsonObject.getString("prices");
                            Log.i("Result request, boxes:", boxes);
                            Log.i("Result request, classes:", classesList.toString());
                            Log.i("Result request, prices:", prices);

                            LayoutInflater inflater = LayoutInflater.from(view.getContext());
                            View fragment_step3 = inflater.inflate(R.layout.fragment_step3, null, false);

                            CheckBox checkBox = findViewById(R.id.checkBox);
                            CheckBox checkBox2 = findViewById(R.id.checkBox2);
                            CheckBox checkBox3 = findViewById(R.id.checkBox3);
                            CheckBox checkBox4 = findViewById(R.id.checkBox4);
                            CheckBox checkBox5 = findViewById(R.id.checkBox5);
                            CheckBox checkBox6 = findViewById(R.id.checkBox6);
                            CheckBox checkBox7 = findViewById(R.id.checkBox7);

                            List<CheckBox> checkBoxes = new ArrayList<>();
                            checkBoxes.add(checkBox);
                            checkBoxes.add(checkBox2);
                            checkBoxes.add(checkBox3);
                            checkBoxes.add(checkBox4);
                            checkBoxes.add(checkBox5);
                            checkBoxes.add(checkBox6);
                            checkBoxes.add(checkBox7);

                            for (int i = 0; i < checkBoxes.size(); i++) {
                                for (int j = 0; j < classesList.size(); j++) {
                                    if (Objects.equals(classesList.get(j), checkBoxes.get(i).getText().toString())) {
                                        checkBoxes.get(i).setChecked(true);
                                    }
                                }
                            }

//                            RecyclerView recyclerView = findViewById(R.id.recyclerViewStep3);
//                            recyclerView.setHasFixedSize(true);
//                            // создаем адаптер
//                            LinearLayoutManager llm = new LinearLayoutManager(fragment_step3.getContext());
//                            llm.setOrientation(LinearLayoutManager.VERTICAL);
//                            recyclerView.setLayoutManager(llm);
//                            // устанавливаем для списка адаптер
//                            recyclerView.setAdapter(new StateAdapter(fragment_step3.getContext(), new ArrayList<>(classesList)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }


        });
        thread.start();
    }

    public void getResultStep4(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                String apiKey = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_USERID, "");
                String response = RestApi.getResult(dashboardViewModel.getCurrentId(), apiKey);

                CheckBox checkBox = findViewById(R.id.checkBox);
                CheckBox checkBox2 = findViewById(R.id.checkBox2);
                CheckBox checkBox3 = findViewById(R.id.checkBox3);
                CheckBox checkBox4 = findViewById(R.id.checkBox4);
                CheckBox checkBox5 = findViewById(R.id.checkBox5);
                CheckBox checkBox6 = findViewById(R.id.checkBox6);
                CheckBox checkBox7 = findViewById(R.id.checkBox7);

                List<CheckBox> checkBoxes = new ArrayList<>();
                checkBoxes.add(checkBox);
                checkBoxes.add(checkBox2);
                checkBoxes.add(checkBox3);
                checkBoxes.add(checkBox4);
                checkBoxes.add(checkBox5);
                checkBoxes.add(checkBox6);
                checkBoxes.add(checkBox7);

                List<String> damages = new ArrayList<>();

                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isChecked()) {
                        damages.add(checkBoxes.get(i).getText().toString());
                    }
                }

                if (damages.size() != 0) {

                    RestApi.updateAutoDamage(apiKey, dashboardViewModel.getCurrentId(), damages);

                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          TextView textView = findViewById(R.id.textView);
                                          textView.setVisibility(View.VISIBLE);
                                          Button button = findViewById(R.id.buttonForSend3);
                                          button.setVisibility(View.INVISIBLE);
                                      }
                                  });

                    RestApi.getPrices(dashboardViewModel.getCurrentId(),apiKey);
                    response = RestApi.getResult(dashboardViewModel.getCurrentId(), apiKey);

                    String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.fragment_step4);

                            try {
                                JSONObject jsonObject = new JSONObject(finalResponse);
                                List<String> list = Arrays.asList(jsonObject.getString("prices").split(","));
                                List<String> clearList = new ArrayList<>();
                                for (int i = 0; i < list.size(); i++) {
                                    clearList.add(list.get(i).replace("[", "").replace("]", "").replace("{", "").replace("}", ""));
                                }

                                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                                View fragment_step4 = inflater.inflate(R.layout.fragment_step4, null, false);

                                RecyclerView recyclerView = findViewById(R.id.recyclerViewStep4);
                                recyclerView.setHasFixedSize(true);
                                // создаем адаптер
                                LinearLayoutManager llm = new LinearLayoutManager(fragment_step4.getContext());
                                llm.setOrientation(LinearLayoutManager.VERTICAL);
                                recyclerView.setLayoutManager(llm);
                                // устанавливаем для списка адаптер
                                recyclerView.setAdapter(new StateAdapterStep4(fragment_step4.getContext(), new ArrayList<>(clearList)));

                                ImageView imageView = findViewById(R.id.imageView2);
                                JSONObject jsonObjectBoxes = new JSONObject(jsonObject.getString("boxes"));
                                String boxes = jsonObjectBoxes.getString("boxes");
                                JSONArray classes = jsonObjectBoxes.getJSONArray("classes");
                                List<String> classesList = new ArrayList<>();
                                for (int i = 0; i < classes.length(); i++) {
                                    classesList.add(classes.getString(i));
                                }
                                imageView.setImageDrawable(imageViewForDownload.getDrawable());
                                imageViewForDownload.clearColorFilter();

                                BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                                Bitmap bitmap = bitmapDrawable.getBitmap();
                                Bitmap tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                Canvas canvas = new Canvas(tempBitmap);
                                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                paint.setColor(Color.GREEN);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setStrokeWidth(10);

                                Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
                                paint1.setColor(Color.RED);
                                paint1.setTextSize(40);

                                boxes = boxes.replace("[","").replace("]","").replace(" ","");
                                String[] boxesArray = boxes.split(",");
                                List<String> boxesList = Arrays.asList(boxesArray);
                                Log.i("Boxes", boxesList.toString());


                                for(int i = 0,j = 0; i < boxesList.size();i=i+4, j++){
                                    Log.i("Boxes", Integer.parseInt(boxesList.get(i)) + " " + Integer.parseInt(boxesList.get(i+1)) + " " + Integer.parseInt(boxesList.get(i+2))+ " " + Integer.parseInt(boxesList.get(i+3)));
                                    canvas.drawText(classesList.get(j), Integer.parseInt(boxesList.get(i)), Integer.parseInt(boxesList.get(i+1))-20, paint1);
                                    canvas.drawRect(Integer.parseInt(boxesList.get(i)), Integer.parseInt(boxesList.get(i+1)), Integer.parseInt(boxesList.get(i+2)), Integer.parseInt(boxesList.get(i+3)), paint);
                                }
                                imageView.setImageBitmap(tempBitmap);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Snackbar.make(view, "Необходимо что-то выбрать!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        thread.start();
    }

    private boolean isNewUser() {
        String email = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_EMAIL, "");
        String password = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_PASSWORD, "");
        //String firstName = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_FIRST_NAME, "");
        //String secondName = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_SECOND_NAME, "");
        String uuid = getSharedPreferences("setting", MODE_PRIVATE).getString(SAVE_USERID, "");

        return email.isEmpty() || password.isEmpty() || uuid.isEmpty();
    }

    private void deleteUserData() {
        SharedPreferences.Editor prefs = getSharedPreferences("setting", MODE_PRIVATE).edit();

        prefs.remove(SAVE_EMAIL);
        prefs.remove(SAVE_PASSWORD);
        //prefs.edit().remove(SAVE_FIRST_NAME).apply();
        //prefs.edit().remove(SAVE_SECOND_NAME).apply();
        prefs.remove(SAVE_USERID);
        prefs.apply();
    }
}