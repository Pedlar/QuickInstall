package com.hostgator.quickinstall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockDialogFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ManageApplications extends SherlockActivity {
    JSONArray manageList;
    ArrayList<HashMap<String, String>> installs;
    ListView installList;
    ManageListAdapter installAdapter;
    
    public static final int ID = R.id.name;
    
    /*    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manage, menu);
        return true;
    }
    */
    
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_apps);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View customNav = LayoutInflater.from(this).inflate(R.layout.action_install, null);

        ((LinearLayout)customNav.findViewById(R.id.ac_install_layout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(ManageApplications.this, InstallApplication.class);
                    ManageApplications.this.startActivity(myIntent);
                }
            });
        //Attach to the action bar
        getSupportActionBar().setCustomView(customNav);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        installList = (ListView)findViewById(R.id.manageView);
        installList.setItemsCanFocus(true);
        updateList();
    }
    
    private void updateList() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog loading = new ProgressDialog(ManageApplications.this);
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
                String result = MainActivity.qApi.getAppList();
                try {
                    manageList = new JSONObject(result)
                                                        .getJSONObject("cpanelresult")
                                                        .getJSONArray("data")
                                                        .getJSONObject(0)
                                                        .getJSONArray("installs");
                
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                return null;
            }
            
        }.execute();
    }
    
    OnItemClickListener listListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            final String pkgId = (String)v.getTag(ID);
            final SherlockDialogFragment pkgDialog = new SherlockDialogFragment();
            final int pos = position;
            final LinearLayout action_layout = (LinearLayout)v.findViewById(R.id.app_actions);
            ImageView uninstallButton = (ImageView)v.findViewById(R.id.uninstall_app);
            ImageView upgradeButton = (ImageView)v.findViewById(R.id.upgrade_app);
            
            final AnimationListener makeGone = new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    action_layout.setVisibility(View.GONE);
                }
            };
            
            if(action_layout.getVisibility() == View.GONE) {
                AnimationSet set = new AnimationSet(true);
    
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(300);
                set.addAnimation(animation);
    
                animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -0.6f, Animation.RELATIVE_TO_SELF, 0.0f
                );
                animation.setDuration(500);
                set.addAnimation(animation);
    
                LayoutAnimationController controller =
                    new LayoutAnimationController(set, 0.25f);
                action_layout.setLayoutAnimation(controller);
                action_layout.setVisibility(View.VISIBLE);
                if(installAdapter.getItem(position).get("updateavail") != null) {
                    if(!(installAdapter.getItem(position).get("updateavail").equals("none")))
                        upgradeButton.setVisibility(View.VISIBLE);
                    else
                        upgradeButton.setVisibility(View.GONE);
                } else {
                    upgradeButton.setVisibility(View.GONE);
                }
                action_layout.startLayoutAnimation();
            } else {
                AnimationSet set = new AnimationSet(true);
                
                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(300);
                set.addAnimation(animation);
    
                animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -0.6f
                );
                animation.setDuration(500);
                animation.setAnimationListener(makeGone);
                set.addAnimation(animation);
    
                LayoutAnimationController controller =
                    new LayoutAnimationController(set, 0.25f);
                action_layout.setLayoutAnimation(controller);
                action_layout.startLayoutAnimation();
            }
            
            uninstallButton.setOnClickListener(new View.OnClickListener() {  
                @Override
                public void onClick(View v) {
                    //removeInstall(pkgId, pos);
                    new AlertDialog.Builder(ManageApplications.this)
                    .setTitle("Uninstall")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Uninstall", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeInstall(pkgId, pos);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
            upgradeButton.setOnClickListener(new View.OnClickListener() {  
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ManageApplications.this)
                    .setTitle("Upgrade")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            upgradeInstall(pkgId);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
            /*
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                                       LayoutParams.WRAP_CONTENT);
            RelativeLayout layout = new RelativeLayout(pkgDialog.getContext());
            layout.setLayoutParams(layoutParams);
            
            /******   TextView Layout    *****
            
            TextView urlView = new TextView(pkgDialog.getContext());
            urlView.setText("Url: " + ((TextView) v.findViewById(R.id.url_path)).getText());
            urlView.setId(2);
            
            RelativeLayout.LayoutParams urlViewLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
            urlViewLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            urlViewLayout.addRule(RelativeLayout.ALIGN_LEFT);
            
            /****** Action Layout *****
            
            TextView actionLabel = new TextView(pkgDialog.getContext());
            actionLabel.setText("Actions:");
            actionLabel.setId(3);
            RelativeLayout.LayoutParams actionLabelLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
            actionLabelLayout.addRule(RelativeLayout.BELOW, urlView.getId());
            actionLabelLayout.addRule(RelativeLayout.ALIGN_LEFT);
            
            Button uninstallButton = new Button(pkgDialog.getContext());
            uninstallButton.setText("Uninstall");
            uninstallButton.setId(4);
            
            uninstallButton.setOnClickListener(new View.OnClickListener() {  
                @Override
                public void onClick(View v) {
                    //removeInstall(pkgId, pos);
                    pkgDialog.dismiss();
                    new AlertDialog.Builder(ManageApplications.this)
                    .setTitle("Uninstall")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Uninstall", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeInstall(pkgId, pos);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
            
            RelativeLayout.LayoutParams uninstallButtonLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            uninstallButtonLayout.addRule(RelativeLayout.RIGHT_OF, actionLabel.getId());
            uninstallButtonLayout.addRule(RelativeLayout.BELOW, actionLabel.getId());
            uninstallButtonLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);

            /******       Upgrade Button   *****
            
            Button upgradeButton = new Button(pkgDialog.getContext());
            upgradeButton.setText("Upgrade");
            upgradeButton.setId(5);
            
            upgradeButton.setOnClickListener(new View.OnClickListener() {  
                @Override
                public void onClick(View v) {
                    pkgDialog.dismiss();
                    new AlertDialog.Builder(ManageApplications.this)
                    .setTitle("Upgrade")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            upgradeInstall(pkgId);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
            
            RelativeLayout.LayoutParams upgradeButtonLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            upgradeButtonLayout.addRule(RelativeLayout.RIGHT_OF, uninstallButton.getId());
            upgradeButtonLayout.addRule(RelativeLayout.BELOW, actionLabel.getId());
            upgradeButtonLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
            
            /******       Button Layout    *****
            
            Button closeButton = new Button(pkgDialog.getContext());
            closeButton.setId(1);
            closeButton.setText("Close");
            
            RelativeLayout.LayoutParams closeButtonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            closeButtonParams.setMargins(0, 10, 0, 2);
            closeButtonParams.addRule(RelativeLayout.BELOW, uninstallButton.getId());
            closeButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            
            closeButton.setOnClickListener(new View.OnClickListener() {  
                @Override
                public void onClick(View v) {
                    pkgDialog.dismiss();
                }
            });
            
            /**************************
           
            layout.addView(urlView, urlViewLayout);
            layout.addView(actionLabel, actionLabelLayout);
            layout.addView(uninstallButton, uninstallButtonLayout);
            if(!(installAdapter.getItem(position).get("updateavail").equals("none"))) {
                layout.addView(upgradeButton, upgradeButtonLayout);
            }
            layout.addView(closeButton, closeButtonParams);
            
            pkgDialog.setContentView(layout);
            pkgDialog.setTitle("Manage " + ((TextView) v.findViewById(R.id.name)).getText());
            
            pkgDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            pkgDialog.show();
            */
            
        }
    };
    
    private void setupList() {
        Log.d("QuickInstall", "Length:" + manageList.length());
        installs = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < manageList.length(); i++) {
            JSONObject obj;
            HashMap<String, String> install = new HashMap<String, String>();;
            try {
                obj = manageList.getJSONObject(i);
                Iterator jsIter = obj.keys();
                while(jsIter.hasNext()) {
                    String key = jsIter.next().toString();
                    install.put(key, obj.getString(key));
                    Log.d("QuickInstall", key + ": " + obj.getString(key));
                }
                installs.add(install);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(installAdapter == null) {   
            installAdapter = new ManageListAdapter(ManageApplications.this, installs);
            installList.setAdapter(installAdapter);
            installList.setOnItemClickListener(listListener);
        } else {
            installAdapter.setData(installs);
            installAdapter.notifyDataSetChanged();
        }
    }
    
    public void removeInstall(final String id, int position) {
        installAdapter.removeItem(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = MainActivity.qApi.removeApplication(id);
                Log.d("QuickInstall", result);
            }
        }).start();
    }
    
    public void upgradeInstall(final String id) {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog loading = new ProgressDialog(ManageApplications.this);
            @Override
            protected void onPostExecute( Void result )  {
                loading.dismiss();
                updateList();
                return;
            }
            
            @Override
            protected void onPreExecute() {
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setMessage("Upgrading");
                loading.show();
            }
            
            @Override
            protected Void doInBackground(Void... params) {
                String result = MainActivity.qApi.upgradeApplication(id);
                Log.d("QuickInstall", result);
                return null;
            }
            
        }.execute();
    }
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            setupList();
        }
    };
    
}