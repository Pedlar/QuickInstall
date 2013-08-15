package com.hostgator.quickinstall;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class InstallApplication extends SherlockActivity {
    JSONArray groupList;
    ArrayList<GroupParent> groups;
    ExpandableListView installList;
    InstallListAdapter installAdapter;
    
    public static final int ID = R.id.name;
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manage, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            updateList();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Manage Apps")
        //.setIcon(R.drawable.ic_manage)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install_apps);
        installList = (ExpandableListView)findViewById(R.id.apps_list);
        installList.setDivider(null);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View customNav = LayoutInflater.from(this).inflate(R.layout.action_manage, null);

        ((LinearLayout)customNav.findViewById(R.id.manage_layout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(InstallApplication.this, ManageApplications.class);
                    InstallApplication.this.startActivity(myIntent);
                }
            });
        //Attach to the action bar
        getSupportActionBar().setCustomView(customNav);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        updateList();
        
    }
    
    private void updateList() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog loading = new ProgressDialog(InstallApplication.this);
            @Override
            protected void onPostExecute( Void result )  {
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
                loading.dismiss();
                return;
            }
            
            @Override
            protected void onPreExecute() {
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setMessage("Loading list...");
                loading.show();
            }
            
            @Override
            protected Void doInBackground(Void... params) {
                String result = MainActivity.qApi.getInstallApps();
                try {
                    groupList = new JSONObject(result)
                                                        .getJSONObject("cpanelresult")
                                                        .getJSONArray("data");
                
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                return null;
            }
            
        }.execute();
    }
    
    private void setupList() {
        Log.d("QuickInstall", "Length:" + groupList.length());
        groups = new ArrayList<GroupParent>();
        for(int i = 0; i < groupList.length(); i++) {
            JSONObject obj;
            try {
                obj = groupList.getJSONObject(i);
                GroupParent group = new GroupParent();
                group.setName(obj.getString("name") + " Software");
                JSONArray apps = obj.getJSONArray("apps");
                ArrayList<GroupChild> children = new ArrayList<GroupChild>();
                for(int x = 0; x < apps.length(); x++) {
                    JSONObject app = apps.getJSONObject(x);
                    GroupChild child = new GroupChild();
                    child.setName(app.getString("name"));
                    child.setTag(app.getString("pkgname"));
                    child.setIconUrl(app.getString("icon"));
                    child.setIcon(MainActivity.qApi.getIcon(app.getString("icon")));
                    children.add(child);
                }
                group.setItems(children);
                groups.add(group);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        if(installAdapter == null) {   
            installAdapter = new InstallListAdapter(InstallApplication.this, groups);
            installList.setAdapter(installAdapter);
//            installList.setOnItemClickListener(listListener);
            installList.setOnChildClickListener(new OnChildClickListener () {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                        int childPosition, long id) {
                    Intent myIntent = new Intent(InstallApplication.this, InstallActivity.class);
                    myIntent.putExtra("pkgName", (String)v.getTag());
                    InstallApplication.this.startActivity(myIntent);
                    return false;
                }
            });
        } else {
            //installAdapter.setData(groups);
            installAdapter.notifyDataSetChanged();
        }
        
    }
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            setupList();
        }
    };
    
}