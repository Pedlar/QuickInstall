package com.hostgator.quickinstall;

import com.actionbarsherlock.app.SherlockActivity;
import com.hostgator.quickinstall.InstallActivity.ValuePair;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InstallActivity extends SherlockActivity {
    
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_CHECKBOX = 1;
    private static final int TYPE_HIDDEN = 2;
    private static final int TYPE_SELECT = 3;
    protected static final int HANDLE_SETUP = 1;
    protected static final int HANDLE_INSTALL = 2;
    private String pkgName;
    JSONObject appInfo;
    //ArrayList<HashMap<String, HashMap<String, View>>> fieldViews;
    ArrayList<Fields> fieldViews = new ArrayList<Fields>();
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras != null) {
            pkgName = extras.getString("pkgName");
        }
        
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog loading = new ProgressDialog(InstallActivity.this);
            @Override
            protected void onPostExecute( Void result )  {
                Message msg = handler.obtainMessage();
                msg.what = HANDLE_SETUP;
                handler.sendMessage(msg);
                loading.dismiss();
                return;
            }
            
            @Override
            protected void onPreExecute() {
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setMessage("Loading...");
                loading.show();
            }
            
            @Override
            protected Void doInBackground(Void... params) {
                String result = MainActivity.qApi.getApplicationInfo(pkgName);
                try {
                    appInfo = new JSONObject(result)
                                                    .getJSONObject("cpanelresult")
                                                    .getJSONArray("data")
                                                    .getJSONObject(0);
                
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                return null;
            }
        }.execute();
    }
    
    public void setupLayout() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        
        try {
            View hr = new View(this); hr.setBackgroundColor(0xFF000000);
            
            LinearLayout titleLayout = new LinearLayout(this);
            LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(5, 8, 5, 4);
            //titleLayout.setLayoutParams(titleParams);
            titleLayout.setOrientation(LinearLayout.HORIZONTAL);
            titleLayout.setPadding(15, 10, 0, 15);
            //titleLayout.setBackgroundColor(Color.parseColor("#f9f9f9"));
            titleLayout.setBackgroundResource(R.drawable.install_border);
            
            ImageView icon = new ImageView(this);
            icon.setLayoutParams(new LayoutParams(21, 21));
            icon.setImageBitmap(MainActivity.qApi.getIcon(appInfo.getString("icon")));
            TextView name = new TextView(this);
            name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            name.setText(appInfo.getString("name"));
            name.setPadding(10, 0, 0, 0);
            
            titleLayout.addView(icon);
            titleLayout.addView(name);
            
            LinearLayout detailsLayout = new LinearLayout(this);
            LayoutParams detailsParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            detailsParams.setMargins(5, 4, 5, 4);
            detailsLayout.setOrientation(LinearLayout.VERTICAL);
            detailsLayout.setPadding(15, 10, 0, 15);
            //titleLayout.setBackgroundColor(Color.parseColor("#f9f9f9"));
            detailsLayout.setBackgroundResource(R.drawable.install_border);
            
            TextView desc = new TextView(this);
            TextView version = new TextView(this);
            TextView size = new TextView(this);
            TextView site = new TextView(this);
            
            desc.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            desc.setText(appInfo.getString("description"));
            version.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            version.setText("Version: " + appInfo.getString("version"));
            size.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            size.setText("Install Size: " + appInfo.getString("size"));
            site.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            site.setText("Official Site: " + appInfo.getString("url"));
            
            detailsLayout.addView(desc);
            detailsLayout.addView(version);
            detailsLayout.addView(size);
            detailsLayout.addView(site);
            
            LinearLayout fieldLayout = new LinearLayout(this);
            LayoutParams fieldParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            fieldParams.setMargins(5, 4, 5, 8);
            fieldLayout.setOrientation(LinearLayout.VERTICAL);
            fieldLayout.setPadding(15, 10, 0, 15);
            //fieldLayout.setBackgroundColor(Color.parseColor("#f9f9f9"));
            fieldLayout.setBackgroundResource(R.drawable.install_border);
            
            TextView appUrlDesc = new TextView(this);
            appUrlDesc.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            appUrlDesc.setText("Domain Name:");
            
            Spinner domSpinner = new Spinner(this);
            JSONArray domainArray = appInfo.getJSONArray("domains");
            ArrayList<ValuePair> domList = new ArrayList<ValuePair>();
            for(int i = 0; i < domainArray.length(); i++) {
                String id = domainArray.getJSONObject(i).getString("domain") + "|" + domainArray.getJSONObject(i).getString("dir");
                String value = domainArray.getJSONObject(i).getString("domain");
                ValuePair dom = new ValuePair(id, value);
                domList.add(dom);
            }
            ArrayAdapter<ValuePair> adapter = new ArrayAdapter<ValuePair>(this, android.R.layout.simple_spinner_item, domList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            domSpinner.setTag("app_domain");
            domSpinner.setAdapter(adapter);
            
            TextView appUrlPathDesc = new TextView(this);
            appUrlPathDesc.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            appUrlPathDesc.setText("URL Path: (folder you want to install to)");
            
            EditText urlPath = new EditText(this);
            urlPath.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            urlPath.setHint("URL Path (ex: blog)");
            urlPath.setTag("url_path");
            urlPath.setSingleLine();
            
            fieldViews.add(new Fields(TYPE_SELECT, (View)domSpinner));
            fieldViews.add(new Fields(TYPE_TEXT, (View)urlPath));
            
            fieldLayout.addView(appUrlDesc);
            fieldLayout.addView(domSpinner);
            fieldLayout.addView(urlPath);
            fieldLayout.addView(hr, LayoutParams.MATCH_PARENT, 1);
            
            TableLayout tableLayout = buildTableLayout();
            TableLayout.LayoutParams layoutparams=new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            fieldLayout.addView(tableLayout, layoutparams);
            
            
            Button installNowButton = new Button(this);
            installNowButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            installNowButton.setText("Install Now!");
            installNowButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    installNow();
                }
            });
            
            fieldLayout.addView(installNowButton);
            
            rootLayout.addView(titleLayout, titleParams);
            rootLayout.addView(detailsLayout, detailsParams);
            rootLayout.addView(fieldLayout, fieldParams);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(rootLayout);
        setContentView(scrollView);
    }
    
    public int convertType(String type) {
        if(type.equals("text")) {
            return TYPE_TEXT;
        } else if(type.equals("checkbox")) {
            return TYPE_CHECKBOX;
        } else if(type.equals("hidden")) {
            return TYPE_HIDDEN;
        } else if(type.equals("select")) {
            return TYPE_SELECT;
        }
        return -1;
    }
    
    public TableLayout buildTableLayout() {
        TableLayout tableLayout = new TableLayout(this);
       // tableLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams .WRAP_CONTENT));
        tableLayout.setColumnStretchable(0, true);
        tableLayout.setColumnShrinkable(1, true);
        
        TableRow adminEmailRow = new TableRow(this);
        TextView adminEmailLabel = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        adminEmailLabel.setLayoutParams(params);
        adminEmailLabel.setText("Admin Email: ");
        EditText adminEmail = new EditText(this);
        adminEmail.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        adminEmail.setHint("you@domain.com");
        adminEmail.setTag("email");
        adminEmail.setSingleLine();
        adminEmailRow.addView(adminEmailLabel);
        adminEmailRow.addView(adminEmail);
        
        fieldViews.add(new Fields(TYPE_TEXT, (View)adminEmail));
        
/*        TableRow adminUserRow = new TableRow(this);
        TextView adminUserLabel = new TextView(this);
        adminUserLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        adminUserLabel.setText("Admin User: ");
        EditText adminUser = new EditText(this);
        adminUser.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        adminUser.setHint("admin");
        adminUserRow.addView(adminUserLabel);
        adminUserRow.addView(adminUser);
*/
  
        tableLayout.addView(adminEmailRow);

//        tableLayout.addView(adminUserRow);
        
        try {
            JSONArray fields = appInfo.getJSONArray("fields");
            for(int i = 0; i < fields.length(); i++) {
                TextView labelView;
                View view = new View(this);
                int type = convertType(fields.getJSONObject(i).getString("type"));
                String label = fields.getJSONObject(i).getString("label");
                String tag = fields.getJSONObject(i).getString("name");
                
                TableRow tr = new TableRow(this);
                labelView = new TextView(this);
                labelView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                labelView.setText(label + ":");
                switch(type) {
                    case TYPE_TEXT:
                        view = new EditText(this);
                        view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                        view.setTag(tag);
                        ((EditText)view).setSingleLine();
                        break;
                    case TYPE_CHECKBOX:
                        view = new CheckBox(this);
                        view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                        view.setTag(tag);
                        break;
                    case TYPE_SELECT:
                        view = new Spinner(this);
                        JSONArray selectArray = fields.getJSONObject(i).getJSONArray("options");
                        ArrayList<ValuePair> valueList = new ArrayList<ValuePair>();
                        for(int x = 0; x < selectArray.length(); x++) {
                            String key = selectArray.getJSONObject(x).names().getString(0);
                            String option = selectArray.getJSONObject(x).getString(key);
                            ValuePair valuePair = new ValuePair(key, option);
                            valueList.add(valuePair);
                        }
                        ArrayAdapter<ValuePair> adapter = new ArrayAdapter<ValuePair>(this, android.R.layout.simple_spinner_item, valueList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ((Spinner)view).setAdapter(adapter);
                        view.setTag(tag);
                }
                tr.addView(labelView);
                tr.addView(view);
                fieldViews.add(new Fields(type, view));
                tableLayout.addView(tr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tableLayout;
    }
    
    public void installNow() {
        new AsyncTask<Void, Void, Void>() {
            public String response;
            ProgressDialog loading = new ProgressDialog(InstallActivity.this);
            
            @Override
            protected void onPostExecute( Void result )  {
                Message msg = handler.obtainMessage();
                msg.what = HANDLE_INSTALL;
                Bundle bundle = new Bundle();
                bundle.putString("response", response);
                msg.setData(bundle);
                handler.sendMessage(msg);
                loading.dismiss();
                return;
            }
            
            @Override
            protected void onPreExecute() {
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setMessage("Installing...");
                loading.show();
            }
            @Override
            protected Void doInBackground(Void... params) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("app_name", pkgName));
                for(Fields field: fieldViews) {
                    switch(field.getType()) {
                        case TYPE_TEXT:
                        {
                            EditText editView = (EditText)field.getView();
                            String id = (String)editView.getTag();
                            String value = editView.getText().toString();
                            nameValuePairs.add(new BasicNameValuePair(id, value));
                            break;
                        }
                        case TYPE_CHECKBOX:
                        {
                            CheckBox checkView = (CheckBox)field.getView();
                            String id = (String)checkView.getTag();
                            boolean value = checkView.isChecked();
                            if(value) {
                                nameValuePairs.add(new BasicNameValuePair(id, "checked"));
                            }
                            break;
                        }
                        case TYPE_SELECT:
                        {
                            Spinner spinView = (Spinner)field.getView();
                            String id = (String)spinView.getTag();
                            String value = ((ValuePair)spinView.getSelectedItem()).getValue();
                            nameValuePairs.add(new BasicNameValuePair(id, value));
                            break;
                        }
                    }
                }
                response = MainActivity.qApi.installApplication(nameValuePairs);
                return null;
            }
        }.execute();
    }
    
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HANDLE_INSTALL:
                {
                    String result = msg.getData().getString("response");
                    try {
                        JSONObject installMsg = new JSONObject(result)
                        .getJSONObject("cpanelresult")
                        .getJSONArray("data")
                        .getJSONObject(0);
                        if(installMsg.has("error")) {
                            new AlertDialog.Builder(InstallActivity.this)
                            .setTitle("Error")
                            .setMessage(Html.fromHtml(installMsg.getString("error")))
                            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                        } else if (installMsg.has("congrats")) {
                            new AlertDialog.Builder(InstallActivity.this)
                            .setTitle("Success!")
                            .setMessage(Html.fromHtml(installMsg.getString("congrats") + "\nThis information will also be emailed to the\nadmin email address you entered."))
                            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent manageIntent = new Intent(InstallActivity.this, ManageApplications.class);
                                    InstallActivity.this.startActivity(manageIntent);
                                }
                            }).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case HANDLE_SETUP:
                    setupLayout();
                    break;
            }
           
        }
    };
    
    
    public class Fields {
        private int _type;
        private View _view;
        public Fields(int type, View view) {
            _type = type;
            _view = view;
        }
        public View getView() {
            return _view;
        }
        public int getType() {
            return _type;
        }
    }
    
    public class ValuePair {
        private String _value;
        private String _option;
        public ValuePair(String value, String option) {
            _value = value;
            _option = option;
        }
        public String getValue() {
            return _value;
        }
        public String toString() {
            return( _option );
        }
    }
    
    public class Domains {
        private String _id;
        private String _name;
        public Domains(String id, String name){
            this._id = id;
            this._name = name;
        }
        public Domains(){
            this._id = "";
            this._name = "";
        }
        public void setId(String id){
            this._id = id;
        }
        public String getId(){
            return this._id;
        }
        public void setName(String name){
            this._name = name;
        }
        public String getName(){
            return this._name;
        }
        public String toString()
        {
            return( _name );
        }
    }
}