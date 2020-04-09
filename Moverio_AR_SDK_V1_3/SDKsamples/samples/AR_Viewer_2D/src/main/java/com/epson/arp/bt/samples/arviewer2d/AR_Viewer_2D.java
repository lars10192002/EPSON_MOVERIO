/**
 *
 * Copyright (c) SEIKO EPSON CORPORATION 2016 - 2017. All rights reserved.
 *
 */
package com.epson.arp.bt.samples.arviewer2d;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Float3;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.epson.arp.bt.MoverioARActivity;
import com.epson.arp.bt.renderer.AmbientLight;
import com.epson.arp.bt.renderer.DirectionalLight;
import com.epson.arp.bt.renderer.Model;
import com.epson.arp.bt.renderer.Renderer;
import com.epson.arp.bt.renderer.Scene;
import com.epson.arp.bt.sdk.DisplayMode;
import com.epson.arp.bt.sdk.TrackingObject;
import com.epson.arp.bt.util.CallableModelLoader;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AR_Viewer_2D  extends MoverioARActivity implements Renderer.RenderListener
{
    public static final String TAG = AR_Viewer_2D.class.getName();

    private View mGUIView = null;
    private Renderer mRenderer = null;
    private ArrayList<TrackingObject> mArObjects = null;
    private ArrayList<Scene> mScenes = new ArrayList<>();

    private TextView mErrorTextView = null;

    private boolean bStereoMode = false;

    private static String TRAINING_DATA_FILE_PATH = "AR_Viewer_2D/AR_Viewer_2D.zip";
    private static final String TEXTURED_MODEL_FILE_PATH = "PaperCarModel/PaperCar.obj";
    private CallableModelLoader mModelPaperCarLoader;
    private Model mModelPaperCar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        mGUIView = View.inflate(this, R.layout.ar_track_ui_2d, null);

        addContentView(mGUIView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));

        if(AR_SDK == null)
        {
            mErrorTextView = (TextView) findViewById(R.id.text_view_error);
            mErrorTextView.setText("Failed to create AR SDK on this device");
            return;
        }

        mRenderer = new Renderer(this, AR_SDK);
        mRenderer.addView();
        mRenderer.addRenderListener(this);

        mGUIView.bringToFront();
        String cachedModelFilePath = getApplicationContext().getCacheDir().getAbsolutePath() + "/";
        mModelPaperCarLoader = (CallableModelLoader) new CallableModelLoader(Model.ModelType.OBJ, getAssets(), TEXTURED_MODEL_FILE_PATH, cachedModelFilePath + "PaperCar.e3d").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mArObjects = AR_SDK.createTrackingObjects(TRAINING_DATA_FILE_PATH);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mModelPaperCar = mModelPaperCarLoader.get();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Unable to load model: " + e);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Log.e(TAG, "Unable to load model: " + e);
                    e.printStackTrace();
                }

                if(mModelPaperCar != null) {
                    mModelPaperCar.position.z = 30;
                    for(int i=0; i<mArObjects.size(); i++) {
                        Scene scene = new Scene();
                        Model textModel = Model.createTextModel(mArObjects.get(i).getObjectName(), 0xffffff00);
                        if(textModel != null) {
                            textModel.position.y = 75;
                            textModel.scale.x = textModel.scale.y = textModel.scale.z = 100.0f;
                            scene.addModel(mModelPaperCar);
                            scene.addModel(textModel);
                            scene.addLight(new AmbientLight(new Float3(.6f, .6f, .6f)));
                            scene.addLight(new DirectionalLight(new Float3(.4f, .4f, .4f), new Float3(.66528066528f, .49896049896f, .37422037422f)));
                            mRenderer.addScene(scene);
                            scene.bindOriginTo(mArObjects.get(i));
                            mScenes.add(scene);
                        }
                        mRenderer.syncTracking(mArObjects.get(i));
                    }
                }
            }
        }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRenderer != null)
            mRenderer.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mRenderer != null)
            mRenderer.onResume();
    }

    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if(event.getAction() == KeyEvent.ACTION_UP) {
            int keyCode = event.getKeyCode();

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (bStereoMode)
                        return true;
                    // Switch to stereo mode
                    bStereoMode = true;
                    AR_SDK.setDisplayMode(DisplayMode.MODE_3D_WITHOUT_NOTIFICATION);
                    mRenderer.setDrawingCameraStream(false);
                    mRenderer.setStereoRendering(true);
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (!bStereoMode)
                        return true;
                    // Switch to camera video mode
                    bStereoMode = false;
                    AR_SDK.setDisplayMode(DisplayMode.MODE_2D_WITHOUT_NOTIFICATION);
                    mRenderer.setDrawingCameraStream(true);
                    mRenderer.setStereoRendering(false);
                    break;

                default:
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPreRender() { }

    @Override
    public void onPostRender() { }

}

