package com.emvsc.excise.fragmentClasses;


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


import com.emvsc.excise.R;
import com.emvsc.excise.adapterClasses.SeizeInspectorAdapter;
import com.emvsc.excise.interfaceClasses.WhmSeizedApi;
import com.emvsc.excise.modelClasses.CustomMessageEvent2;
import com.emvsc.excise.modelClasses.RefreshTab1;
import com.emvsc.excise.modelClasses.WhmSeizeInspectorVehicle;
import com.emvsc.excise.modelClasses.WhmSeizeInspectorVehicleData;
import com.emvsc.excise.utilClasses.Config;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class TwoFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager mLayoutManager;
    SeizeInspectorAdapter seizeInspectorAdapter;
    List<WhmSeizeInspectorVehicleData> list;
    ImageView frame_two_no_record;
    public TwoFragment() {
        // Required empty public constructor
    }
  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_two, container, false);
        setUi(v);
        EventBus.getDefault().register(this);
        return v;
    }

    private void setUi(View v) {
        mRecyclerView = v.findViewById(R.id.seize_inspector_list);
        mSwipeRefreshLayout = v.findViewById(R.id.seize_inspector_swipe);
        frame_two_no_record = v.findViewById(R.id.frame_two_no_record);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadInspectorList();
            }
        });
        loadInspectorList();


    }
    public void loadInspectorList() {
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
        Call<WhmSeizeInspectorVehicle> call = whmSeizedApi.getInsepctorList();
        call.enqueue(new Callback<WhmSeizeInspectorVehicle>() {
            @Override
            public void onResponse(Call<WhmSeizeInspectorVehicle> call, Response<WhmSeizeInspectorVehicle> response) {
                final WhmSeizeInspectorVehicle whmSeizeInspectorVehicle =  response.body();
                int success = whmSeizeInspectorVehicle.getSuccess();
                Log.e("Success", "onResponse: "+success );
                if (response.isSuccessful()){
                    if (success == 1){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                list = whmSeizeInspectorVehicle.getSeizedData();
                                CustomMessageEvent2 customMessageEvent = new CustomMessageEvent2();
                                customMessageEvent.setTotalInspectorReports(String.valueOf(list.size()));
                                EventBus.getDefault().post(customMessageEvent);
                                seizeInspectorAdapter = new SeizeInspectorAdapter(list);
                                mRecyclerView.setAdapter(seizeInspectorAdapter);
                                seizeInspectorAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    }else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                frame_two_no_record.setVisibility(View.GONE);

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<WhmSeizeInspectorVehicle> call, Throwable t) {
                Log.e("Failure", "onFailure: "+t.toString() );
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        frame_two_no_record.setVisibility(View.GONE);
                    }
                });


            }
        });
    }

    public void beginSearch(String query) {
        Log.e("QueryFragment", query);
        final List<WhmSeizeInspectorVehicleData> filterModelList = filter(list, query);
        seizeInspectorAdapter.setFilter(filterModelList);

    }
    private List<WhmSeizeInspectorVehicleData> filter(List<WhmSeizeInspectorVehicleData> list, String query){
        query = query.toLowerCase();
        final List<WhmSeizeInspectorVehicleData> filterList = new ArrayList<>();
        for (WhmSeizeInspectorVehicleData model : list){
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
        loadInspectorList();
    }
    @Subscribe
    public void RefreshEvent(RefreshTab1 event){
        if (event.getRefreshStatus() == 1){
            loadInspectorList();
        }else {

        }

    }
}

