package com.epson.moverio.bt350.sample.trackpadcontrol;

import java.lang.Thread;
import java.util.List;
import java.util.ArrayList;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import com.epson.moverio.btcontrol.UIControl;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    public final String TAG = MainActivity.class.getName();
    private static final String BT3S_API_SET_TRACKPAD_ROTATION = "setTrackpadRotation";
    private static final String BT3S_API_GET_TRACKPAD_ROTATION = "getTrackpadRotation";
    private static final String BT3S_API_SETTRACKPAD_ENABLE = "setTrackpadEnable";
    private static final String BT3S_API_IS_TRACKPAD_ENABLE = "isTrackpadEnable";
    private static final String BT3S_API_SET_TRACKPAD_COORDINATE = "setTrackpadCoordinate";
    private static final String BT3S_API_GET_TRACKPAD_COORDINATE = "getTrackpadCoordinate";
    private static final String BT3S_API_SET_ESPAD_SENSITIVITY = "setESpadSensitivity";
    private static final String BT3S_API_GET_ESPAD_SENSITIVITY = "getESpadSensitivity";

    private final static int TIMEOUT_SEC_MIN = 1;
    private final static int TIMEOUT_SEC_MAX = 60;
    private final static int TIMEOUT_SEC_DEFULT = 10;

    // UIControl Api
    UIControl _uiControl;
    // Switch View
    Switch _swEnable;
    SwichCheckedChangeListener _swichChangeListener;
    // Spinner View
    Spinner _spRotationAngle, _spCoordinateMode, _spSensitivity;
    KeyValuePairAdapter _rotationAngleAdapter, _coordinateModeAdapter, _sensitivityAdapter;
    SpinnerSelectListener _spinnerSelectListener;
    // Edit Text View
    TextView _tvCurrentRotationAngle, _tvCurrentCoordinateMode, _tvCurrentSensitivity;
    TextView _tvEventLog;;
    // NumberPicker
    NumberPicker _npTimeout;
    // CountDownTimer
    TrackPadEnableCountTimer _Counttimer;
    // ScrollView
    ScrollView _svEventLog;
    // timeout value
    int _CurrentTimeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create instance
        _uiControl = new UIControl(this);

        // init each View
        initSwitchs();
        initSpnners();
        initTextView();

        // init NumberPicker
        _npTimeout = (NumberPicker) findViewById(R.id.npTimeout);
        _npTimeout.setMaxValue(TIMEOUT_SEC_MAX);
        _npTimeout.setMinValue(TIMEOUT_SEC_MIN);
        _npTimeout.setValue(TIMEOUT_SEC_DEFULT);
        _CurrentTimeOut = TIMEOUT_SEC_DEFULT;

        // init CountDownTimer
        _Counttimer = new TrackPadEnableCountTimer(10000, 1000);

        // show current value
        getCurrentEnable();
        getAllCurrentTrackpadCtrl();

        // software keyboard hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // default setting of all settings
        setAllTrackpadControlDefult();
    }

    /* init SwicthView */
    private void initSwitchs() {
        // create instance
        _swEnable = (Switch) findViewById(R.id.swEnable);

        // set listener
        _swichChangeListener = new SwichCheckedChangeListener();
        _swEnable.setOnCheckedChangeListener(_swichChangeListener);
    }

    /* init SpinnerView */
    private void initSpnners() {
        // create instance
        _spRotationAngle = (Spinner) findViewById(R.id.spRotationAngle);
        _spCoordinateMode = (Spinner) findViewById(R.id.spCoordinateMode);
        _spSensitivity = (Spinner) findViewById(R.id.spSensitivity);

        // set adapter to spinner
        _rotationAngleAdapter = new KeyValuePairAdapter(this,
                android.R.layout.simple_spinner_item,
                getSpinnerList(R.array.trackpadangle_spinner_data));

        _coordinateModeAdapter = new KeyValuePairAdapter(this,
                android.R.layout.simple_spinner_item,
                getSpinnerList(R.array.trackpadmode_spinner_data));

        _sensitivityAdapter = new KeyValuePairAdapter(this,
                android.R.layout.simple_spinner_item,
                getSpinnerList(R.array.sensitivity_spinner_data));

        // set Adapter
        _spRotationAngle.setAdapter(_rotationAngleAdapter);
        _spCoordinateMode.setAdapter(_coordinateModeAdapter);
        _spSensitivity.setAdapter(_sensitivityAdapter);
        // set current value
        _spRotationAngle.setSelection(getSpinnerTrackpadCtrlPos(_rotationAngleAdapter, UIControl.TRACKPAD_ANGLE_0), false);
        _spCoordinateMode.setSelection(getSpinnerTrackpadCtrlPos(_coordinateModeAdapter, UIControl.TRACKPAD_RELATIVE_MODE), false);
        _spSensitivity.setSelection(getSpinnerTrackpadCtrlPos(_sensitivityAdapter, UIControl.ESPAD_SENSITIVITY_LOW), false);
        // set listener
        _spinnerSelectListener = new SpinnerSelectListener();
        _spRotationAngle.setOnItemSelectedListener(_spinnerSelectListener);
        _spCoordinateMode.setOnItemSelectedListener(_spinnerSelectListener);
        _spSensitivity.setOnItemSelectedListener(_spinnerSelectListener);
    }

    /* get list for spinner display */
    private ArrayList<Pair<String, String>> getSpinnerList(int arrayId) {
        // Get list for spinner display
        ArrayList<Pair<String, String>> sortItemList = new ArrayList<Pair<String, String>>();
        Resources resources = getResources();
        TypedArray sortItemArray = resources.obtainTypedArray(arrayId);
        int arrayLength = sortItemArray.length();
        for (int i = 0; i < arrayLength; i++) {
            int id = sortItemArray.getResourceId(i, -1);
            if (id != -1) {
                String[] item = resources.getStringArray(id);
                sortItemList.add(new Pair<String, String>(item[0], item[1]));
            }
        }
        sortItemArray.recycle();

        return sortItemList;
    }

    /* inite TextView */
    public void initTextView() {
        _tvCurrentRotationAngle = (TextView)findViewById(R.id.tvCurrentRotationAngle);
        _tvCurrentCoordinateMode = (TextView)findViewById(R.id.tvCurrentCoordinateMode);
        _tvCurrentSensitivity = (TextView)findViewById(R.id.tvCurrentSensitivity);
        _tvEventLog = (TextView)findViewById(R.id.tvEventLog);
        _svEventLog = (ScrollView)findViewById(R.id.svEventLog);

        _tvCurrentRotationAngle.setText(_rotationAngleAdapter.getItem(0).second);
        _tvCurrentCoordinateMode.setText(_coordinateModeAdapter.getItem(0).second);
        _tvCurrentSensitivity.setText(_sensitivityAdapter.getItem(0).second);
        _tvEventLog.setText("");
        _tvEventLog.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /* get the posiion of the specified trackpad control */
    public int getSpinnerTrackpadCtrlPos(KeyValuePairAdapter spinnerAdapter, int value ) {
        Pair<String, String> selectedItem = null;
        int count = spinnerAdapter.getCount();
        int indx, pos = count - 1;

        for (indx = 0; indx < count; indx++) {
            selectedItem = spinnerAdapter.getItem(indx);
            if (Integer.parseInt(selectedItem.first) == value) {
                pos = indx;
                break;
            }
        }
        return pos;
    }

    @Override
    public void onClick(View v) {
    }

    // SwichView CheckedChangeListener class
    public class SwichCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton bottonView, boolean IsChecked) {
            Log.d(TAG, "onCheckedChanged");

            if (bottonView == _swEnable) {
                setTrackpadEnable(IsChecked);
            }
        }
    }

    // SpinnerView ItemSelectedListener class
    public class SpinnerSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> patent, View view, int posision, long id) {
            Log.d(TAG, "onItemSelected");

            Spinner spinner = (Spinner) patent;
            TextView textView = null;
            KeyValuePairAdapter adapter = null;
            Pair<String, String> selectedItem = null;
            String apiName = "";
            boolean ret = false;
            if (spinner == _spRotationAngle) {
                textView = _tvCurrentRotationAngle;
                // get selected item
                selectedItem = _rotationAngleAdapter.getItem(spinner.getSelectedItemPosition());
                // TrackpadRotation
                ret = _uiControl.setTrackpadRotation(Integer.parseInt(selectedItem.first));
                apiName = BT3S_API_SET_TRACKPAD_ROTATION;
            } else if (spinner == _spCoordinateMode) {
                textView = _tvCurrentCoordinateMode;
                // get selected item
                selectedItem = _coordinateModeAdapter.getItem(spinner.getSelectedItemPosition());
                // TrackpadCoordinate
                ret = _uiControl.setTrackpadCoordinate(Integer.parseInt(selectedItem.first));
                apiName = BT3S_API_SET_TRACKPAD_COORDINATE;
            } else if (spinner == _spSensitivity) {
                textView = _tvCurrentSensitivity;
                // get selected item
                selectedItem = _sensitivityAdapter.getItem(spinner.getSelectedItemPosition());
                // ESpadSensitivity
                ret = _uiControl.setESpadSensitivity(Integer.parseInt(selectedItem.first));
                apiName = BT3S_API_SET_ESPAD_SENSITIVITY;
            }
            if (textView != null) {
                setEvetLog(apiName, selectedItem.first, String.valueOf(ret), true);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Log.e(TAG, ex.getMessage());
                }
                getCurrentTrackpadControl(textView, ret, true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> var1) {
        }
    }

    /* default setting of all settings */
    public void setAllTrackpadControlDefult() {
        try {
            // TRACKPAD_ANGLE_0
            _uiControl.setTrackpadRotation(UIControl.TRACKPAD_ANGLE_0);
            // TRACKPAD_RELATIVE_MODE
            _uiControl.setTrackpadCoordinate(UIControl.TRACKPAD_RELATIVE_MODE);
            // ESPAD_SENSITIVITY_LOW
            _uiControl.setESpadSensitivity(UIControl.ESPAD_SENSITIVITY_LOW);
            // Enable
            _uiControl.setTrackpadEnable(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /* current enable  */
    public void getCurrentEnable(){

        // disable OnCheckedChangeListener
        _swEnable.setOnCheckedChangeListener(null);

        // get current trackpad enable and display update
        getCurrentTrackpadEnable(_swEnable);

        // enable OnCheckedChangeListener
        _swEnable.setOnCheckedChangeListener(_swichChangeListener);
    }

    /* Get all of the trackpad control value and View update  */
    public void getAllCurrentTrackpadCtrl(){
        // disable OnItemSelectedListener
        _spRotationAngle.setOnItemSelectedListener(null);
        _spCoordinateMode.setOnItemSelectedListener(null);
        _spSensitivity.setOnItemSelectedListener(null);

        // get current trackpad enable and display update
        int currentRotationAngle = getCurrentTrackpadControl(_tvCurrentRotationAngle, true, false);
        int currentCoordinateMode = getCurrentTrackpadControl(_tvCurrentCoordinateMode, true, false);
        int currentSensitivity = getCurrentTrackpadControl(_tvCurrentSensitivity, true, false);

        // Select current trackpad control value with Spinner
        _spRotationAngle.setSelection(getSpinnerTrackpadCtrlPos(_rotationAngleAdapter, currentRotationAngle), false);
        _spCoordinateMode.setSelection(getSpinnerTrackpadCtrlPos(_coordinateModeAdapter, currentCoordinateMode), false);
        _spSensitivity.setSelection(getSpinnerTrackpadCtrlPos(_sensitivityAdapter ,currentSensitivity), false);

        // enable OnItemSelectedListener
        _spRotationAngle.setOnItemSelectedListener(_spinnerSelectListener);
        _spCoordinateMode.setOnItemSelectedListener(_spinnerSelectListener);
        _spSensitivity.setOnItemSelectedListener(_spinnerSelectListener);
    }

    /* get current trackpad enable and view update */
    public Boolean getCurrentTrackpadEnable(Switch switchView) {
        // get trackpad enable
        Boolean enable = _uiControl.isTrackpadEnable();
        //setEvetLog("isTrackpadEnable", String.valueOf(enable), false);

        // update SwitchView
        switchView.setChecked(enable);

        return enable;
    }

    public void setTrackpadEnable(Boolean IsEnable) {
        // set trackpad enable
        Boolean ret = _uiControl.setTrackpadEnable(IsEnable);

        setEvetLog(BT3S_API_SETTRACKPAD_ENABLE, String.valueOf(IsEnable), String.valueOf(ret), true);

        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }

        Boolean onoff = _uiControl.isTrackpadEnable();

        setEvetLog(BT3S_API_IS_TRACKPAD_ENABLE, String.valueOf(onoff), false);

        // update SwichView
        _swEnable.setOnCheckedChangeListener(null);
        _swEnable.setChecked(onoff);
        _swEnable.setOnCheckedChangeListener(_swichChangeListener);
        if (onoff == false) {
            _CurrentTimeOut = _npTimeout.getValue();
            _Counttimer = new TrackPadEnableCountTimer(_CurrentTimeOut * 1000, 500);
            _Counttimer.start();
        }
    }

    // get current trackpad control value and update view
    public int getCurrentTrackpadControl(TextView textView, Boolean sendRet, Boolean viewLog) {
        int value = 0;
        int defultValue = 0;
        int textColor;
        String apiName = "";
        KeyValuePairAdapter adapter = null;

        // get trackpad control value
        if (textView == _tvCurrentRotationAngle) {
            // Trackpad Rotation
            try {
                value = _uiControl.getTrackpadRotation();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
            adapter = _rotationAngleAdapter;
            defultValue = _uiControl.TRACKPAD_ANGLE_0;
            apiName = BT3S_API_GET_TRACKPAD_ROTATION;
        } else if (textView == _tvCurrentCoordinateMode) {
            // Trackpad Coordinate
            try {
                value = _uiControl.getTrackpadCoordinate();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
            adapter = _coordinateModeAdapter;
            defultValue = _uiControl.TRACKPAD_RELATIVE_MODE;
            apiName = BT3S_API_GET_TRACKPAD_COORDINATE;
        } else if (textView == _tvCurrentSensitivity) {
            // Sensitivity
            try {
                value = _uiControl.getESpadSensitivity();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
            adapter = _sensitivityAdapter;
            defultValue = _uiControl.ESPAD_SENSITIVITY_LOW;
            apiName = BT3S_API_GET_ESPAD_SENSITIVITY;
        }
        if (adapter != null) {
            if (viewLog) {
                setEvetLog(apiName, String.valueOf(value), false);
            }

            // update the contents of the current value display TextView
            textView.setText(apiName + "( " + value +" )");
            if (sendRet) {
                if (value == defultValue) {
                    textColor = Color.GREEN;
                } else {
                    textColor = Color.BLUE;
                }
            } else {
                textColor = Color.RED;
            }
            textView.setTextColor(textColor);
        }
        return value;
    }

    // CountTimer class
    public class TrackPadEnableCountTimer extends CountDownTimer {
        public long countMillis = -1;

        public TrackPadEnableCountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // display the remaining seconds in the view
            double sec = (double)millisUntilFinished / 1000;
            _npTimeout.setValue((int)Math.round(sec));
        }

        @Override
        public void onFinish() {
            // Trackpad Enable
            setTrackpadEnable(true);
            _npTimeout.setValue((int)_CurrentTimeOut);
        }
    }

    public void setEvetLog(String method, String returnVale, Boolean setApi) {
        if (setApi) {
            _tvEventLog.append(String.format("%s() return: %s", method, returnVale) + " -> ");
            //setFullScroll();
        } else {
            _tvEventLog.append(String.format("%s() return: %s", method, returnVale) + "\n");
            setFullScroll();
        }
    }

    public void setEvetLog(String method, String parameter, String returnVale, Boolean setApi) {
        if (setApi) {
            _tvEventLog.append(String.format("%s( %s ) return: %s", method, parameter, returnVale) + " -> ");
            //setFullScroll();
        } else {
            _tvEventLog.append(String.format("%s( %s ) return: %s", method, parameter, returnVale) + "\n");
            setFullScroll();
        }
    }

    // Full Scroll setting
    public void setFullScroll() {
        _svEventLog.post(new Runnable() {
            public void run() {
                _svEventLog.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public class KeyValuePair extends Pair<String, String> {

        public KeyValuePair(String key, String value) {
            super(key, value);
        }

        public String getKey() {
            return super.first;
        }

        public String getValue() {
            return super.second;
        }
    }

    public class KeyValuePairArrayAdapter extends ArrayAdapter<KeyValuePair> {

        public KeyValuePairArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public KeyValuePairArrayAdapter(Context context, int textViewResourceId, List<KeyValuePair> list) {
            super(context, textViewResourceId, list);
        }
    }
}


