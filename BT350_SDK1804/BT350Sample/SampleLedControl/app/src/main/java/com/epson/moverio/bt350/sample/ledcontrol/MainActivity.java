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

package com.epson.moverio.bt350.sample.ledcontrol;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.epson.moverio.btcontrol.Bt3sCameraLedMode;
import com.epson.moverio.btcontrol.Bt3sControllerLedMode;

import static com.epson.moverio.btcontrol.Bt3sCameraLedMode.CAMERA_LED_NORMAL;
import static com.epson.moverio.btcontrol.Bt3sCameraLedMode.CAMERA_LED_OFF;
import static com.epson.moverio.btcontrol.Bt3sControllerLedMode.LED_MODE_LOW;
import static com.epson.moverio.btcontrol.Bt3sControllerLedMode.LED_MODE_NORMAL;
import static com.epson.moverio.btcontrol.Bt3sControllerLedMode.LED_MODE_OFF;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Bt3sControllerLedMode _CtrlLedMode;
    Bt3sCameraLedMode _CamLedMode;
    TextView _tvModeCam,_tvModeCtrl,_tvCatCam,_tvCatCtrl,_tvLog;
    Spinner _spModeCam,_spModeCtrl;
    ScrollView _svLog;
    int _setCtrlValue, _setCamValue,_getCtrlValue,_getCamValue;
    String _setAPIName,_getAPIName;
    boolean _setStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create instance
        _CtrlLedMode = new Bt3sControllerLedMode(this);
        _CamLedMode = new Bt3sCameraLedMode(this);

        _tvModeCtrl = (TextView)findViewById(R.id.tvModeCtrl);
        _tvModeCam = (TextView)findViewById(R.id.tvModeCam);
        _tvCatCam = (TextView)findViewById((R.id.tvCatCam));
        _tvCatCtrl = (TextView)findViewById(R.id.tvCatCtrl);
        _tvLog = (TextView)findViewById(R.id.tvLog);
        _svLog = (ScrollView)findViewById(R.id.svLog);
        //scroll settings
        _tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());

        _spModeCtrl = (Spinner)findViewById(R.id.spModeCtrl);
        _spModeCam = (Spinner)findViewById(R.id.spModeCam);
        //get and display of setting values
        _getCtrlValue = _CtrlLedMode.getControllerLedMode();
        _setStatus = _CtrlLedMode.setControllerLedMode(_getCtrlValue);
        _setAPIName = "setControllerLedMode";
        StatusOutput(_tvModeCtrl,_setAPIName,_setStatus,_getCtrlValue);
        _getCamValue = _CamLedMode.getCameraLedMode();
        _setStatus = _CamLedMode.setCameraLedMode(_getCamValue);
        _setAPIName = "setCameraLedMode";
        StatusOutput(_tvModeCam,_setAPIName,_setStatus,_getCamValue);
        //set listener
        _spModeCtrl.setOnItemSelectedListener(this);
        _spModeCam.setOnItemSelectedListener(this);

        //add spinner items
        ArrayAdapter adapter_ctrl = new ArrayAdapter(this,android.R.layout.simple_spinner_item);

        adapter_ctrl.add("LED_MODE_OFF(0)");
        adapter_ctrl.add("LED_MODE_NORMAL(1)");
        adapter_ctrl.add("LED_MODE_LOW(2)");
        adapter_ctrl.add("LED_MODE_INVALID(-1)");
        adapter_ctrl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spModeCtrl.setAdapter(adapter_ctrl);
        _spModeCtrl.setFocusable(false); //Remove focus from spinner at application startup
        ArrayAdapter adapter_cam = new ArrayAdapter(this,android.R.layout.simple_spinner_item);
        adapter_cam.add("CAMERA_LED_OFF(0)");
        adapter_cam.add("CAMERA_LED_NORMAL(1)");
        adapter_cam.add("CAMERA_LED_INVALID(-1)");
        adapter_cam.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spModeCam.setAdapter(adapter_cam);
        _spModeCam.setFocusable(false); //Remove focus from spinner at application startup

        //display of current value
        _spModeCtrl.setSelection(_getCtrlValue);
        _spModeCam.setSelection(_getCamValue);
    }
    @Override
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        String item =(String) spinner.getSelectedItem();
        if (_spModeCtrl.isFocusable() == false) {
            _spModeCtrl.setFocusable(true);
            return;
        }
        //change of controller LED
        if(spinner == _spModeCtrl) {
            switch (item) {
                case "LED_MODE_OFF(0)":
                    //set to LED_MODE_OFF
                    _setCtrlValue = LED_MODE_OFF;
                    break;
                case "LED_MODE_NORMAL(1)":
                    //set to LED_MODE_NORMAL
                    _setCtrlValue = LED_MODE_NORMAL;
                    break;
                case "LED_MODE_LOW(2)":
                    //set to LED_MODE_LOW
                    _setCtrlValue = LED_MODE_LOW;
                    break;
                case "LED_MODE_INVALID(-1)":
                    //set to -1
                    _setCtrlValue = -1;
                    break;
            }
            _setStatus=_CtrlLedMode.setControllerLedMode(_setCtrlValue);
            _setAPIName = "setControllerLedMode";
            try {
                Thread.sleep(100); //wait for 100 msec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //display of log and status
            _getCtrlValue = _CtrlLedMode.getControllerLedMode();
            _getAPIName = "getControllerLedMode";
            _tvModeCtrl.setText("getControllerLedMode :" + Integer.toString(_getCtrlValue));
            StatusOutput(_tvModeCtrl,_getAPIName,_setStatus,_getCtrlValue);
            LogOutput(_tvLog,_setCtrlValue,_setStatus,_getCtrlValue);



        }

        if (_spModeCam.isFocusable() == false) {
            _spModeCam.setFocusable(true);
            return;
        }
        //change of camera LED
        if(spinner == _spModeCam) {
            switch (item) {
                case "CAMERA_LED_OFF(0)":
                    //set to CAMERA_LED_OFF
                    _setCamValue = CAMERA_LED_OFF;
                    break;
                case "CAMERA_LED_NORMAL(1)":
                    //set to CAMERA_LED_NORMAL
                    _setCamValue = CAMERA_LED_NORMAL;
                    break;
                case "CAMERA_LED_INVALID(-1)":
                    _setCamValue = -1;
                    //set to CAMERA_LED_NORMAL
                    break;
            }
            _setStatus = _CamLedMode.setCameraLedMode(_setCamValue);
            _setAPIName = "setCameraLedMode";
            try {
                Thread.sleep(100); //wait for 100 msec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //display of log and status
            _getCamValue = _CamLedMode.getCameraLedMode();
            _getAPIName = "getCameraLedMode";
            StatusOutput(_tvModeCam,_getAPIName,_setStatus,_getCamValue);
            LogOutput(_tvLog,_setCamValue,_setStatus,_getCamValue);

        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        _setStatus = _CtrlLedMode.setControllerLedMode(LED_MODE_NORMAL);
        _setStatus = _CamLedMode.setCameraLedMode(CAMERA_LED_NORMAL);

    }

    public void StatusOutput(TextView textview ,String apiName,boolean res,int getValue){

        textview.setText(apiName+" :"+ Integer.toString(getValue));
        //status font color change by acquired value
        if(res == true){
            if(getValue == 1){
                textview.setTextColor(Color.GREEN);
            }else{
                textview.setTextColor(Color.BLUE);
            }
        }else{
            textview.setTextColor(Color.RED);
        }
    }

    public void LogOutput(TextView textview,int set,boolean result,int get){
        textview.append(_setAPIName+"("+set+")"+"return:"+result+ " -> "+_getAPIName+" return:"+get+ "\n" );
        _svLog.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
