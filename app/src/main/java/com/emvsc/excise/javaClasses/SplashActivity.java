package com.emvsc.excise.javaClasses;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.emvsc.excise.R;
import com.emvsc.excise.dbClasses.DbHelper;
import com.emvsc.excise.interfaceClasses.AccessoriesApi;
import com.emvsc.excise.interfaceClasses.BodyBuildApi;
import com.emvsc.excise.interfaceClasses.ColorApi;
import com.emvsc.excise.interfaceClasses.DistrictsApi;
import com.emvsc.excise.interfaceClasses.MakeApi;
import com.emvsc.excise.interfaceClasses.ModelApi;
import com.emvsc.excise.interfaceClasses.ModelYearApi;
import com.emvsc.excise.interfaceClasses.RegDistrictApi;
import com.emvsc.excise.interfaceClasses.SeizeVehCatApi;
import com.emvsc.excise.interfaceClasses.WheelsApi;
import com.emvsc.excise.modelClasses.Accessories;
import com.emvsc.excise.modelClasses.Assecory;
import com.emvsc.excise.modelClasses.BodyBuild;
import com.emvsc.excise.modelClasses.BodybuildData;
import com.emvsc.excise.modelClasses.Color;
import com.emvsc.excise.modelClasses.ColorData;
import com.emvsc.excise.modelClasses.District;
import com.emvsc.excise.modelClasses.DistrictData;
import com.emvsc.excise.modelClasses.Make;
import com.emvsc.excise.modelClasses.MakeData;
import com.emvsc.excise.modelClasses.Model;
import com.emvsc.excise.modelClasses.ModelData;
import com.emvsc.excise.modelClasses.ModelYear;
import com.emvsc.excise.modelClasses.ModelYearData;
import com.emvsc.excise.modelClasses.RegisterNoDistricts;
import com.emvsc.excise.modelClasses.SeizeVehicleCat;
import com.emvsc.excise.modelClasses.SeizedVechicleCatData;
import com.emvsc.excise.modelClasses.WheelData;
import com.emvsc.excise.modelClasses.Wheels;
import com.emvsc.excise.utilClasses.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends AppCompatActivity {
    DbHelper mDbHelper = new DbHelper(this);
    List<MakeData> mMakeData = new ArrayList<>();
    List<ModelData> mModelData = new ArrayList<>();
    Make mMake;
    Model mModel;
    OkHttpClient okHttpClient;
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!isNetworkAvailable()) {
            if (mDbHelper.getAccessoriesData().size() != 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                        // Apply activity transition
                                        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                                        startActivity(loginIntent);
                                   // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();


                                } catch (InterruptedException e) {
                                    Log.e("InterruptedException", "run: " + e.toString());
                                }
                            }
                        });
                        thread.start();

                    }
                });

            }
        } else {
            if (mDbHelper.getAccessoriesData().size() == 0) {
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();
                retrofit = new Retrofit.Builder()
                        .baseUrl(Config.BaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                loadMakeList();
                loadModelList();
                loadAccessories();
                loadDistrict();
                loadSeizeCat();
                loadModelYear();
                loadColor();
                loadBodyBuild();
                loadWheels();
                loadRegDistrict();


            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                   // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                    finish();
                                } catch (InterruptedException e) {
                                    Log.e("InterruptedException", "run: " + e.toString());
                                }
                            }
                        });
                        thread.start();

                    }
                });
            }

        }

    }

    private void loadRegDistrict() {
        RegDistrictApi regDistrictApi = retrofit.create(RegDistrictApi.class);
        Call<RegisterNoDistricts> call = regDistrictApi.getRegDistricts();
        call.enqueue(new Callback<RegisterNoDistricts>() {
            @Override
            public void onResponse(Call<RegisterNoDistricts> call, Response<RegisterNoDistricts> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteRegDistricts();
                        RegisterNoDistricts registerNoDistricts = response.body();
                        List<RegisterNoDistricts.RegistationDistrict> mArrayList = registerNoDistricts.getRegistationDistrict();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addRegDistrict(mArrayList.get(i).getRegistrationdistrictid(), mArrayList.get(i).getRegistrationdistrictname());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterNoDistricts> call, Throwable t) {

            }
        });

    }

    private void loadBodyBuild() {
        BodyBuildApi bodyBuildApi = retrofit.create(BodyBuildApi.class);
        Call<BodyBuild> call = bodyBuildApi.getBodyBuild();
        call.enqueue(new Callback<BodyBuild>() {
            @Override
            public void onResponse(Call<BodyBuild> call, Response<BodyBuild> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteBodyBuild();
                        BodyBuild bodyBuild = response.body();
                        List<BodybuildData> mArrayList = bodyBuild.getBodybuild();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addBody(mArrayList.get(i).getBodybuild(), mArrayList.get(i).getBodybuildname());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BodyBuild> call, Throwable t) {

            }
        });

    }



    private void loadSeizeCat() {
        SeizeVehCatApi seizeVehCatApi = retrofit.create(SeizeVehCatApi.class);
        Call<SeizeVehicleCat> call = seizeVehCatApi.getSeizeCat();
        call.enqueue(new Callback<SeizeVehicleCat>() {
            @Override
            public void onResponse(Call<SeizeVehicleCat> call, Response<SeizeVehicleCat> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteSeizeCat();
                        SeizeVehicleCat seizeVehicleCat = response.body();
                        List<SeizedVechicleCatData> mArrayList = seizeVehicleCat.getSeizedVechicle();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addSeizeCat(mArrayList.get(i).getSiezedid(), mArrayList.get(i).getSeizedtype());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SeizeVehicleCat> call, Throwable t) {

            }
        });
    }

    private void loadModelYear() {
        ModelYearApi modelYearApi = retrofit.create(ModelYearApi.class);
        Call<ModelYear> call = modelYearApi.getModelYear();
        call.enqueue(new Callback<ModelYear>() {
            @Override
            public void onResponse(Call<ModelYear> call, Response<ModelYear> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteModelYear();
                        ModelYear modelYear = response.body();
                        List<ModelYearData> mArrayList = modelYear.getModelYear();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addVehicleModelYear(mArrayList.get(i).getModelid(), mArrayList.get(i).getModelyear());
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<ModelYear> call, Throwable t) {

            }
        });
    }

    private void loadColor() {
        ColorApi colorApi = retrofit.create(ColorApi.class);
        Call<Color> call = colorApi.getColors();
        call.enqueue(new Callback<Color>() {
            @Override
            public void onResponse(Call<Color> call, Response<Color> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteColor();
                        Color color = response.body();
                        List<ColorData> mArrayList = color.getColors();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addVehicleColor(mArrayList.get(i).getColorid(), mArrayList.get(i).getColorname());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Color> call, Throwable t) {

            }
        });
    }

    private void loadWheels() {
        WheelsApi wheelsApi = retrofit.create(WheelsApi.class);
        Call<Wheels> call = wheelsApi.getWheels();
        call.enqueue(new Callback<Wheels>() {
            @Override
            public void onResponse(Call<Wheels> call, Response<Wheels> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteWheels();
                        Wheels wheels = response.body();
                        List<WheelData> mArrayList = wheels.getWheelNumber();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addVehicleWheels(mArrayList.get(i).getWheelid(), mArrayList.get(i).getWheelnumber());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                               // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<Wheels> call, Throwable t) {

            }
        });
    }

    private void loadDistrict() {
        DistrictsApi districtsApi = retrofit.create(DistrictsApi.class);
        Call<District> call = districtsApi.getDistricts();
        call.enqueue(new Callback<District>() {
            @Override
            public void onResponse(Call<District> call, Response<District> response) {
                int success = response.body().getSuccess();
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteDistrictTable();
                        District district = response.body();
                        List<DistrictData> mArrayList = district.getDistricts();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addDistricts(mArrayList.get(i).getDistrictid(), mArrayList.get(i).getDistrictname());
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<District> call, Throwable t) {
                Log.e("district fail", "onFailure: " + t.toString());

            }
        });


    }

    private void loadAccessories() {
        AccessoriesApi accessoriesApi = retrofit.create(AccessoriesApi.class);
        Call<Accessories> call = accessoriesApi.getAccessories();
        call.enqueue(new Callback<Accessories>() {
            @Override
            public void onResponse(Call<Accessories> call, Response<Accessories> response) {
                int success = response.body().getSuccess();
                Log.e("success", "success: " + response.body().getSuccess());
                if (response.isSuccessful()) {
                    if (success == 1) {
                        mDbHelper.deleteAccessoriesTable();
                        Accessories accessories = response.body();
                        List<Assecory> mArrayList = accessories.getAssecories();
                        for (int i = 0; i < mArrayList.size(); i++) {
                            mDbHelper.addAccessories(mArrayList.get(i).getAccessoryid(), mArrayList.get(i).getAccessoryname());
                        }
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Call<Accessories> call, Throwable t) {
                Log.e("accessories fail", "onFailure: " + t.toString());
            }
        });
    }

    private void loadModelList() {
        ModelApi modelApi = retrofit.create(ModelApi.class);
        Call<Model> call = modelApi.getModelList();
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                mModel = response.body();
                mModelData = mModel.getModel();
                int success = mModel.getSuccess();
                if (success == 1) {
                    mDbHelper.deleteModelTable();
                    for (int i = 0; i < mModelData.size(); i++) {
                        String parent_id = mModelData.get(i).getMakeParentId();
                        String model_id = mModelData.get(i).getSubmakeid();
                        String model_name = mModelData.get(i).getSubmakename();
                        mDbHelper.addModel(parent_id, model_id, model_name);

                    }

                } else {

                }

            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Log.e("model fail", "onFailure: " + t.toString());
            }
        });
    }

    private void loadMakeList() {
        MakeApi makeApi = retrofit.create(MakeApi.class);
        Call<Make> call = makeApi.getMakeList();
        call.enqueue(new Callback<Make>() {
            @Override
            public void onResponse(Call<Make> call, Response<Make> response) {
                mMake = response.body();
                mMakeData = mMake.getMake();
                int success = mMake.getSuccess();
                if (success == 1) {
                    mDbHelper.deleteMakeTable();
                    for (int i = 0; i < mMakeData.size(); i++) {
                        String make_id = mMakeData.get(i).getMakeid();
                        String make_name = mMakeData.get(i).getMakename();
                        mDbHelper.addMake(make_id, make_name);
                    }

                } else {

                }


            }

            @Override
            public void onFailure(Call<Make> call, Throwable t) {
                Log.e("make fail", "onFailure: " + t.toString());
            }
        });


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                isAvailable = true;

            }
            return isAvailable;



    }
}