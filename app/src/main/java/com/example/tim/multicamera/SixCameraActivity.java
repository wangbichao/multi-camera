package com.example.tim.multicamera;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import com.example.tim.multicamera.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.example.tim.multicamera.util.SystemUiHider;

import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import android.view.SurfaceView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.hardware.Camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.graphics.ImageFormat;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SixCameraActivity extends Activity implements OnClickListener {
    public final String TAG = SixCameraActivity.class.getSimpleName();
    private static int MAX_CAMERA = 6;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    //private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    //private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    //private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private int Camera_num = 0;
    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    //private SystemUiHider mSystemUiHider;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private SurfaceView[] mSurfaceView;
    private Camera[] mCamera;
    private MultiOpenCameraThread[] mOpenThread;
    private MultiCloseCameraThread[] mCloseThread;
    private Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    private Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    private Handler mHandler;
    private int count = 0;
    private Button mCameraTestButton1;
    private Button mCameraTestButton2;
    private Button mCameraTestButton3;
    private Camera.Parameters mParameters;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    //private static Object lock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);

        requestPermission(1, Manifest.permission.CAMERA, new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "App have permission to open camera");
            }
        }, new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "App have NO permission to open camera");
            }
        });

        setContentView(R.layout.activity_six_camera);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        mSurfaceView = new SurfaceView[MAX_CAMERA];
        mCamera = new Camera[MAX_CAMERA];
        mOpenThread = new MultiOpenCameraThread[MAX_CAMERA];
        mCloseThread = new MultiCloseCameraThread[MAX_CAMERA];

        mSurfaceView[0] = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceView[1] = (SurfaceView) findViewById(R.id.surfaceView2);
        mSurfaceView[2] = (SurfaceView) findViewById(R.id.surfaceView3);
        mSurfaceView[3] = (SurfaceView) findViewById(R.id.surfaceView4);
        mSurfaceView[4] = (SurfaceView) findViewById(R.id.surfaceView5);
        mSurfaceView[5] = (SurfaceView) findViewById(R.id.surfaceView6);

        for (int i = 0; i < MAX_CAMERA; i++) {
            mOpenThread[i] = new MultiOpenCameraThread(i);
        }

        for (int i = 0; i < MAX_CAMERA; i++) {
            mCloseThread[i] = new MultiCloseCameraThread(i);
        }

        Camera_num = Camera.getNumberOfCameras();
        Log.d(TAG, " Number of Cameras is " + Camera_num);

        // open all camera
