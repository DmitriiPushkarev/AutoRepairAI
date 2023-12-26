package com.example.autorepairai;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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
import java.util.List;

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
            "ВАЗ-1111 (ОКА)"};

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

                if (responseString.contains("2")) {
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

                if (responseString.contains("3")) {
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

                                RecyclerView recyclerView = findViewById(R.id.recyclerViewStep3);
                                recyclerView.setHasFixedSize(true);
                                // создаем адаптер
                                LinearLayoutManager llm = new LinearLayoutManager(fragment_step3.getContext());
                                llm.setOrientation(LinearLayoutManager.VERTICAL);
                                recyclerView.setLayoutManager(llm);
                                // устанавливаем для списка адаптер
                                recyclerView.setAdapter(new StateAdapter(fragment_step3.getContext(), new ArrayList<>(classesList)));
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

                            RecyclerView recyclerView = findViewById(R.id.recyclerViewStep3);
                            recyclerView.setHasFixedSize(true);
                            // создаем адаптер
                            LinearLayoutManager llm = new LinearLayoutManager(fragment_step3.getContext());
                            llm.setOrientation(LinearLayoutManager.VERTICAL);
                            recyclerView.setLayoutManager(llm);
                            // устанавливаем для списка адаптер
                            recyclerView.setAdapter(new StateAdapter(fragment_step3.getContext(), new ArrayList<>(classesList)));
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

                RecyclerView recyclerView = findViewById(R.id.recyclerViewStep3);
                StateAdapter adapter = (StateAdapter) recyclerView.getAdapter();
                List<String> damages = adapter.getStrList();

                RestApi.updateAutoDamage(apiKey, dashboardViewModel.getCurrentId(), damages);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.fragment_step4);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            List<String> list = Arrays.asList(jsonObject.getString("prices").split(","));
                            List<String> clearList = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                clearList.add(list.get(i).replace("[", "").replace("]", "").replace("{", "").replace("}", "").replace(" ", ""));
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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