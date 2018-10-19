package com.emvsc.excise.fragmentClasses;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.emvsc.excise.R;
import com.emvsc.excise.adapterClasses.SeizeAprovedAdapter;
import com.emvsc.excise.interfaceClasses.OnItemClickListener;
import com.emvsc.excise.interfaceClasses.WhmSeizedApi;
import com.emvsc.excise.javaClasses.FormBDetailsActivity;
import com.emvsc.excise.modelClasses.CustomMessageEvent;
import com.emvsc.excise.modelClasses.RefreshTab1;
import com.emvsc.excise.modelClasses.WhmSeizeVehicle;
import com.emvsc.excise.modelClasses.WhmSeizeVehicleData;
import com.emvsc.excise.utilClasses.Config;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class OneFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager mLayoutManager;
    SeizeAprovedAdapter seizeAprovedAdapter;
    ImageView frame_one_no_record;
    List<WhmSeizeVehicleData> list = new ArrayList<>();
    LinearLayout frame_one_norecord;
    public OneFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_one, container, false);
        setUi(v);
        EventBus.getDefault().register(this);
        return v;
    }
    private void setUi(View v) {
        mRecyclerView = v.findViewById(R.id.seize_approve_list);
        mSwipeRefreshLayout = v.findViewById(R.id.seize_approve_swipe);
        frame_one_no_record = v.findViewById(R.id.frame_one_no_record);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadApprovedList();
            }
        });
        loadApprovedList();
        setHasOptionsMenu(true);
        }
    public void loadApprovedList() {
        mSwipeRefreshLayout.setRefreshing(true);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WhmSeizedApi whmSeizedApi = retrofit.create(WhmSeizedApi.class);
        Call<WhmSeizeVehicle> call = whmSeizedApi.getApprovedList();
        call.enqueue(new Callback<WhmSeizeVehicle>() {
            @Override
            public void onResponse(Call<WhmSeizeVehicle> call, Response<WhmSeizeVehicle> response) {
                final WhmSeizeVehicle whmSeizeVehicle =  response.body();
                int success = whmSeizeVehicle.getSuccess();
                Log.e("Success", "onResponse: "+success );
                if (response.isSuccessful()){
                    if (success == 1){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                        list = whmSeizeVehicle.getSeizedVechiclesData();
                        CustomMessageEvent customMessageEvent = new CustomMessageEvent();
                        customMessageEvent.setTotalSeize(String.valueOf(list.size()));
                        EventBus.getDefault().post(customMessageEvent);
                        seizeAprovedAdapter = new SeizeAprovedAdapter(list, new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(WhmSeizeVehicleData item) {
                                        Log.e("OnClick", "onItemClick: "+item.getVehicleImages());
                                        Intent intent = new Intent(getActivity(), FormBDetailsActivity.class);
                                        Bundle args = new Bundle();
                                        args.putString("form_no", item.getFormserialno());
                                        args.putString("seize_time", item.getSiezedtime());
                                        args.putString("seize_date", item.getDatesiezeddate());
                                        args.putString("approved_date", item.getApprovedDate());
                                        args.putString("approved_time", item.getApprovedTime());
                                        args.putString("squad_no", item.getMobilesquadno());
                                        args.putString("inspector_name", item.getUsername());
                                        args.putString("lat", item.getSeizedlocationlat());
                                        args.putString("lng", item.getSeizedlocationlong());
                                        args.putString("seize_type", item.getSeizedtype());
                                        args.putString("chasis_no", item.getChasisno());
                                        args.putString("engine_no", item.getEngineno());
                                        args.putString("vehicle_id", item.getVechileid());
                                        if (item.getDriveraddress().equals("") || item.getDriveraddress().equals(null)){
                                            args.putString("reg_no", "N/A");
                                        }else {
                                            args.putString("reg_no", item.getVechileregistrationno());
                                        }
                                        args.putString("make_name", item.getMakename());
                                        args.putString("model_name", item.getSubmakename());
                                        args.putString("model_year", item.getModelyear());
                                        args.putString("vehicle_type", item.getVehicletype());
                                        args.putString("body_build", item.getBodybuildname());
                                        args.putString("color", item.getColorname());
                                        args.putString("transmission", item.getTransmission());
                                        args.putString("assembely", item.getAssembely());
                                        args.putString("wheels", item.getWheelnumber());
                                        args.putString("engine_type", item.getEnginetype());
                                        args.putString("engine_capicity", item.getVehicleengineCapcaity());
                                        args.putString("mileage", item.getMileage());
                                        if (item.getVechicledescription().equals("")){
                                            args.putString("description", "N/A");
                                        }else {
                                            args.putString("description", item.getVechicledescription());
                                        }
                                        args.putString("driver_name", item.getDrivername());
                                        args.putString("driver_cnic", item.getDrivercnicno());
                                        args.putString("driver_mob", item.getDrivermobileno());
                                        if (item.getDriveraddress().equals("") || item.getDriveraddress().equals(null)){
                                            args.putString("driver_address", "N/A");
                                        }else {
                                            args.putString("driver_address", item.getDriveraddress());
                                        }

                                        if (item.getVechileownername().equals("") || item.getVechileownername().equals(null)){
                                            args.putString("owner_name", "N/A");
                                        }else {
                                            args.putString("owner_name", item.getVechileownername());
                                        }

                                        if (item.getVechileownercnic().equals("") || item.getVechileownercnic().equals(null) || item.getVechileownercnic().equals("0")){
                                            args.putString("owner_cnic", "N/A");
                                        }else {
                                            args.putString("owner_cnic", item.getVechileownercnic());
                                        }

                                        if (item.getVechileownermobileno().equals("") || item.getVechileownermobileno().equals(null) || item.getVechileownermobileno().equals("0")){
                                            args.putString("owner_mob", "N/A");
                                        }else {
                                            args.putString("owner_mob", item.getVechileownermobileno());
                                        }


                                        args.putSerializable("vehicle_images", (Serializable) item.getVehicleImages());
                                        args.putSerializable("accessories_list", (Serializable) item.getVehicleAccessories());
                                        intent.putExtra("BUNDLE",args);
                                        startActivity(intent);
                                    }
                                });
                                mRecyclerView.setAdapter(seizeAprovedAdapter);
                                mSwipeRefreshLayout.setRefreshing(false);

                            }
                        });

                    }
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            frame_one_no_record.setVisibility(View.VISIBLE);

                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<WhmSeizeVehicle> call, Throwable t) {
                Log.e("Failure", "onFailure: "+t.toString() );
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        frame_one_no_record.setVisibility(View.VISIBLE);

                    }
                });

            }
        });
    }

    public void beginSearch(String query) {
        Log.e("QueryFragment", query);
        final List<WhmSeizeVehicleData> filterModelList = filter(list, query);
        seizeAprovedAdapter.setFilter(filterModelList);

    }
    private List<WhmSeizeVehicleData> filter(List<WhmSeizeVehicleData> list, String query){
        query = query.toLowerCase();
        final List<WhmSeizeVehicleData> filterList = new ArrayList<>();
        for (WhmSeizeVehicleData model : list){
            final String  form_no = model.getFormserialno().toLowerCase().trim();
            final String  chasis_no = model.getChasisno().toLowerCase().trim();
            final String  seize_type = model.getSeizedtype().toLowerCase().trim();
            final String  district_name = model.getDistrictname().toLowerCase().trim();
            if (form_no.startsWith(query)){
                filterList.add(model);
            }else if (chasis_no.startsWith(query)){
                filterList.add(model);
            }else if (seize_type.contains(query)){
                filterList.add(model);
            }else if (district_name.contains(query)){
                filterList.add(model);
            }
        }
        return filterList;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadApprovedList();
    }

    @Subscribe
    public void RefreshEvent(RefreshTab1 event){
        if (event.getRefreshStatus() == 1){
            loadApprovedList();
        }else {

        }

        }
}
