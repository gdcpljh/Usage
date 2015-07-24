package com.example.aditya.usage.Fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.aditya.usage.Adapter.ProcessAdapter;
import com.example.aditya.usage.Data.ProcessData;
import com.example.aditya.usage.Event.ProcessInfoEvent;
import com.example.aditya.usage.R;
import com.example.aditya.usage.Service.ProcessDataUpdateService;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by aditya on 10/07/15.
 */
public class MemoryUsageFragment extends Fragment {

    private ListView lvProcesses;
    private ProcessAdapter adapterProcess;
    private Intent intentService;
    private static final String TAG = "MemoryUsageFragment";
    private final ArrayList<ProcessData> data = new ArrayList<ProcessData>();
    private ProcessAdapter processAdapter;


    public void onResume() {

        super.onResume();
        getActivity().startService(intentService);
        EventBus.getDefault().register(this);
    }


    public void onPause() {

        super.onPause();
        getActivity().stopService(intentService);
        EventBus.getDefault().unregister(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.memory_usage_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        intentService = new Intent(getActivity(), ProcessDataUpdateService.class);

        lvProcesses = (ListView) getActivity().findViewById(R.id.lv_memory_usage);
        processAdapter = new ProcessAdapter(getActivity(), R.id.tv_process_name, data);
        lvProcesses.setAdapter(processAdapter);

        lvProcesses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProcessData unit = data.get(position);
                openAppSettings(unit.getPackageName());
            }
        });
    }


    public void onEvent(ProcessInfoEvent event) {

        ArrayList<ProcessData> userApps = new ArrayList<ProcessData>();
        ArrayList<ProcessData> systemApps = new ArrayList<ProcessData>();
        ArrayList<ProcessData> unknownApps = new ArrayList<ProcessData>();

        ArrayList<ProcessData> list = event.getProcessDataList();
        data.clear();
        for(ProcessData unit : list) {

            // Don't show usage in list
            if(unit.getPackageName().equals(getActivity().getPackageName())) {
                continue;
            }

            if(unit.isUnknown()) {
                unknownApps.add(unit);
            } else if(unit.isSystemApp()) {
                systemApps.add(unit);
            } else {
                userApps.add(unit);
            }
        }

        data.addAll(userApps);
        data.addAll(unknownApps);
        data.addAll(systemApps);

        processAdapter.setHeaderUnknownApps(userApps.size());
        processAdapter.setHeaderSystemApps(userApps.size() + unknownApps.size());

        processAdapter.notifyDataSetChanged();
        getActivity().findViewById(R.id.pb_memory_usage).setVisibility(View.GONE);
    }


    private void openAppSettings(String packageName) {

        try {
            //Open the specific App Info page:
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);

        } catch ( ActivityNotFoundException e ) {
            e.printStackTrace();
            //Open the generic Apps page:
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(intent);
        }
    }
}