package com.example.tim.multicamera;

import java.io.IOException;

import android.app.Activity;
import android.graphics.ImageFormat;
import com.example.tim.multicamera.R;
//import com.example.tim.multicamera.util.SystemUiHider;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MultiCameraActivity extends Activity implements OnClickListener,FragmentCompat.OnRequestPermissionsResultCallback{
	public final String TAG = MultiCameraActivity.class.getSimpleName();
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	//private static final boolean AUTO_HIDE = true;
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

    private boolean Flag_CameraRelease = false;
	private boolean[] CAMERA_INITED;
//	private Button[] mCameraTestButton;
	private SurfaceView[] mSurfaceView;
	private Camera[] mCamera;
    private MultiOpenCameraThread[] mOpenThread;
    private MultiCloseCameraThread[] mCloseThread;
	private Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
	private Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
		super.onCreate(savedInstanceState);

        requestPermission(1, Manifest.permission.CAMERA, new Runnable() {
           @Override
           public void run() {
            Log.d(TAG ,"App have permission to open camera");
           }
        }, new Runnable() {
           @Override
           public void run() {
            Log.e(TAG ,"App have NO permission to open camera");
           }
        });

		setContentView(R.layout.activity_multi_camera);
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

        CAMERA_INITED = new boolean[MAX_CAMERA];
//    	mCameraTestButton = new Button[MAX_CAMERA];
    	mSurfaceView = new SurfaceView[MAX_CAMERA];
    	mCamera = new Camera[MAX_CAMERA];
        mOpenThread = new MultiOpenCameraThread[MAX_CAMERA];
        mCloseThread = new MultiCloseCameraThread[MAX_CAMERA];

        for(int i=0; i<MAX_CAMERA; i++) {
            CAMERA_INITED[i] = false;
            mCamera[i] = null;
        }
		mSurfaceView[0] = (SurfaceView)findViewById(R.id.surfaceView1);
		mSurfaceView[1] = (SurfaceView)findViewById(R.id.surfaceView2);
		mSurfaceView[2] = (SurfaceView)findViewById(R.id.surfaceView3);
		mSurfaceView[3] = (SurfaceView)findViewById(R.id.surfaceView4);
		mSurfaceView[4] = (SurfaceView)findViewById(R.id.surfaceView5);
		mSurfaceView[5] = (SurfaceView)findViewById(R.id.surfaceView6);

//		mCameraTestButton[0] = (Button)findViewById(R.id.button1);
//		mCameraTestButton[1] = (Button)findViewById(R.id.button2);
//		mCameraTestButton[2] = (Button)findViewById(R.id.button3);
//		mCameraTestButton[3] = (Button)findViewById(R.id.button4);
//		mCameraTestButton[4] = (Button)findViewById(R.id.button5);
//		mCameraTestButton[5] = (Button)findViewById(R.id.button6);

//        for(int i=0; i<MAX_CAMERA; i++) {
//    		mCameraTestButton[i].setOnClickListener(this);
//        }

        for(int i=0; i<MAX_CAMERA; i++) {
               mOpenThread[i] = new MultiOpenCameraThread(i);
        }

        for(int i=0; i<MAX_CAMERA; i++) {
               mCloseThread[i] = new MultiCloseCameraThread(i);
        }

		Camera_num = Camera.getNumberOfCameras();
		Log.d(TAG ," Number of  Cameras is " + Camera_num);


		//mSurfaceView.setOnClickListener((OnClickListener) this);
