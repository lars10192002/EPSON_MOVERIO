/**
 *
 * Copyright (c) SEIKO EPSON CORPORATION 2016 - 2017. All rights reserved.
 *
 */
package com.epson.arp.bt.samples.arviewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar.LayoutParams;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Float3;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epson.arp.bt.MoverioARActivity;
import com.epson.arp.bt.renderer.AmbientLight;
import com.epson.arp.bt.renderer.DirectionalLight;
import com.epson.arp.bt.renderer.Model;
import com.epson.arp.bt.renderer.Renderer;
import com.epson.arp.bt.renderer.Scene;
import com.epson.arp.bt.renderer.Model.ModelType;
import com.epson.arp.bt.sdk.TrackingObject;
import com.epson.arp.bt.sdk.MoverioARTrackingListener;
import com.epson.arp.bt.sdk.DisplayMode;
import com.epson.arp.bt.util.CallableModelLoader;

public class AR_Viewer extends MoverioARActivity implements Renderer.RenderListener, MoverioARTrackingListener
{
    private static final String TAG = AR_Viewer.class.getName();

    private View mGUIView = null;
    private Renderer mRenderer = null;
    private Scene mScene = null;
    private Scene mGuidanceScene = null;
    private Scene mTextScene = null;
    private TrackingObject mArObject = null;
    private boolean bStereoMode = false;
    private ImageView mGuidanceImageView = null;
    private LinearLayout mGuidanceStereoLinearLayout = null;
    private ImageView mGuidanceImageView_left = null;
    private ImageView mGuidanceImageView_right = null;
    private TextView mErrorTextView = null;
    private boolean  mEnableImageGuidance = true;

    // set a Text Pose with Rotation fixed to be always facing user
    private static float[] mTextPose = { 0, -1,  0,  0,
                                        -1,  0,  0,  0,
                                         0,  0,  1,  0,
                                         0,  0,  0,  1};

    private static final String TRAINING_DATA_FILE_NOEXTENSION = "model";
    private static final String TEXTURED_MODEL_FILE_PATH = "TexturedModels/stopsign.obj";
    private static final String GUIDANCE_FOLDER = "guidance";

    private enum TRAINING_TYPE {
        textured_model_sparse_view,
        textured_model_dense_view,
        plain_model_sparse_view,
        plain_model_dense_view
    }
    public static TRAINING_TYPE TrainingType = TRAINING_TYPE.textured_model_dense_view; // set this enum to select model and associated training data

    private static String TRAINING_DATA_FOLDER = "";
    private static String TRAINING_DATA_IMAGE_FILE = "";
    private static String TRAINING_DATA_POSE_FILE = "";
    private static String TRAINING_DATA_FILE_PATH = "";

    //  Models
    private Model mModelPaperCar;   // Paper car
    private Model mModelHeadlights;  // Headlights
    private Model mModelBlueSiren;   // Blue siren lights (flashing)
    private Model mModelOrangeSiren; // Orange siren lights (flashing)
    private Model mModelText;
    private Model mModelStopSign;

    private Thread mAnimationThread = null;
    private volatile boolean mAnimationRunning;

    // Model loaders
    private CallableModelLoader mModelPaperCarLoader;
    private CallableModelLoader mModelHeadlightsLoader;
    private CallableModelLoader mModelBlueSirenLoader;
    private CallableModelLoader mModelOrangeSirenLoader;
    private CallableModelLoader mModelStopSignLoader;

    // 3D guidance model
    private Model mModelGuidance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mGUIView = View.inflate(this, R.layout.ar_track_ui, null);

