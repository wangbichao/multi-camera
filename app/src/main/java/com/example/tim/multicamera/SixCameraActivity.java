package com.example.tim.multicamera;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import com.example.tim.multicamera.R;
//import com.example.tim.multicamera.util.SystemUiHider;

import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
public class SixCameraActivity extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onCreate(savedInstanceState);

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
        Log.d(TAG, " Number of  Cameras is " + Camera_num);

        // open all camera
        for (int i = 0; i < Camera_num; i++) {
            mOpenThread[i].start();
        }
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
                try {
                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        Log.d(TAG, "Time out waiting to lock camera opening.");
                    }
                    Log.d(TAG, "new thread open camera is " + camera_id);
                    mCamera[camera_id] = Camera.open(camera_id);
/*
                    if ((camera_id == 0)) {
                        mParameters1 = mCamera[camera_id].getParameters();
                        PreSupSizeList1 = mParameters1.getSupportedPreviewSizes();
                        //if(PreSupSize1 == null){
                        //    Log.d(TAG, "Have no preview size parameters and use the default parameters.");
                        //    PreSupSize1 =  PreSupSizeList1.get(0);
                        //}
                       for(int num=0; num<PreSupSizeList1.size(); num++) {
                           PreSupSize1 =  PreSupSizeList1.get(num);
                           Log.d(TAG, "PreviewSizes = " +PreSupSize1.width+ "X" + PreSupSize1.height);
                       }
//                        PreSupFormatList1 = mParameters1.getSupportedPreviewFormats();
//                        if(PreSupFormat1 == null){
//                            Log.d(TAG, "Have no preview format parameters and use the default parameters.");
//                            PreSupFormat1 = PreSupFormatList1.get(0);
//                        }
//                        for (int num=0; num<PreSupFormatList1.size(); num++) {
//                            PreSupFormat1 = PreSupFormatList1.get(num);
//                            Log.d(TAG, "PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
//                        }
                    }
                    if ((camera_id == 1)) {
                        mParameters2 = mCamera[camera_id].getParameters();
                        //PreSupSizeList2 = mParameters2.getSupportedPreviewSizes();
                        //if(PreSupSize2 == null){
                        //    Log.d(TAG, "Have no preview size parameters and use the default parameters.");
                        //    PreSupSize2 =  PreSupSizeList2.get(0);
                        //}
//                        for(int num=0; num<PreSupSizeList2.size(); num++) {
//                            PreSupSize2 =  PreSupSizeList2.get(num);
//                            Log.d(TAG, "PreviewSizes = " +PreSupSize2.width+ "X" + PreSupSize2.height);
//                        }
                        //PreSupFormatList2 = mParameters2.getSupportedPreviewFormats();
                        //if(PreSupFormat2 == null){
                        //    Log.d(TAG, "Have no preview format parameters and use the default parameters.");
                        //    PreSupFormat2 = PreSupFormatList2.get(0);
                        //}
//                        for (int num=0; num<PreSupFormatList2.size(); num++) {
//                            PreSupFormat2 = PreSupFormatList2.get(num);
//                            Log.d(TAG, "PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
//                        }
                    }
*/
                } catch (InterruptedException e) {
                    Log.d(TAG, "Interrupted while trying to lock camera opening.", e);
                }
                try {
                    //mSurfaceView[camera_id].setZOrderOnTop(false);
                    //mSurfaceView[camera_id].getHolder().setFormat(PixelFormat.TRANSLUCENT);
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
                    mCamera[camera_id].release();
                    mCamera[camera_id] = null;
                } finally {
                    mCameraOpenCloseLock.release();
                }
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
                try {
                    mCameraOpenCloseLock.acquire();
                    Log.d(TAG, "new thread close camera is " + camera_id);
                    mCamera[camera_id].stopPreview();
                    mCamera[camera_id].release();
                    mCamera[camera_id] = null;
                    //mSurfaceView[camera_id].setZOrderOnTop(true);
                    //mSurfaceView[camera_id].getHolder().setFormat(PixelFormat.TRANSPARENT);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Interrupted while trying to lock camera closing.", e);
                } finally {
                    mCameraOpenCloseLock.release();
                }
/*
                Canvas mCanvas=null;
                try {
                    mCanvas =mSurfaceView[camera_id].getHolder().lockCanvas(null);
                    //mCanvas.drawColor(Color.WHITE);
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
                } catch (Exception e) {
                    Log.d(TAG, "Interrupted while trying to lock camera closing.", e);
                } finally {
                    mSurfaceView[camera_id].getHolder().unlockCanvasAndPost(mCanvas);
                }
*/
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
        super.onStart();
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
        for (int i = 0; i < Camera_num; i++) {
            mCloseThread[i].start();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onDestroy();
    }

}
