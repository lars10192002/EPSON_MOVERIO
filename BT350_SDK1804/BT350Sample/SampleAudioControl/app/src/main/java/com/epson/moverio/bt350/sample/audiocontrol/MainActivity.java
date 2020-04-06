package com.epson.moverio.bt350.sample.audiocontrol;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;

import com.epson.moverio.btcontrol.AudioControl;

import static com.epson.moverio.btcontrol.AudioControl.PRESET_0;
import static com.epson.moverio.btcontrol.AudioControl.PRESET_1;
import static com.epson.moverio.btcontrol.AudioControl.PRESET_2;
import static com.epson.moverio.btcontrol.AudioControl.PRESET_3;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    AudioControl _audioControl;
    Spinner _spALCPreset;
    TextView _tvALCstate,_tvLog,_tvCategory;
    ScrollView _svLog;
    int  _setValue,_getValue;
    String _setAPIName,_getAPIName;
    boolean _setStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create instance
        _audioControl = new AudioControl(this);

        _spALCPreset = (Spinner)findViewById(R.id.spALCPreset);
        _tvALCstate = (TextView)findViewById(R.id.tvALCstate);
        _tvLog = (TextView)findViewById(R.id.tvEventLog);
        _tvCategory = (TextView)findViewById(R.id.tvCategory);
        _svLog = (ScrollView)findViewById(R.id.svLog);

        //Get and display of setting values
        _getValue = _audioControl.getALCPreset();
        _setStatus = _audioControl.setALCPreset(_getValue);
        _getAPIName = "getALCPreset";
        StatusOutput(_tvALCstate,_getAPIName,_setStatus,_getValue);
        _spALCPreset.setOnItemSelectedListener(this);

        //Add spinner item
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        adapter.add("PRESET_0(0)");
        adapter.add("PRESET_1(1)");
        adapter.add("PRESET_2(2)");
        adapter.add("PRESET_3(3)");
        adapter.add("PRESET_INVALID(-1)");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spALCPreset.setAdapter(adapter);
        _spALCPreset.setFocusable(false);//Remove focus from spinner at application startup

        //Display of current value
        _spALCPreset.setSelection(_getValue);

    }
    @Override
    public void onItemSelected(AdapterView parent, View view, int position, long id){
        Spinner _spALCPreset = (Spinner) parent;
        String item =(String) _spALCPreset.getSelectedItem();
        //Operation at application startup
        if (_spALCPreset.isFocusable() == false) {
            _spALCPreset.setFocusable(true);
            return;
        }
        //Change audio control
        switch (item) {
            case "PRESET_0(0)":
                _setValue = PRESET_0;
                break;
            case "PRESET_1(1)":
                _setValue = PRESET_1;
                break;
            case "PRESET_2(2)":
                _setValue = PRESET_2;
                break;
            case "PRESET_3(3)":
                _setValue = PRESET_3;
                break;
            case "PRESET_INVALID(-1)":
                _setValue = -1;
                break;
        }
        _setStatus=_audioControl.setALCPreset(_setValue);
        _setAPIName ="setALCPreset";

        try {
            Thread.sleep(100); //wait for 100 msec
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            _getValue = _audioControl.getALCPreset();
            _getAPIName = "getALCPreset";
        }catch (Exception e){
            Log.e("BT350-API","Exception",e);
        }

        //Status font color change by acquired value
        if(_setStatus == true){
            if(_getValue == PRESET_0) {
                _tvALCstate.setTextColor(Color.GREEN);
            }else{
                _tvALCstate.setTextColor(Color.BLUE);
            }
        }else{
            _tvALCstate.setTextColor(Color.RED);
        }

        StatusOutput(_tvALCstate,_setAPIName,_setStatus,_getValue);
        LogOutput(_tvLog,_setValue,_setStatus,_getValue);

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //Initialize at the onDestroy
    @Override
    public void onDestroy(){
        super.onDestroy();
        _setStatus=_audioControl.setALCPreset(PRESET_0);
    }
    public void StatusOutput(TextView textview ,String apiName,boolean res,int getValue){

        textview.setText(apiName+" :"+ Integer.toString(getValue));
        //Status font color change by acquired value
        if(res == true){
            if(getValue == 0){
                textview.setTextColor(Color.GREEN);
            }else{
                textview.setTextColor(Color.BLUE);
            }
        }else{
            textview.setTextColor(Color.RED);
        }
    }
    //Log outputs to the TextView
    public void LogOutput(TextView textview,int set,boolean result,int get){
        textview.append(_setAPIName+"("+set+")"+"return:"+result+ " -> "+_getAPIName+" return:"+get+ "\n" );
        _svLog.fullScroll(ScrollView.FOCUS_DOWN);
    }

}