//        for (int i = 0; i < MAX_CAMERA; i++) {
//            mOpenThread[i].start();
//        }

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        //count++;
                        //Toast.makeText(SixCameraActivity.this, "" + count, Toast.LENGTH_SHORT).show();
                        //mOpenThread[4].start();
                        //mOpenThread[5].start();

                        break;
                    default:
                        break;
                }
            }
        };


        /*mOpenThread[0].start();
        mOpenThread[1].start();
        mOpenThread[2].start();
        mOpenThread[3].start();*/

        mCameraTestButton1 = (Button) findViewById(R.id.button1);
        mCameraTestButton2 = (Button) findViewById(R.id.button2);
        mCameraTestButton3 = (Button) findViewById(R.id.button3);

        mCameraTestButton1.setOnClickListener(this);
        mCameraTestButton2.setOnClickListener(this);
        mCameraTestButton3.setOnClickListener(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mCameraTestButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mCameraTestButton1.setBackgroundColor(Color.TRANSPARENT);
                    mCameraTestButton3.setBackgroundColor(Color.TRANSPARENT);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mCameraTestButton1.setBackgroundColor(Color.YELLOW);
                    mCameraTestButton3.setBackgroundColor(Color.YELLOW);
                }

                return false;
            }
        });
        mCameraTestButton2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mCameraTestButton2.setBackgroundColor(Color.TRANSPARENT);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mCameraTestButton2.setBackgroundColor(Color.YELLOW);
                }

                return false;
            }
        });
        mCameraTestButton3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mCameraTestButton1.setBackgroundColor(Color.TRANSPARENT);
                    mCameraTestButton3.setBackgroundColor(Color.TRANSPARENT);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mCameraTestButton1.setBackgroundColor(Color.YELLOW);
                    mCameraTestButton3.setBackgroundColor(Color.YELLOW);
                }

                return false;
            }
        });
    }

    //permission strat
    protected void requestPermission(int id, String permission, Runnable allowableRunnable, Runnable disallowableRunnable) {
        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        allowablePermissionRunnables.put(id, allowableRunnable);
        if (disallowableRunnable != null) {
            disallowablePermissionRunnables.put(id, disallowableRunnable);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SixCameraActivity.this, new String[]{permission}, id);
                return;
            } else {
                allowableRunnable.run();
            }
        } else {
            allowableRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Runnable allowRun = allowablePermissionRunnables.get(requestCode);
            allowRun.run();
        } else {
            Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
            disallowRun.run();
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SixCamera Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    public class MultiOpenCameraThread extends Thread {

        private int camera_id;

        public MultiOpenCameraThread(int camera_id) {
            this.camera_id = camera_id;
        }

        public void run() {
            Log.d(TAG, "open camera" + camera_id + " ---Number of  Cameras is " + Camera_num);

            if (Camera_num <= camera_id) {
                Log.d(TAG, "Not found camera :" + camera_id);
                return;
            }
            if (null == mCamera[camera_id]) {

                Log.d(TAG, "new thread open camera is " + camera_id);
                mCamera[camera_id] = Camera.open(camera_id);
                count = camera_id;
                Message msg = new Message();
                msg.what = 0;
                mHandler.sendMessage(msg);
                mParameters = mCamera[camera_id].getParameters();
                mParameters.set("CameraHalNum",6);
                mCamera[camera_id].setParameters(mParameters);

                try {
                    //mSurfaceView[camera_id].setZOrderOnTop(false);
                    //mSurfaceView[camera_id].getHolder().setFormat(PixelFormat.TRANSPARENT);
                    mCamera[camera_id].setPreviewDisplay(mSurfaceView[camera_id].getHolder());

/*
                    if (camera_id == 0) {
                        if((width0 == 0)||(height0 == 0)){
                            width0 = 1920;
                            height0 = 1080;
                        }
                        if(PreSupFormat1 == null)
                            PreSupFormat1 = ImageFormat.NV21;
                        Log.d(TAG, "camera preview size" + width0 + "X" + height0);
                        //mParameters1.setPreviewSize(PreSupSize1.width,PreSupSize1.height);
                        mParameters1.setPreviewSize((int)width0,(int)height0);
                        mParameters1.set("CameraHalField",CameraHalField0);
                        mParameters1.setPreviewFormat(Integer.valueOf(PreSupFormat1.intValue()));
                        mCamera[camera_id].setParameters(mParameters1);

                    }
                    if (camera_id == 1) {
                        if((width1 == 0)||(height1 == 0)){
                            width1 = 1920;
                            height1 = 1080;
                        }
                        if(PreSupFormat2 == null)
                            PreSupFormat2 = ImageFormat.NV21;
                        Log.d(TAG, "camera preview size" + width1 + "X" + height1);
                        mParameters2.setPreviewSize((int)width1,(int)height1);
                        mParameters2.set("CameraHalField",CameraHalField1);
                        mParameters2.setPreviewFormat(Integer.valueOf(PreSupFormat2.intValue()));
                        mCamera[camera_id].setParameters(mParameters2);
                    }
*/
                    mCamera[camera_id].startPreview();

                } catch (IOException e) {
                    Log.d(TAG, "error");
                    mCamera[camera_id].release();
                    mCamera[camera_id] = null;
                } finally {
                    // synchronized (lock) {

//                                Message msg = new Message();
//                                msg.what = 0;
//                                mHandler.sendMessage(msg);

                }
                //}
                //}
            } else {
                Log.d(TAG, "camera" + camera_id + "is always open");
            }
        }

    }

    public class MultiCloseCameraThread extends Thread {

        private int camera_id;

        public MultiCloseCameraThread(int camera_id) {
            this.camera_id = camera_id;
        }

        public void run() {
            Log.d(TAG, "close camera" + camera_id + " ---Number of  Cameras is " + Camera_num);

            if (Camera_num <= camera_id) {
                Log.d(TAG, "Not found camera :" + camera_id);
                return;
            }
            if (null != mCamera[camera_id]) {
                Log.d(TAG, "new thread close camera is " + camera_id);
                mCamera[camera_id].stopPreview();
                mCamera[camera_id].release();
                mCamera[camera_id] = null;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "camera" + camera_id + "is not open");
            }
        }

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
/*
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
             }
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
*/

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
/*
    private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
*/
    @Override
    protected void onStart() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onResume() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onResume();
        // open all camera
        //for (int i = 0; i < Camera_num; i++) {
        //    mOpenThread[i].start();
        //}
    }

    @Override
    protected void onPause() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onPause();
        try {
            for (int i = 0; i < Camera_num; i++) {
                mCloseThread[i].start();
            }
        } catch (Exception e) {
            Log.d(TAG, "" + e.getMessage());
        }

    }

    @Override
    protected void onStop() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        //AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.disconnect();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onDestroy();
    }

    public void onClick(View v) {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());

        switch (v.getId()) {
            case R.id.button1:
                mOpenThread[4].start();
                mOpenThread[5].start();
                break;
            case R.id.button2:
                mOpenThread[0].start();
                mOpenThread[1].start();
                mOpenThread[2].start();
                mOpenThread[3].start();
                break;
            case R.id.button3:
                mOpenThread[4].start();
                mOpenThread[5].start();
                break;
            default:
                break;
        }
    }


}
