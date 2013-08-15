package com.hostgator.quickinstall;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MenuActivity extends SherlockActivity {
    
    Button manageInstalls;
    Button installNew;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        manageInstalls = (Button)findViewById(R.id.manage_applications);
        installNew = (Button)findViewById(R.id.install_applications);
        
        manageInstalls.setOnClickListener(manageClick);
        installNew.setOnClickListener(installClick);
    }
    
    View.OnClickListener manageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(MenuActivity.this, ManageApplications.class);
            MenuActivity.this.startActivity(myIntent);
        }
    };
    
    View.OnClickListener installClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(MenuActivity.this, InstallApplication.class);
            MenuActivity.this.startActivity(myIntent);
        }
    };
    
}