package com.epson.moverio.bt350.sample.displaycontrol;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.epson.moverio.btcontrol.DisplayControl;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    DisplayControl _displayControl;
    Button _btnMode, _btnMute;
    CheckBox _cbShowToast;
    SeekBar _sbDistance, _sbBrightness;
    RadioButton _rbInvalid1, _rbInvalid2;
    TextView _tvLog,_tvGetDistance;
    RadioGroup _rgDistance;
    ScrollView _svLog;
    int _setDistance,_getDistace ,_mode, _backlight,_setStatus;
    String _setAPIName,_getAPIName;
    boolean _showToast = true;

    public static final int INVALID_1 = 16;
    public static final int INVALID_2 = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create instance
        _displayControl = new DisplayControl(this);

        _tvGetDistance = (TextView)findViewById(R.id.tvGetDistance);
        _rgDistance = (RadioGroup)findViewById(R.id.rgDistance);
        _btnMode = (Button)findViewById(R.id.btnDispMode);
        _btnMute = (Button)findViewById(R.id.btnMute);
        _cbShowToast = (CheckBox)findViewById(R.id.cbOSD);
        _sbDistance = (SeekBar)findViewById(R.id.sbDistance);
        _sbBrightness = (SeekBar)findViewById(R.id.sbBrightness);
        _rbInvalid1 = (RadioButton)findViewById(R.id.rbInvalid1);
        _rbInvalid2 = (RadioButton)findViewById(R.id.rbInvalid2);
        _tvLog = (TextView)findViewById(R.id.tvLog);
        _svLog = (ScrollView)findViewById(R.id.svLog);


        //set listener
        _btnMode.setOnClickListener(this);
        _btnMute.setOnClickListener(this);
        _cbShowToast.setOnClickListener(this);
        _rbInvalid1.setOnClickListener(this);
        _rbInvalid2.setOnClickListener(this);
        _sbDistance.setOnSeekBarChangeListener(this);
        _sbBrightness.setOnSeekBarChangeListener(this);


        //Setting the seek bar value
        _sbDistance.setMax(15);
        _sbBrightness.setMax(20);
        //Remove focus from seek bar at application startup
        _sbDistance.setFocusable(false);
        _sbBrightness.setFocusable(false);


        //Get and display of setting values
        _getDistace = _displayControl.getDisplayDistance();
        outputDisplayDistance(_tvGetDistance,_getDistace);
        _mode = _displayControl.getMode();
        _backlight = _displayControl.getBacklight();

        //display of current value
        _cbShowToast.setChecked(_showToast);
        _sbDistance.setProgress(_getDistace);
        _sbBrightness.setProgress(_backlight);


        if (_mode == DisplayControl.DISPLAY_MODE_2D){
            _btnMode.setText("2D");
        }else if (_mode == DisplayControl.DISPLAY_MODE_3D){
            _btnMode.setText("3D");
        }

    }

    @Override
    public void onClick(View v){

        if(v == _btnMode) {
            _mode = _displayControl.getMode();

            if (_mode == DisplayControl.DISPLAY_MODE_2D) {
                // change to 3D mode
                _btnMode.setText("3D");
                _mode = DisplayControl.DISPLAY_MODE_3D;

            } else if (_mode == DisplayControl.DISPLAY_MODE_3D) {
                //change to 2D mode
                _btnMode.setText("2D");
                _mode = DisplayControl.DISPLAY_MODE_2D;
            }
            _displayControl.setMode(_mode, _showToast);

        }else if (v == _btnMute) {
                //Display Mute
            boolean isMute = _displayControl.getMute();
                if (isMute == false) {
                    _displayControl.setMute(true);
                    try {
                        Thread.sleep(3 * 1000); //wait for 3 sec
                    } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                _displayControl.setMute(false);
            }
        }else if (v == _cbShowToast){
            if (true == _cbShowToast.isChecked()){
                _showToast = true;
            }else {
                _showToast = false;
            }

        }else if (v == _rbInvalid1){
            //Set invalid value(16)
            _setDistance = INVALID_1;
            _setStatus = _displayControl.setDisplayDistance(_setDistance);
            _setAPIName = "setDisplayDistance";
            _getAPIName = "getDisplayDistance";

        }else if(v == _rbInvalid2){
            //Set invalid value(-1)
            _setDistance = INVALID_2;
            _setStatus =  _displayControl.setDisplayDistance(_setDistance);
            _setAPIName = "setDisplayDistance";
            _getAPIName = "getDisplayDistance";

        }

        if(v == _rbInvalid1 || v == _rbInvalid2) {
            try {
                Thread.sleep(100); //wait for 100 msec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _getDistace = _displayControl.getDisplayDistance();
            outputDisplayDistance(_tvGetDistance, _getDistace);
            LogOutput(_tvLog, _setDistance, _setStatus, _getDistace);
        }
    }
    //Change seek bar value
    @Override
    public void onProgressChanged(SeekBar sBar, int Progress, boolean fromUser){
        if(_sbBrightness.isFocusable() == false && _sbDistance.isFocusable() == false){
            _sbDistance.setFocusable(true);
            _sbBrightness.setFocusable(true);
            return;
        }
        if(sBar == _sbBrightness) {
            _backlight = Progress;
            _displayControl.setBacklight(_backlight);
            _setAPIName = "setBacklight";
        }else if(sBar == _sbDistance){

            if(_rgDistance.getCheckedRadioButtonId() != -1){
                _rgDistance.clearCheck();
            }

            _setDistance = Progress;
           _setStatus = _displayControl.setDisplayDistance(_setDistance);

            _getDistace = _displayControl.getDisplayDistance();
            outputDisplayDistance(_tvGetDistance,_getDistace);
            _setAPIName = "setDisplayDistance";
            _getAPIName = "getDisplayDistance";
            LogOutput(_tvLog,_setDistance,_setStatus,_getDistace);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar sBar){
    }

    @Override
    public void onStopTrackingTouch(SeekBar sBar){
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        _displayControl.setDisplayDistance(0);

    }
    //Display of display distance to the TextView
    public void outputDisplayDistance(TextView textview,int DistanceValue){
        if(_setDistance == 0){
            textview.setTextColor(Color.GREEN);
        }else if( _setDistance < 16 && _setDistance > -1 ){
            textview.setTextColor(Color.BLUE);
        }else{
            textview.setTextColor(Color.RED);
        }
        textview.setText("getDisplayDistance :"+ Integer.toString(_setDistance)+"\n");
        textview.setText("DisplayDistance[m] :" + DisplayDistance(DistanceValue));
    }
    //Log outputs to the TextView
    public void LogOutput(TextView textview,int set,int result,int get){
        textview.append(_setAPIName+"("+set+")"+"return:"+result+ " -> "+_getAPIName+" return:"+get+ "\n" );
        _svLog.fullScroll(ScrollView.FOCUS_DOWN);
    }
    //Definition of display distance
    public double DisplayDistance(int DistanceValue){
        double Distance[] = {11.3,10,9,8,7.5,7,6.5,6,5.5,5,4.5,4,3.5,3,2.5,2};
        return Distance[DistanceValue];
    }

}
