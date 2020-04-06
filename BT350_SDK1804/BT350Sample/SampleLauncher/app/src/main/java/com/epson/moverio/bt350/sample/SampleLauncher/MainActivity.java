/*
 * Copyright(C) Seiko Epson Corporation 2016. All rights reserved.
 *
 * Warranty Disclaimers.
 * You acknowledge and agree that the use of the software is at your own risk.
 * The software is provided "as is" and without any warranty of any kind.
 * Epson and its licensors do not and cannot warrant the performance or results
 * you may obtain by using the software.
 * Epson and its licensors make no warranties, express or implied, as to non-infringement,
 * merchantability or fitness for any particular purpose.
 */
package com.epson.moverio.bt350.sample.SampleLauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String mApkName = "Browser";
    private String mPackageName = "com.android.browser";
    private final String CONFIG_FILE_PATH = "/sdcard/launcher.conf";
    /*  specification of "bthome.cnf"
          line1: Application name
          line2: package name of application

          example
           line1: Browser
           line2: com.android.browser
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startApp();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        // get app name and package name from file
        FileRead fileread = new FileRead(CONFIG_FILE_PATH);
        if(fileread == null){
            Toast.makeText(this, "config file not found.", Toast.LENGTH_LONG).show();
        }

        String temp;
        temp = fileread.getApkName();
        if(temp != null){
            mApkName =temp;
        }

        temp = fileread.getPackageName();
        if(temp != null){
            mPackageName = temp;
        }

        Button button = new Button(this);
        button = (Button) findViewById(R.id.button);
        button.setAllCaps(false);
        button.setText(mApkName);
    }

    public void startApp() {

        PackageManager pm = this.getPackageManager();
        Intent intent = null;

        try {
            intent = new Intent(Intent.ACTION_MAIN);
            intent = pm.getLaunchIntentForPackage(mPackageName);
        } catch(Exception e){
            Toast.makeText(this, "Calling application error.", Toast.LENGTH_LONG).show();
        }

        // check whether the app is installed
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        if (apps.size() > 0) {
            try {
                startActivity(intent);
            } catch(Exception e){
                Toast.makeText(this, "Starting activity error.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Application not found.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Intent SettingIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            SettingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(SettingIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
