package com.hostgator.quickinstall;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class MainActivity extends SherlockFragmentActivity {

    ProgressDialog progDialog;
    int typeBar = 0;
    ProgressThread progThread;
    int delay = 40;
    int maxBarValue = 200;
    protected static Api qApi;
    
    EditText username_field;
    EditText password_field;
    EditText domain_field;
    Button login_button;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar ab = getSupportActionBar();
        //ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        username_field = (EditText)findViewById(R.id.username);
        password_field = (EditText)findViewById(R.id.password);
        domain_field = (EditText)findViewById(R.id.domain);
        login_button = (Button)findViewById(R.id.login);
        
        login_button.setOnClickListener(loginClicked);
    }
    
    View.OnClickListener loginClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(username_field.length() == 0 || password_field.length() == 0 || domain_field.length() == 0) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Failed")
                    .setMessage("Please fill in all of the fields")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                return;
            }
            qApi = new Api(MainActivity.this, username_field.getText().toString(), password_field.getText().toString(), domain_field.getText().toString());
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog loading = new ProgressDialog(MainActivity.this);
                @Override
                protected void onPostExecute( Void result )  {
                    loading.dismiss();
                    if(qApi.isLoggedIn()) {
                        Intent myIntent = new Intent(MainActivity.this, ManageApplications.class);
                        MainActivity.this.startActivity(myIntent);
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Failed")
                        .setMessage("You failed to login.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                    return;
                }
                
                @Override
                protected void onPreExecute() {
                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loading.setMessage("Logging in...");
                    loading.show();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    qApi.login();
                    return null;
                }
            }.execute();
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        
        switch(id) {
        case 0:                      // Spinner
            progDialog = new ProgressDialog(this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setMessage("Loading...");
            progThread = new ProgressThread(handler);
            progThread.start();
            return progDialog;
        case 1:                      // Horizontal
            progDialog = new ProgressDialog(this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progDialog.setMax(maxBarValue);
            progDialog.setMessage("Dollars in checking account:");
            progThread = new ProgressThread(handler);
            progThread.start();
            return progDialog;
        default:
            return null;
        }
    }
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            /*
            int total = msg.getData().getInt("total");
            progDialog.setProgress(total);
            if (total <= 0){
                dismissDialog(typeBar);
                progThread.setState(ProgressThread.DONE);
                if(qApi.isLoggedIn()) {
                    TextView textview = (TextView) findViewById(R.id.textView);
                    textview.setText("Logged in!");
                    textview.setText(qApi.getAppList("qi.dev-hosts.us"));
                }
            }
            */
        }
    };
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    */
    
    private class ProgressThread extends Thread {   
        final static int DONE = 0;
        final static int RUNNING = 1;
        
        Handler mHandler;
        int mState;
        int total;
        ProgressThread(Handler h) {
            mHandler = h;
        }
        @Override
        public void run() {
            mState = RUNNING;   
            total = maxBarValue;
            while (mState == RUNNING) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "Thread was Interrupted");
                }
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("total", total);
                msg.setData(b);
                mHandler.sendMessage(msg);
                
                total--;
            }
        }
        public void setState(int state) {
            mState = state;
        }
    }
}
