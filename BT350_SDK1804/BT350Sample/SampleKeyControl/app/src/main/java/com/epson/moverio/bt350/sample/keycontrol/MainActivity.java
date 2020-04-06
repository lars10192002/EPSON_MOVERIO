package com.epson.moverio.bt350.sample.keycontrol;

import java.util.List;
import java.util.ArrayList;
import java.lang.Thread;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.epson.moverio.bt3sapitestkeycontrol.KeyValuePairAdapter;
import com.epson.moverio.btcontrol.Bt3sCustomKey;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    public final String TAG = MainActivity.class.getName();
    private static final String BT3S_API_SET_KEY_ASSIGN = "setKeyAssign";
    private static final String BT3S_API_GET_KEY_ASSIGN = "getKeyAssign";
    private static final String BT3S_API_SET_KEY_ENABLE = "setKeyEnable";
    private static final String BT3S_API_IS_KEY_ENABLE = "isKeyEnable";
    private static final String BT3S_API_RESET_TO_DEFULT = "resetToDefault";

    Bt3sCustomKey _bt3sCustomKey;
    // Switch View
    Switch _swDpadUp, _swDpadDown, _swDpadLeft, _swDpadRight, _swDpadCenter;
    Switch _swVolumeUp, _swVolumeDown, _swFunction;
    Switch _swBack, _swHome, _swAppSwitch, _swPower;
    Switch _swInputParam;
    SwichCheckedChangeListener _swichChangeListener;
    // Spinner View
    Spinner _spDpadUp, _spDpadDown, _spDpadLeft, _spDpadRight, _spDpadCenter;
    Spinner _spVolumeUp, _spVolumeDown, _spFunction;
    KeyValuePairAdapter _spinnerAdapter;
    SpinnerSelectListener _spinnerSelectListener;
    // Edit Text View
    TextView _tvCurrentDpadUp, _tvCurrentDpadDown, _tvCurrentDpadLeft, _tvCurrentDpadRight, _tvCurrentDpadCenter;
    TextView _tvCurrentVolumeUp, _tvCurrentVolumeDown, _tvCurrentFunction, _tvEventLog, _tvKeyEventLog;
    // Button View
    Button _btnSetKeyAssign, _btnResetToDefult;
    // EditText
    EditText _etPhysicalkey, _etSetKeyEventCode, _etGetKeyEventCode;
    // ScrollView
    ScrollView _svEventLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create instance
        _bt3sCustomKey = new Bt3sCustomKey(this);

        // init each View
        initSwitchs();
        initSpnners();
        initTextView();
        initButton();
        initEditText();

        // update current value
        getAllCurrentKeyEnable(false);
        getAllCurrentKeyAssign();

        // software keyboard hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // reset to default
        try {
            _bt3sCustomKey.resetToDefault();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /* init SwicthView */
    private void initSwitchs() {
        // create instance
        _swDpadUp = (Switch) findViewById(R.id.swDpadUp);
        _swDpadDown = (Switch) findViewById(R.id.swDpadDown);
        _swDpadLeft = (Switch) findViewById(R.id.swDpadLeft);
        _swDpadRight = (Switch) findViewById(R.id.swDpadRight);
        _swDpadCenter = (Switch) findViewById(R.id.swDpadCenter);
        _swVolumeUp = (Switch) findViewById(R.id.swVolumeUp);
        _swVolumeDown = (Switch) findViewById(R.id.swVolumeDown);
        _swFunction = (Switch) findViewById(R.id.swFunction);
        _swBack = (Switch) findViewById(R.id.swBack);
        _swHome = (Switch) findViewById(R.id.swHome);
        _swAppSwitch = (Switch) findViewById(R.id.swAppSwitch);
        _swPower = (Switch) findViewById(R.id.swPower);
        _swInputParam = (Switch) findViewById(R.id.swInputParam);
        // set listener
        _swichChangeListener = new SwichCheckedChangeListener();
        _swDpadUp.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadLeft.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadDown.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadRight.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadCenter.setOnCheckedChangeListener(_swichChangeListener);
        _swVolumeUp.setOnCheckedChangeListener(_swichChangeListener);
        _swVolumeDown.setOnCheckedChangeListener(_swichChangeListener);
        _swFunction.setOnCheckedChangeListener(_swichChangeListener);
        _swBack.setOnCheckedChangeListener(_swichChangeListener);
        _swHome.setOnCheckedChangeListener(_swichChangeListener);
        _swAppSwitch.setOnCheckedChangeListener(_swichChangeListener);
        _swPower.setOnCheckedChangeListener(_swichChangeListener);
        _swInputParam.setOnCheckedChangeListener(_swichChangeListener);
    }

    /* init SpinnerView */
    private void initSpnners() {
        // create instance
        _spDpadUp = (Spinner) findViewById(R.id.spDpadUp);
        _spDpadDown = (Spinner) findViewById(R.id.spDpadDown);
        _spDpadLeft = (Spinner) findViewById(R.id.spDpadLeft);
        _spDpadRight = (Spinner) findViewById(R.id.spDpadRight);
        _spDpadCenter = (Spinner) findViewById(R.id.spDpadCenter);
        _spVolumeUp = (Spinner) findViewById(R.id.spVolumeUp);
        _spVolumeDown = (Spinner) findViewById(R.id.spVolumeDown);
        _spFunction = (Spinner) findViewById(R.id.spFunction);

        // get spinner value
        ArrayList<Pair<String, String>> sortItemList = new ArrayList<Pair<String, String>>();
        Resources resources = getResources();
        TypedArray sortItemArray = resources.obtainTypedArray(R.array.keycode_spinner_data);
        int arrayLength = sortItemArray.length();
        for (int i = 0; i < arrayLength; i++) {
            int id = sortItemArray.getResourceId(i, -1);
            if (id != -1) {
                String[] item = resources.getStringArray(id);
                sortItemList.add(new Pair<String, String>(item[0], item[1]));
            }
        }
        sortItemArray.recycle();

        // set adapter to spinner
        _spinnerAdapter = new KeyValuePairAdapter(this,
                android.R.layout.simple_spinner_item,
                sortItemList);

        // set Adapter
        _spDpadUp.setAdapter(_spinnerAdapter);
        _spDpadDown.setAdapter(_spinnerAdapter);
        _spDpadLeft.setAdapter(_spinnerAdapter);
        _spDpadRight.setAdapter(_spinnerAdapter);
        _spDpadCenter.setAdapter(_spinnerAdapter);
        _spVolumeUp.setAdapter(_spinnerAdapter);
        _spVolumeDown.setAdapter(_spinnerAdapter);
        _spFunction.setAdapter(_spinnerAdapter);
        // set current value
        _spDpadUp.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.DPAD_UP), false);
        _spDpadDown.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.DPAD_DOWN), false);
        _spDpadLeft.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.DPAD_LEFT), false);
        _spDpadRight.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.DPAD_RIGHT), false);
        _spDpadCenter.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.DPAD_CENTER), false);
        _spVolumeUp.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.VOLUME_UP), false);
        _spVolumeDown.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.VOLUME_DOWN), false);
        _spFunction.setSelection(getSpinnerKeyCodePos(Bt3sCustomKey.FUNCTION), false);
        // set listener
        _spinnerSelectListener = new SpinnerSelectListener();
        _spDpadUp.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadDown.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadLeft.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadRight.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadCenter.setOnItemSelectedListener(_spinnerSelectListener);
        _spVolumeUp.setOnItemSelectedListener(_spinnerSelectListener);
        _spVolumeDown.setOnItemSelectedListener(_spinnerSelectListener);
        _spFunction.setOnItemSelectedListener(_spinnerSelectListener);
    }

    /* init TextView */
    public void initTextView() {
        _tvCurrentDpadUp = (TextView)findViewById(R.id.tvCurrentDpadUp);
        _tvCurrentDpadDown = (TextView)findViewById(R.id.tvCurrentDpadDown);
        _tvCurrentDpadLeft = (TextView)findViewById(R.id.tvCurrentDpadLeft);
        _tvCurrentDpadRight = (TextView)findViewById(R.id.tvCurrentDpadRight);
        _tvCurrentDpadCenter = (TextView)findViewById(R.id.tvCurrentDpadCenter);
        _tvCurrentVolumeUp = (TextView)findViewById(R.id.tvCurrentVolumeUp);
        _tvCurrentVolumeDown = (TextView)findViewById(R.id.tvCurrentVolumeDown);
        _tvCurrentFunction = (TextView)findViewById(R.id.tvCurrentFunction);
        _tvEventLog = (TextView)findViewById(R.id.tvEventLog);
        _svEventLog = (ScrollView)findViewById(R.id.svEventLog);
        _tvKeyEventLog = (TextView)findViewById(R.id.tvKeyEventLog);

        _tvCurrentDpadUp.setText(_spinnerAdapter.getItem(0).second);
        _tvCurrentDpadDown.setText(_spinnerAdapter.getItem(1).second);
        _tvCurrentDpadLeft.setText(_spinnerAdapter.getItem(2).second);
        _tvCurrentDpadRight.setText(_spinnerAdapter.getItem(3).second);
        _tvCurrentDpadCenter.setText(_spinnerAdapter.getItem(4).second);
        _tvCurrentVolumeUp.setText(_spinnerAdapter.getItem(5).second);
        _tvCurrentVolumeDown.setText(_spinnerAdapter.getItem(6).second);
        _tvCurrentFunction.setText(_spinnerAdapter.getItem(7).second);
        _tvEventLog.setText("");
        _tvEventLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        _tvKeyEventLog.setText("");
    }

    /* init Button */
    public void initButton() {
        _btnSetKeyAssign = (Button)findViewById(R.id.btnSetKeyAssign);
        _btnResetToDefult = (Button)findViewById(R.id.btnResetToDefult);
        _btnSetKeyAssign.setOnClickListener(this);
        _btnResetToDefult.setOnClickListener(this);
    }

    /* init EditText */
    public void initEditText() {
        _etPhysicalkey = (EditText)findViewById(R.id.etPhysicalkey);
        _etSetKeyEventCode = (EditText)findViewById(R.id.etSetKeyEventCode);
        _etGetKeyEventCode  = (EditText)findViewById(R.id.etGetKeyEventCode);
        _etGetKeyEventCode.setFocusable(false);
    }

    /* et the posiion of the specified KeyCode */
    public int getSpinnerKeyCodePos(int keyCode ) {
        Pair<String, String> selectedItem = null;
        int indx, pos = -1;
        int count = _spinnerAdapter.getCount();

        for (indx = 0; indx < count; indx++) {
            selectedItem = _spinnerAdapter.getItem(indx);
            if (Integer.parseInt(selectedItem.first) == keyCode) {
                pos = indx;
                break;
            }
        }
        return pos;
    }

    @Override
    public void onClick(View v) {
        int Result;
        if (v == _btnSetKeyAssign) {
            // setKeyAssign
            int phyKey, keyCode;
            boolean ret;
            try {
                phyKey = Integer.parseInt(_etPhysicalkey.getText().toString());
                keyCode = Integer.parseInt(_etSetKeyEventCode.getText().toString());
                ret = _bt3sCustomKey.setKeyAssign(phyKey, keyCode);

                setEvetLog(BT3S_API_SET_KEY_ASSIGN, String.valueOf(phyKey), String.valueOf(keyCode), String.valueOf(ret), true);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
            // getKeyAssign
            int keyAssign = _bt3sCustomKey.getKeyAssign(phyKey);

            setEvetLog(BT3S_API_GET_KEY_ASSIGN, String.valueOf(phyKey), String.valueOf(keyAssign), false);

            // Update the contents of the current value display EditText
            _etGetKeyEventCode.setText(String.valueOf(keyAssign));

        } else if(v == _btnResetToDefult) {
            // resetToDefault
            try {
                _bt3sCustomKey.resetToDefault();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return;
            }

            setEvetLog(BT3S_API_RESET_TO_DEFULT);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }

            // update view all KeyEnable and KeyAssign
            getAllCurrentKeyEnable(true);
            getAllCurrentKeyAssign();
        }
    }

    // SwichView CheckedChangeListener class
    public class SwichCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton bottonView, boolean IsChecked) {
            boolean ret, onoff;
            int bt3sCustomKey = 0;
            switch (bottonView.getId()) {
                case R.id.swDpadUp:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_UP;
                    break;
                case R.id.swDpadDown:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_DOWN;
                    break;
                case R.id.swDpadLeft:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_LEFT;
                    break;
                case R.id.swDpadRight:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_RIGHT;
                    break;
                case R.id.swDpadCenter:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_CENTER;
                    break;
                case R.id.swVolumeUp:
                    bt3sCustomKey = Bt3sCustomKey.VOLUME_UP;
                    break;
                case R.id.swVolumeDown:
                    bt3sCustomKey = Bt3sCustomKey.VOLUME_DOWN;
                    break;
                case R.id.swFunction:
                    bt3sCustomKey = Bt3sCustomKey.FUNCTION;
                    break;
                case R.id.swBack:
                    bt3sCustomKey = Bt3sCustomKey.BACK;
                    break;
                case R.id.swHome:
                    bt3sCustomKey = Bt3sCustomKey.HOME;
                    break;
                case R.id.swAppSwitch:
                    bt3sCustomKey = Bt3sCustomKey.APP_SWITCH;
                    break;
                case R.id.swPower:
                    bt3sCustomKey = Bt3sCustomKey.POWER;
                    break;
                case R.id.swInputParam:
                    try {
                        bt3sCustomKey = Integer.parseInt(_etPhysicalkey.getText().toString());
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getLocalizedMessage());
                        return;
                    }
                    break;
            }

            // setKeyEnable
            try {
                ret = _bt3sCustomKey.setKeyEnable(bt3sCustomKey, IsChecked);
            } catch (Exception e ) {
                Log.e(TAG, e.getMessage());
                return;
            }

            setEvetLog(BT3S_API_SET_KEY_ENABLE, String.valueOf(bt3sCustomKey), String.valueOf(IsChecked), String.valueOf(ret), true);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                onoff = _bt3sCustomKey.isKeyEnable(bt3sCustomKey);
            } catch (Exception e ) {
                Log.e(TAG, e.getMessage());
                return;
            }
            setEvetLog(BT3S_API_IS_KEY_ENABLE, String.valueOf(bt3sCustomKey), String.valueOf(onoff), false);

            // Update SwichView
            Switch currentSw = (Switch) findViewById(bottonView.getId());
            currentSw.setChecked(onoff);
        }
    }

    // SpinnerView ItemSelectedListener class
    public class SpinnerSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> patent, View view, int posision, long id) {
            Spinner spinner = (Spinner) patent;
            TextView textView = null;
            int bt3sCustomKey = 0;
            switch (spinner.getId()) {
                case R.id.spDpadUp:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_UP;
                    textView =_tvCurrentDpadUp;
                    break;
                case R.id.spDpadDown:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_DOWN;
                    textView =_tvCurrentDpadDown;
                    break;
                case R.id.spDpadLeft:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_LEFT;
                    textView =_tvCurrentDpadLeft;
                    break;
                case R.id.spDpadRight:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_RIGHT;
                    textView =_tvCurrentDpadRight;
                    break;
                case R.id.spDpadCenter:
                    bt3sCustomKey = Bt3sCustomKey.DPAD_CENTER;
                    textView =_tvCurrentDpadCenter;
                    break;
                case R.id.spVolumeUp:
                    bt3sCustomKey = Bt3sCustomKey.VOLUME_UP;
                    textView =_tvCurrentVolumeUp;
                    break;
                case R.id.spVolumeDown:
                    bt3sCustomKey = Bt3sCustomKey.VOLUME_DOWN;
                    textView =_tvCurrentVolumeDown;
                    break;
                case R.id.spFunction:
                    bt3sCustomKey = Bt3sCustomKey.FUNCTION;
                    textView =_tvCurrentFunction;
                    break;
            }
            // retrieve the selected Pair item
            Pair<String, String> selectedItem = _spinnerAdapter.getItem(spinner.getSelectedItemPosition());
            // key
            int keyCode = Integer.parseInt(selectedItem.first);

            // setKeyAssign
            boolean ret;
            try {
                ret = _bt3sCustomKey.setKeyAssign(bt3sCustomKey, keyCode);
            } catch (Exception e ) {
                Log.e(TAG, e.getMessage());
                return;
            }
            setEvetLog(BT3S_API_SET_KEY_ASSIGN, String.valueOf(bt3sCustomKey), String.valueOf(keyCode), String.valueOf(ret), true);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            // getKeyAssign
            int keyAssign = _bt3sCustomKey.getKeyAssign(bt3sCustomKey);
            setEvetLog(BT3S_API_GET_KEY_ASSIGN, String.valueOf(bt3sCustomKey), String.valueOf(keyAssign), false);

            // Update the contents of the current value display TextView
            textView.setText(BT3S_API_GET_KEY_ASSIGN + "( " + keyAssign + " )");
            int color;
            if (ret) {
                if (keyAssign == getDefultKeyAssign(textView)) {
                    color = Color.GREEN;
                } else {
                    color = Color.BLUE;
                }
            } else {
                color = Color.RED;
            }
            textView.setTextColor(color);
        }

        @Override
        public void onNothingSelected(AdapterView<?> var1) {
        }
    }

    /* get all current KeyEnable and update view */
    public void getAllCurrentKeyEnable(Boolean logON){
        // disable OnCheckedChangeListener
        _swDpadUp.setOnCheckedChangeListener(null);
        _swDpadLeft.setOnCheckedChangeListener(null);
        _swDpadDown.setOnCheckedChangeListener(null);
        _swDpadRight.setOnCheckedChangeListener(null);
        _swDpadCenter.setOnCheckedChangeListener(null);
        _swVolumeUp.setOnCheckedChangeListener(null);
        _swVolumeDown.setOnCheckedChangeListener(null);
        _swFunction.setOnCheckedChangeListener(null);
        _swBack.setOnCheckedChangeListener(null);
        _swHome.setOnCheckedChangeListener(null);
        _swAppSwitch.setOnCheckedChangeListener(null);
        _swPower.setOnCheckedChangeListener(null);
        // current KeyEnable and update view
        getCurrentKeyEnable(_swDpadUp, Bt3sCustomKey.DPAD_UP, logON);
        getCurrentKeyEnable(_swDpadDown, Bt3sCustomKey.DPAD_DOWN, logON);
        getCurrentKeyEnable(_swDpadLeft, Bt3sCustomKey.DPAD_LEFT, logON);
        getCurrentKeyEnable(_swDpadRight, Bt3sCustomKey.DPAD_RIGHT, logON);
        getCurrentKeyEnable(_swDpadCenter, Bt3sCustomKey.DPAD_CENTER, logON);
        getCurrentKeyEnable(_swVolumeUp, Bt3sCustomKey.VOLUME_UP, logON);
        getCurrentKeyEnable(_swVolumeDown, Bt3sCustomKey.VOLUME_DOWN, logON);
        getCurrentKeyEnable(_swFunction, Bt3sCustomKey.FUNCTION, logON);
        getCurrentKeyEnable(_swBack, Bt3sCustomKey.BACK, logON);
        getCurrentKeyEnable(_swHome, Bt3sCustomKey.HOME, logON);
        getCurrentKeyEnable(_swAppSwitch, Bt3sCustomKey.APP_SWITCH, logON);
        getCurrentKeyEnable(_swPower, Bt3sCustomKey.POWER, logON);
        // enable OnCheckedChangeListener
        _swDpadUp.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadLeft.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadDown.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadRight.setOnCheckedChangeListener(_swichChangeListener);
        _swDpadCenter.setOnCheckedChangeListener(_swichChangeListener);
        _swVolumeUp.setOnCheckedChangeListener(_swichChangeListener);
        _swVolumeDown.setOnCheckedChangeListener(_swichChangeListener);
        _swFunction.setOnCheckedChangeListener(_swichChangeListener);
        _swBack.setOnCheckedChangeListener(_swichChangeListener);
        _swHome.setOnCheckedChangeListener(_swichChangeListener);
        _swAppSwitch.setOnCheckedChangeListener(_swichChangeListener);
        _swPower.setOnCheckedChangeListener(_swichChangeListener);
    }

    /* get all current KeyAssign and update view */
    public void getAllCurrentKeyAssign(){
        // disable OnItemSelectedListener
        _spDpadUp.setOnItemSelectedListener(null);
        _spDpadDown.setOnItemSelectedListener(null);
        _spDpadLeft.setOnItemSelectedListener(null);
        _spDpadRight.setOnItemSelectedListener(null);
        _spDpadCenter.setOnItemSelectedListener(null);
        _spVolumeUp.setOnItemSelectedListener(null);
        _spVolumeDown.setOnItemSelectedListener(null);
        _spFunction.setOnItemSelectedListener(null);
        // current KeyAssign and update view
        int currentDpadUp = getCurrentKeyAssign(_tvCurrentDpadUp, getDefultKeyAssign(_tvCurrentDpadUp), true);
        int currentDpadDown = getCurrentKeyAssign(_tvCurrentDpadDown, getDefultKeyAssign(_tvCurrentDpadDown), true);
        int currentDpadLeft = getCurrentKeyAssign(_tvCurrentDpadLeft, getDefultKeyAssign(_tvCurrentDpadLeft), true);
        int currentDpadRight = getCurrentKeyAssign(_tvCurrentDpadRight, getDefultKeyAssign(_tvCurrentDpadRight), true);
        int currentDpadCenter = getCurrentKeyAssign(_tvCurrentDpadCenter, getDefultKeyAssign(_tvCurrentDpadCenter), true);
        int currentVolumeUp = getCurrentKeyAssign(_tvCurrentVolumeUp, getDefultKeyAssign(_tvCurrentVolumeUp), true);
        int currentVolumeDown = getCurrentKeyAssign(_tvCurrentVolumeDown, getDefultKeyAssign(_tvCurrentVolumeDown), true);
        int currentFunction = getCurrentKeyAssign(_tvCurrentFunction, getDefultKeyAssign(_tvCurrentFunction), true);
        // select current KeyCode with Spinner
        _spDpadUp.setSelection(getSpinnerKeyCodePos(currentDpadUp), false);
        _spDpadDown.setSelection(getSpinnerKeyCodePos(currentDpadDown), false);
        _spDpadLeft.setSelection(getSpinnerKeyCodePos(currentDpadLeft), false);
        _spDpadRight.setSelection(getSpinnerKeyCodePos(currentDpadRight), false);
        _spDpadCenter.setSelection(getSpinnerKeyCodePos(currentDpadCenter), false);
        _spVolumeUp.setSelection(getSpinnerKeyCodePos(currentVolumeUp), false);
        _spVolumeDown.setSelection(getSpinnerKeyCodePos(currentVolumeDown), false);
        _spFunction.setSelection(getSpinnerKeyCodePos(currentFunction), false);
        // enable OnItemSelectedListener
        _spDpadUp.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadDown.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadLeft.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadRight.setOnItemSelectedListener(_spinnerSelectListener);
        _spDpadCenter.setOnItemSelectedListener(_spinnerSelectListener);
        _spVolumeUp.setOnItemSelectedListener(_spinnerSelectListener);
        _spVolumeDown.setOnItemSelectedListener(_spinnerSelectListener);
        _spFunction.setOnItemSelectedListener(_spinnerSelectListener);
    }

    /* current KeyAssign and update view */
    public Boolean getCurrentKeyEnable(Switch switchView, int bt3sCustomKey, boolean logOn) {
        // get KeyEnable
        Boolean keyEnable;
        try {
            keyEnable= _bt3sCustomKey.isKeyEnable(bt3sCustomKey);

            // update SwitchView
            switchView.setChecked(keyEnable);

        } catch (Exception e) {
            keyEnable = false;
        }
        return keyEnable;
    }

    // get defult key code
    public int getDefultKeyAssign(TextView textView) {
        int keyAssign = 0;
        if (textView == _tvCurrentDpadUp) {
            keyAssign = Bt3sCustomKey.DPAD_UP;
        } else if(textView == _tvCurrentDpadDown) {
            keyAssign = Bt3sCustomKey.DPAD_DOWN;
        } else if(textView == _tvCurrentDpadLeft) {
            keyAssign = Bt3sCustomKey.DPAD_LEFT;
        } else if(textView == _tvCurrentDpadRight) {
            keyAssign = Bt3sCustomKey.DPAD_RIGHT;
        } else if(textView == _tvCurrentDpadCenter) {
            keyAssign = Bt3sCustomKey.DPAD_CENTER;
        } else if(textView == _tvCurrentVolumeUp) {
            keyAssign = Bt3sCustomKey.VOLUME_UP;
        } else if(textView == _tvCurrentVolumeDown) {
            keyAssign = Bt3sCustomKey.VOLUME_DOWN;
        } else if(textView == _tvCurrentFunction) {
            keyAssign = Bt3sCustomKey.FUNCTION;
        }
        return keyAssign;
    }

    /* get current KeyAssign and update view */
    public int getCurrentKeyAssign(TextView textView , int bt3sCustomKey, Boolean sendRet) {
        int keyAssign = 0;
        int color = Color.GREEN;
        // getKeyAssign
        try {
            keyAssign = _bt3sCustomKey.getKeyAssign(bt3sCustomKey);

            // Update the contents of the current value display TextView
            textView.setText(BT3S_API_GET_KEY_ASSIGN + "( " + keyAssign + " )");
            if (sendRet) {
                if (keyAssign == getDefultKeyAssign(textView)) {
                    color = Color.GREEN;
                } else {
                    color = Color.BLUE;
                }
            } else {
                color = Color.RED;
            }
            textView.setTextColor(color);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return keyAssign;
    }

    public void setEvetLog(String method) {
        _tvEventLog.append(String.format("%s()", method) + "\n");
        setFullScroll();
    }

    public void setEvetLog(String method, String returnVale, Boolean setApi) {
        if (setApi) {
            _tvEventLog.append(String.format("%s() return: %s", method, returnVale) + " -> ");
        } else {
            _tvEventLog.append(String.format("%s() return: %s", method, returnVale) + "\n");
            setFullScroll();
        }
    }

    public void setEvetLog(String method, String parameter, String returnVale, Boolean setApi) {
        if (setApi) {
            _tvEventLog.append(String.format("%s( %s ) return: %s", method, parameter, returnVale) + " -> ");
        } else {
            _tvEventLog.append(String.format("%s( %s ) return: %s", method, parameter, returnVale) + "\n");
            setFullScroll();
        }
    }

    public void setEvetLog(String method, String parameter1, String parameter2, String returnVale, Boolean setApi) {
        if (setApi) {
            _tvEventLog.append(String.format("%s( %s, %s ) return: %s", method, parameter1, parameter2, returnVale) + " -> ");
        } else {
            _tvEventLog.append(String.format("%s( %s, %s ) return: %s", method, parameter1, parameter2, returnVale) + "\n");
            setFullScroll();
        }
    }

    // Full Scroll
    public void setFullScroll() {
        _svEventLog.post(new Runnable() {
            public void run() {
                _svEventLog.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (getSpinnerKeyCodePos(event.getKeyCode()) != -1) {
            _tvKeyEventLog.setText("KeyCode:" + event.getKeyCode()  + "\n" + event.toString());
        }
        return super.dispatchKeyEvent(event);
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