/*
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,HIDER_FLAGS);
		mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            // Cached values.
            int mControlsHeight;
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
            	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            		// If the ViewPropertyAnimator API is available
            		// (Honeycomb MR2 and later), use it to animate the
            		// in-layout UI controls at the bottom of the
            		// screen.
            		if (mControlsHeight == 0) {
            			mControlsHeight = controlsView.getHeight();
            		}
            		if (mShortAnimTime == 0) {
            			mShortAnimTime = getResources().getInteger(
            					android.R.integer.config_shortAnimTime);
            		}
            		controlsView
            				.animate()
            				.translationY(visible ? 0 : mControlsHeight)
            				.setDuration(mShortAnimTime);
            	} else {
            		// If the ViewPropertyAnimator APIs aren't
            		// available, simply show or hide the in-layout UI
            		// controls.
            		controlsView.setVisibility(visible ? View.VISIBLE
            				: View.GONE);
            	}

            	if (visible && AUTO_HIDE) {
            		// Schedule a hide().
            		delayedHide(AUTO_HIDE_DELAY_MILLIS);
            	}
            }
            });

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
*/
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		//findViewById(R.id.dummy_button).setOnTouchListener(
		//		mDelayHideTouchListener);
	}


    //permission strat
  /**
     * \u8bf7\u6c42\u6743\u9650
     * @param id \u8bf7\u6c42\u6388\u6743\u7684id \u552f\u4e00\u6807\u8bc6\u5373\u53ef
     * @param permission \u8bf7\u6c42\u7684\u6743\u9650
     * @param allowableRunnable \u540c\u610f\u6388\u6743\u540e\u7684\u64cd\u4f5c
     * @param disallowableRunnable \u7981\u6b62\u6743\u9650\u540e\u7684\u64cd\u4f5c
     */
    protected void requestPermission(int id, String permission, Runnable allowableRunnable, Runnable disallowableRunnable) {
        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

		allowablePermissionRunnables.put(id, allowableRunnable);
        if (disallowableRunnable != null) {
            disallowablePermissionRunnables.put(id, disallowableRunnable);
        }

        //\u7248\u672c\u5224\u65ad
        if (Build.VERSION.SDK_INT >= 23) {
            //\u51cf\u5c11\u662f\u5426\u62e5\u6709\u6743\u9650
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //\u5f39\u51fa\u5bf9\u8bdd\u6846\u63a5\u6536\u6743\u9650
                ActivityCompat.requestPermissions(MultiCameraActivity.this, new String[]{permission}, id);
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
        Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Runnable allowRun = allowablePermissionRunnables.get(requestCode);
            allowRun.run();
        } else {
            Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
            disallowRun.run();
        }
    }
    //permission end

    public class MultiOpenCameraThread extends Thread {

        private int camera_id;

    	public MultiOpenCameraThread(int camera_id) {
    		this.camera_id = camera_id;
    	}

    	public void run() {
    		Log.d(TAG ,"open camera" + camera_id + " ---Number of  Cameras is " + Camera_num);

            if (Camera_num <= camera_id) {
               	Log.d(TAG ,"Not found camera camera_id");
                return;
            }
            if(!CAMERA_INITED[camera_id]) {
        		Log.d(TAG ,"new thread open camera is " + camera_id);
                try {
                    mCamera[camera_id] = Camera.open(camera_id);
                	mCamera[camera_id].setPreviewDisplay(mSurfaceView[camera_id].getHolder());
                	mCamera[camera_id].startPreview();
				} catch (IOException e) {
                	mCamera[camera_id].release();
                	mCamera[camera_id] = null;
                	e.printStackTrace();
                }
            } else {
                Log.d(TAG ,"camera camera_id is always open");
            }

            CAMERA_INITED[camera_id] = true;
    	}

    }

    public class MultiCloseCameraThread extends Thread {

        private int camera_id;

    	public MultiCloseCameraThread(int camera_id) {
    		this.camera_id = camera_id;
    	}

    	public void run() {
    		Log.d(TAG ,"close camera" + camera_id + " ---Number of  Cameras is " + Camera_num);

            if (Camera_num <= camera_id) {
               	Log.d(TAG ,"Not found camera camera_id");
                return;
            }
            if(null != mCamera[camera_id]) {
        		Log.d(TAG ,"new thread close camera is " + camera_id);
                mCamera[camera_id].setPreviewCallback(null);
                mCamera[camera_id].stopPreview();
                mCamera[camera_id].release();
                mCamera[camera_id] = null;
            } else {
                Log.d(TAG ,"camera camera_id is not open");
            }
            CAMERA_INITED[camera_id] = false;
    	}

    }


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
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
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
		super.onStart();
    }

	@Override
	protected void onResume() {
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
		super.onResume();
        // open all camera
        if(!Flag_CameraRelease) {
            for(int i=0; i<Camera_num; i++){
                mOpenThread[i].start();
            }
            Flag_CameraRelease = true;
        }
	}

	@Override
	protected void onPause() {
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
		super.onPause();
        for(int i=0; i<Camera_num; i++)
            mCloseThread[i].start();
        Flag_CameraRelease = false;
	}

	@Override
	protected void onStop() {
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
		super.onStop();
	}

	@Override
	protected void onDestroy() {
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
		super.onDestroy();
        for(int i=0; i<Camera_num; i++)
            mCloseThread[i].start();
        Flag_CameraRelease = false;
    }

	@Override
	public void onClick(View v) {
	    Log.d(TAG,new Exception().getStackTrace()[0].getMethodName());
/*
		switch (v.getId()) {
		case R.id.button1:{
			mOpenThread[0].start();
		}
		break;
		case R.id.button2:{
                mOpenThread[1].start();
		}
		break;
		case R.id.button3:{
                mOpenThread[2].start();
		}
		break;
		case R.id.button4:{
                mOpenThread[3].start();
		}
		break;
		case R.id.button5:{
                mOpenThread[4].start();
		}
		break;
		case R.id.button6:{
                mOpenThread[5].start();
		}
		break;
		default:
			break;
		}
*/
    }

}