        addContentView(mGUIView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if(AR_SDK == null)
        {
            mErrorTextView = (TextView) findViewById(R.id.text_view_error);
            mErrorTextView.setText("Failed to create AR SDK on this device");
            return;
        }

        boolean bIsLicenseValid = AR_SDK.unlockSDK("put-here-your-license-key");

        mRenderer = new Renderer(this, AR_SDK);
        mRenderer.addView();
        mRenderer.addRenderListener(this);

        mGUIView.bringToFront();

        String cachedModelFilePath = getApplicationContext().getCacheDir().getAbsolutePath() + "/";
        final Handler handler = new Handler();
        mScene = new Scene();
        mGuidanceScene = new Scene();
        mGuidanceScene.setAsGuidanceScene(true);
        mTextScene = new Scene();

        setTrainingDataPaths();

        // Model loading could take a long time to process
        // Load in non-GUI thread to prevent Application Not Responding (ANR) issues
        mModelPaperCarLoader = (CallableModelLoader) new CallableModelLoader(ModelType.OBJ, getResources(), R.raw.papercar, cachedModelFilePath + "papercar.e3d").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mModelHeadlightsLoader = (CallableModelLoader) new CallableModelLoader(ModelType.OBJ, getResources(), R.raw.headlights, cachedModelFilePath + "headlights.e3d").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mModelBlueSirenLoader = (CallableModelLoader) new CallableModelLoader(ModelType.OBJ, getResources(), R.raw.bluesiren, cachedModelFilePath + "bluesiren.e3d").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mModelOrangeSirenLoader = (CallableModelLoader) new CallableModelLoader(ModelType.OBJ, getResources(), R.raw.orangesiren, cachedModelFilePath + "orangesiren.e3d").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mModelStopSignLoader = (CallableModelLoader) new CallableModelLoader(ModelType.OBJ, getAssets(), TEXTURED_MODEL_FILE_PATH, cachedModelFilePath + "stopsign.e3d").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mAnimationRunning = true;

        // Thread for loading and customizing models
        new Thread(new Runnable() {
            @Override
            public void run() {
                mModelText = Model.createTextModel("Paper Car", 0xffffff00);

                // Load model from tracking data
                try {
                    mModelGuidance = AR_SDK.loadTrackingModel(TRAINING_DATA_FILE_PATH);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to load model: " + e);
                    e.printStackTrace();
                }

                try {
                    mModelPaperCar = mModelPaperCarLoader.get();
                    mModelHeadlights = mModelHeadlightsLoader.get();
                    mModelBlueSiren = mModelBlueSirenLoader.get();
                    mModelOrangeSiren = mModelOrangeSirenLoader.get();
                    mModelStopSign = mModelStopSignLoader.get();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Unable to load model: " + e);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Log.e(TAG, "Unable to load model: " + e);
                    e.printStackTrace();
                }

                // Set up guidance UI for user to find initialization view
                handler.post(new Runnable() {
                    public void run() {
                        mGuidanceImageView = (ImageView) findViewById(R.id.imageView_img);
                        mGuidanceStereoLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_stereo_imgs);
                        mGuidanceImageView_left = (ImageView) findViewById(R.id.imageView_img_left);
                        mGuidanceImageView_right = (ImageView) findViewById(R.id.imageView_img_right);
                        loadGuidanceImages();

                        if(mModelGuidance != null) {
                            mModelGuidance.scale.x = mModelGuidance.scale.y = mModelGuidance.scale.z = 1.0f;
                            mModelGuidance.setARGB(0x7F00FB01); // Make 3D guidance model semi-transparent and green
                            Model ghostModelGuidance = new Model(mModelGuidance);
                            ghostModelGuidance.isGhost = true;
                            mGuidanceScene.addModel(ghostModelGuidance);
                            mGuidanceScene.addModel(mModelGuidance);
                        }
                        mGuidanceScene.setPoseMatrix ( getAssets(), TRAINING_DATA_POSE_FILE);
                        if(mModelGuidance != null)
                            enableGuidanceUI(true);
                    }
                });

                if(mModelText != null) {
                    mModelText.scale.x = mModelText.scale.y = mModelText.scale.z = 200.0f;
                    mModelText.position.x = 70.0f;
                    mModelText.rotation.z = 270;

                    mTextScene.addModel(mModelText);
                }

                if(mModelPaperCar != null && mModelHeadlights != null && mModelBlueSiren != null
                        && mModelOrangeSiren != null && mModelStopSign != null) {
                    mModelPaperCar.scale.x = mModelPaperCar.scale.y = mModelPaperCar.scale.z = 1.0f;
                    mModelHeadlights.scale.x = mModelHeadlights.scale.y = mModelHeadlights.scale.z = 1.0f;
                    mModelBlueSiren.scale.x = mModelBlueSiren.scale.y = mModelBlueSiren.scale.z = 1.0f;
                    mModelOrangeSiren.scale.x = mModelOrangeSiren.scale.y = mModelOrangeSiren.scale.z = 1.0f;
                    mModelStopSign.scale.x = mModelStopSign.scale.y = mModelStopSign.scale.z = 1.0f;

                    // Enable ghost mode to simulate virtual object occlusion
                    mModelPaperCar.isGhost = true;

                    // Rendering is done in the order the models are added
                    // Add the occlusion model first to ensure proper draw order
                    mScene.addModel(mModelPaperCar);
                    mScene.addModel(mModelHeadlights);
                    mScene.addModel(mModelBlueSiren);
                    mScene.addModel(mModelOrangeSiren);
                    mScene.addModel(mModelStopSign);

                    // Thread for car siren animation
                    mAnimationThread = new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            synchronized (this) {
                                mModelBlueSiren.isGhost = !mModelBlueSiren.isGhost;
                                // Alternate visibility of the sirens to simulate blinking animation
                                while (mAnimationRunning)
                                {
                                    mModelBlueSiren.isGhost = !mModelBlueSiren.isGhost;
                                    mModelOrangeSiren.isGhost = !mModelOrangeSiren.isGhost;
                                    try
                                    {
                                        wait(300);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        // Do nothing the exception is expected
                                    }
                                }
                            }
                        }
                    });
                    mAnimationThread.start();
                }

            }
        }).start();

        // Add lights to give models a 3D appearance
        mScene.addLight(new AmbientLight(new Float3(.6f, .6f, .6f)));
        mScene.addLight(new DirectionalLight(new Float3(.4f, .4f, .4f), new Float3(.66528066528f, .49896049896f, .37422037422f)));
        mGuidanceScene.addLight(new AmbientLight(new Float3(.6f, .6f, .6f)));
        mGuidanceScene.addLight(new DirectionalLight(new Float3(.4f, .4f, .4f), new Float3(.66528066528f, .49896049896f, .37422037422f)));
        mTextScene.addLight(new AmbientLight(new Float3(.6f, .6f, .6f)));
        mTextScene.addLight(new DirectionalLight(new Float3(.4f, .4f, .4f), new Float3(.66528066528f, .49896049896f, .37422037422f)));

        mRenderer.addScene(mScene);
        mRenderer.addScene(mTextScene);
        mRenderer.addScene(mGuidanceScene);

        // Initialize object tracker parameters
        ArrayList<TrackingObject> trackingObjects = AR_SDK.createTrackingObjects(TRAINING_DATA_FILE_PATH);
        if(trackingObjects.size() == 1) {
            mArObject = trackingObjects.get(0);
            mScene.bindOriginTo(mArObject);
            mTextScene.bindOriginTo(mArObject);
            mTextScene.setPauseTracking(true);
            mGuidanceScene.bindOriginTo(mArObject);
            mArObject.addListener(this);
            mRenderer.syncTracking(mArObject);
        }
    }

    private void stopAnimationThread() throws InterruptedException {
        if(mAnimationThread == null) return;
        mAnimationRunning = false;
        mAnimationThread.interrupt();
        mAnimationThread = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            stopAnimationThread();
        } catch (InterruptedException e) {
            // Do nothing the exception is expected
        }
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

    @Override
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
                    mGuidanceScene.setVisibility(false);
                    enableGuidanceUI(true);
                    mModelText.scale.x = mModelText.scale.y = mModelText.scale.z = 100.0f;
                    mModelText.position.x = 56.0f;
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (!bStereoMode)
                        return true;
                    // Switch to camera video mode
                    bStereoMode = false;
                    AR_SDK.setDisplayMode(DisplayMode.MODE_2D_WITHOUT_NOTIFICATION);
                    mRenderer.setDrawingCameraStream(true);
                    mRenderer.setStereoRendering(false);
                    mGuidanceScene.setVisibility(true);
                    enableGuidanceUI(true);
                    mModelText.scale.x = mModelText.scale.y = mModelText.scale.z = 200.0f;
                    mModelText.position.x = 70.0f;
                    break;

                default:
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void loadGuidanceImages()
    {
        if (mGuidanceImageView == null || mGuidanceImageView_left == null || mGuidanceImageView_right == null) return;

        try
        {
            // Read training image from assets
            InputStream in = getAssets().open(TRAINING_DATA_IMAGE_FILE);
            Drawable d = Drawable.createFromStream(in, null);
            mGuidanceImageView.setImageDrawable(d);
            mGuidanceImageView_left.setScaleType(ScaleType.FIT_XY);
            mGuidanceImageView_right.setScaleType(ScaleType.FIT_XY);
            mGuidanceImageView_left.setImageDrawable(d);
            mGuidanceImageView_right.setImageDrawable(d);
        } catch (Exception e) {
            Log.e(TAG, "loadGuidanceImages load image file from assets error: " + e);
        }
    }

    // Enable/Disable image and model for guiding user to initialization view
    private void enableGuidanceUI(final boolean enable) {
        mEnableImageGuidance = enable;
        if (mGuidanceImageView == null || mGuidanceStereoLinearLayout == null) {
            Log.e(TAG, "Guidance View not available");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    if(bStereoMode) {
                        mGuidanceStereoLinearLayout.setVisibility(View.VISIBLE);
                        mGuidanceImageView.setVisibility(View.GONE);
                    } else {
                        mGuidanceStereoLinearLayout.setVisibility(View.GONE);
                        mGuidanceImageView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mGuidanceStereoLinearLayout.setVisibility(View.GONE);
                    mGuidanceImageView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onPreRender() { }

    @Override
    public void onPostRender() { }

    @Override
    public void onPose(String patternName, float[] poseMatrix, long timeStamp) {
        mTextPose[12] = poseMatrix[12];
        mTextPose[13] = poseMatrix[13];
        mTextPose[14] = poseMatrix[14];
        mTextScene.setPoseMatrix(mTextPose);
    }

    @Override
    public void onTracking(String patternName, boolean bTracking) {
        if(bTracking) {
            if(!mEnableImageGuidance)
                return;
            enableGuidanceUI(false);
        } else {
            if(mEnableImageGuidance)
                return;
            enableGuidanceUI(true);
        }
    }

    public void setTrainingDataPaths()
    {
        if( TrainingType == TRAINING_TYPE.textured_model_sparse_view) {
            TRAINING_DATA_FOLDER = "SparseTrainingForTexturedModel";
        } else if(TrainingType == TRAINING_TYPE.textured_model_dense_view) {
            TRAINING_DATA_FOLDER = "DenseTrainingForTexturedModel";
        } else if(TrainingType == TRAINING_TYPE.plain_model_sparse_view) {
            TRAINING_DATA_FOLDER = "SparseTrainingForPlainModel";
        } else if (TrainingType == TRAINING_TYPE.plain_model_dense_view) {
            TRAINING_DATA_FOLDER = "DenseTrainingForPlainModel";
        }

        TRAINING_DATA_FILE_PATH = TRAINING_DATA_FOLDER + "/" + TRAINING_DATA_FILE_NOEXTENSION + ".zip";
        TRAINING_DATA_IMAGE_FILE = TRAINING_DATA_FOLDER + "/" + GUIDANCE_FOLDER + "/" + TRAINING_DATA_FILE_NOEXTENSION + "1.jpg";
        TRAINING_DATA_POSE_FILE = TRAINING_DATA_FOLDER + "/" + GUIDANCE_FOLDER + "/" + TRAINING_DATA_FILE_NOEXTENSION + "1.sgt";
    }
}
