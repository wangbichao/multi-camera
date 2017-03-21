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

import static java.sql.Types.NULL;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MultiCameraActivity extends Activity implements OnCheckedChangeListener, FragmentCompat.OnRequestPermissionsResultCallback {
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

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private boolean[] CAMERA_INITED;
    //private Button[] mCameraTestButton;
    private Spinner[] mCameraTestSpinner;
    private SurfaceView[] mSurfaceView;
    private Camera[] mCamera;
    private MultiOpenCameraThread[] mOpenThread;
    private MultiCloseCameraThread[] mCloseThread;
    private Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    private Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    private ToggleButton mtogglebutton1;
    private ToggleButton mtogglebutton2;
    private PopupMenu pop;
    private Camera.Parameters mParameters1;
    private Camera.Parameters mParameters2;
    private List<Camera.Size> PreSupSizeList1;
    private List<Camera.Size> PreSupSizeList2;
    private List<Integer> PreSupFormatList1;
    private List<Integer> PreSupFormatList2;
    private Camera.Size PreSupSize1;
    private Camera.Size PreSupSize2;
    private Integer PreSupFormat1;
    private Integer PreSupFormat2;
//    private PopupMenu pop1;
//    private PopupMenu pop2;
//    private PopupMenu pop3;
//    private PopupMenu pop4;
//    private PopupMenu pop5;
//    private PopupMenu pop6;
    private int width0;
    private int height0;
    private int width1;
    private int height1;
    private int CameraHalField0;
    private int CameraHalField1;

    private int input_width0;
    private int input_height0;
    private int input_width1;
    private int input_height1;
    private Integer InputCameraFormat0;
    private Integer InputCameraFormat1;

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

        setContentView(R.layout.activity_multi_camera);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

//        CAMERA_INITED = new boolean[MAX_CAMERA];
//        mCameraTestButton = new Button[MAX_CAMERA];
        mCameraTestSpinner = new Spinner[10];
        mSurfaceView = new SurfaceView[MAX_CAMERA];
        mCamera = new Camera[MAX_CAMERA];
        mOpenThread = new MultiOpenCameraThread[MAX_CAMERA];
        mCloseThread = new MultiCloseCameraThread[MAX_CAMERA];

//        for(int i=0; i<MAX_CAMERA; i++) {
//            CAMERA_INITED[i] = false;
//            mCamera[i] = null;
//        }

        mSurfaceView[0] = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceView[1] = (SurfaceView) findViewById(R.id.surfaceView2);
        mSurfaceView[2] = (SurfaceView) findViewById(R.id.surfaceView3);
        mSurfaceView[3] = (SurfaceView) findViewById(R.id.surfaceView4);
        mSurfaceView[4] = (SurfaceView) findViewById(R.id.surfaceView5);
        mSurfaceView[5] = (SurfaceView) findViewById(R.id.surfaceView6);

//      mCameraTestButton[0] = (Button)findViewById(R.id.button1);
//      mCameraTestButton[1] = (Button)findViewById(R.id.button2);
//		mCameraTestButton[2] = (Button)findViewById(R.id.button3);
//		mCameraTestButton[3] = (Button)findViewById(R.id.button4);
//		mCameraTestButton[4] = (Button)findViewById(R.id.button5);
//		mCameraTestButton[5] = (Button)findViewById(R.id.button6);

        mCameraTestSpinner[0] = (Spinner) findViewById(R.id.spinner1);
        mCameraTestSpinner[1] = (Spinner) findViewById(R.id.spinner2);
        mCameraTestSpinner[2] = (Spinner) findViewById(R.id.spinner3);
        mCameraTestSpinner[3] = (Spinner) findViewById(R.id.spinner4);
        mCameraTestSpinner[4] = (Spinner) findViewById(R.id.spinner5);
        mCameraTestSpinner[5] = (Spinner) findViewById(R.id.spinner6);
        mCameraTestSpinner[6] = (Spinner) findViewById(R.id.spinner7);
        mCameraTestSpinner[7] = (Spinner) findViewById(R.id.spinner8);
        mCameraTestSpinner[8] = (Spinner) findViewById(R.id.spinner9);
        mCameraTestSpinner[9] = (Spinner) findViewById(R.id.spinner10);

//        for (int i = 0; i < MAX_CAMERA; i++) {
//            mCameraTestButton[i].setOnClickListener(this);
//        }

        //init togglebutton
        mtogglebutton1 = (ToggleButton) findViewById(R.id.togglebutton1);
        mtogglebutton2 = (ToggleButton) findViewById(R.id.togglebutton2);
        mtogglebutton1.setOnCheckedChangeListener(this);
        mtogglebutton2.setOnCheckedChangeListener(this);

        for (int i = 0; i < MAX_CAMERA; i++) {
            mOpenThread[i] = new MultiOpenCameraThread(i);
        }

        for (int i = 0; i < MAX_CAMERA; i++) {
            mCloseThread[i] = new MultiCloseCameraThread(i);
        }

        for (int i = 0; i < 10; i++) {
            mCameraTestSpinner[i].setOnItemSelectedListener(new SpinnerSelectedListener());
        }

        Camera_num = Camera.getNumberOfCameras();
        Log.d(TAG, " Number of  cameras is " + Camera_num);

        /*
        try {
            //execCommand("adb root");
            //execCommand("adb remount");
            //execCommand("adb shell setprop enable.multi.camera 1");
        } catch (IOException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
//        execShell("setprop enable.multi.camera 1");

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

    }

/*
    public void execShell(String cmd){
        try{
            Process p = Runtime.getRuntime().exec("su");
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }

    public void execCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line+" ");
            }
            System.out.println(stringBuffer.toString());
            Log.d(TAG,stringBuffer.toString());
        } catch (InterruptedException e) {
            System.err.println(e);
        }finally{
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }
    }
*/

    public class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            Log.d(TAG, " id =  " + id + " --- pos = " + pos);
            switch (parent.getId()) {
                case R.id.spinner1:
                    switch (pos) {
                        case 0:
                            CameraHalField0 = 0;
                            Log.d(TAG, "camera 0 progressive = " + CameraHalField0);
                            break;
                        case 1:
                            CameraHalField0 = 1;
                            Log.d(TAG, "camera 0 interlaced = " + CameraHalField0);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner2:
                    switch (pos) {
                        case 0:
                            width0 = 640;
                            height0 = 480;
                            Log.d(TAG, "camera 0 PreviewSizes = " + width0 + "X" + height0);
                            break;
                        case 1:
                            width0 = 720;
                            height0 = 480;
                            Log.d(TAG, "camera 0 PreviewSizes = " + width0 + "X" + height0);
                            break;
                        case 2:
                            width0 = 720;
                            height0 = 576;
                            Log.d(TAG, "camera 0 PreviewSizes = " + width0 + "X" + height0);
                            break;
                        case 3:
                            width0 = 800;
                            height0 = 480;
                            Log.d(TAG, "camera 0 PreviewSizes = " + width0 + "X" + height0);
                            break;
                        case 4:
                            width0 = 1280;
                            height0 = 720;
                            Log.d(TAG, "camera 0 PreviewSizes = " + width0 + "X" + height0);
                            break;
                        case 5:
                            width0 = 1920;
                            height0 = 1080;
                            Log.d(TAG, "camera 0 PreviewSizes = " + width0 + "X" + height0);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner3:
                    switch (pos) {
                        case 0:
                            PreSupFormat1 = ImageFormat.NV21;
                            Log.d(TAG, "camera 0 PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                            break;
                        case 1:
                            PreSupFormat1 = ImageFormat.YV12;
                            Log.d(TAG, "camera 0 PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                            break;
                        case 2:
                            PreSupFormat1 = ImageFormat.RGB_565;
                            Log.d(TAG, "camera 0 PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                            break;
                        case 3:
                            PreSupFormat1 = ImageFormat.NV16;
                            Log.d(TAG, "camera 0 PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                            break;
                        case 4:
                            PreSupFormat1 = ImageFormat.YUY2;
                            Log.d(TAG, "camera 0 PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                            break;
                        case 5:
                            PreSupFormat1 = ImageFormat.YUV_420_888;
                            Log.d(TAG, "camera 0 PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner4:
                    switch (pos) {
                        case 0:
                            CameraHalField1 = 0;
                            Log.d(TAG, "camera 1 progressive = " + CameraHalField1);
                            break;
                        case 1:
                            CameraHalField1 = 1;
                            Log.d(TAG, "camera 1 interlaced = " + CameraHalField1);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner5:
                    switch (pos) {
                        case 0:
                            width1 = 640;
                            height1 = 480;
                            Log.d(TAG, "camera 1 PreviewSizes = " + width1 + "X" + height1);
                            break;
                        case 1:
                            width1 = 720;
                            height1 = 480;
                            Log.d(TAG, "camera 1 PreviewSizes = " + width1 + "X" + height1);
                            break;
                        case 2:
                            width1 = 720;
                            height1 = 576;
                            Log.d(TAG, "camera 1 PreviewSizes = " + width1 + "X" + height1);
                            break;
                        case 3:
                            width1 = 800;
                            height1 = 480;
                            Log.d(TAG, "camera 1 PreviewSizes = " + width1 + "X" + height1);
                            break;
                        case 4:
                            width1 = 1280;
                            height1 = 720;
                            Log.d(TAG, "camera 1 PreviewSizes = " + width1 + "X" + height1);
                            break;
                        case 5:
                            width1 = 1920;
                            height1 = 1080;
                            Log.d(TAG, "camera 1 PreviewSizes = " + width1 + "X" + height1);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner6:
                    switch (pos) {
                        case 0:
                            PreSupFormat2 = ImageFormat.NV21;
                            Log.d(TAG, "camera 1 PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
                            break;
                        case 1:
                            PreSupFormat2 = ImageFormat.YV12;
                            Log.d(TAG, "camera 1 PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
                            break;
                        case 2:
                            PreSupFormat2 = ImageFormat.RGB_565;
                            Log.d(TAG, "camera 1 PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
                            break;
                        case 3:
                            PreSupFormat2 = ImageFormat.NV16;
                            Log.d(TAG, "camera 1 PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
                            break;
                        case 4:
                            PreSupFormat2 = ImageFormat.YUY2;
                            Log.d(TAG, "camera 1 PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
                            break;
                        case 5:
                            PreSupFormat2 = ImageFormat.YUV_420_888;
                            Log.d(TAG, "camera 1 PreSupFormat = " + getImageFormatString(PreSupFormat2.intValue()));
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner7:
                    switch (pos) {
                        case 0:
                            input_width0 = 640;
                            input_height0 = 480;
                            Log.d(TAG, "camera 0 Input PreviewSizes = " + input_width0 + "X" + input_height0);
                            break;
                        case 1:
                            input_width0 = 720;
                            input_height0 = 480;
                            Log.d(TAG, "camera 0 Input PreviewSizes = " + input_width0 + "X" + input_height0);
                            break;
                        case 2:
                            input_width0 = 720;
                            input_height0 = 576;
                            Log.d(TAG, "camera 0 Input PreviewSizes = " + input_width0 + "X" + input_height0);
                            break;
                        case 3:
                            input_width0 = 800;
                            input_height0 = 480;
                            Log.d(TAG, "camera 0 Input PreviewSizes = " + input_width0 + "X" + input_height0);
                            break;
                        case 4:
                            input_width0 = 1280;
                            input_height0 = 720;
                            Log.d(TAG, "camera 0 Input PreviewSizes = " + input_width0 + "X" + input_height0);
                            break;
                        case 5:
                            input_width0 = 1920;
                            input_height0 = 1080;
                            Log.d(TAG, "camera 0 Input PreviewSizes = " + input_width0 + "X" + input_height0);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner8:
                    switch (pos) {
                        case 0:
                            InputCameraFormat0 = NULL;
                            Log.d(TAG, "camera 0 Input PreSupFormat Default" );
                            break;
                        case 1:
                            InputCameraFormat0 = ImageFormat.YUY2;
                            Log.d(TAG, "camera 0 Input PreSupFormat = " + getImageFormatString(InputCameraFormat0.intValue()));
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner9:
                    switch (pos) {
                        case 0:
                            input_width1 = 640;
                            input_height1 = 480;
                            Log.d(TAG, "camera 1 Input PreviewSizes = " + input_width1 + "X" + input_height1);
                            break;
                        case 1:
                            input_width1 = 720;
                            input_height1 = 480;
                            Log.d(TAG, "camera 1 Input PreviewSizes = " + input_width1 + "X" + input_height1);
                            break;
                        case 2:
                            input_width1 = 720;
                            input_height1 = 576;
                            Log.d(TAG, "camera 1 Input PreviewSizes = " + input_width1 + "X" + input_height1);
                            break;
                        case 3:
                            input_width1 = 800;
                            input_height1 = 480;
                            Log.d(TAG, "camera 1 Input PreviewSizes = " + input_width1 + "X" + input_height1);
                            break;
                        case 4:
                            input_width1 = 1280;
                            input_height1 = 720;
                            Log.d(TAG, "camera 1 Input PreviewSizes = " + input_width1 + "X" + input_height1);
                            break;
                        case 5:
                            input_width1 = 1920;
                            input_height1 = 1080;
                            Log.d(TAG, "camera 1 Input PreviewSizes = " + input_width1 + "X" + input_height1);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.spinner10:
                    switch (pos) {
                        case 0:
                            InputCameraFormat1 = NULL;
                            input_width0 = 0;
                            input_height0 = 0;
                            input_width0 = 0;
                            input_height0 = 0;
                            Log.d(TAG, "camera 1 Input PreSupFormat Default" );
                            break;
                        case 1:
                            InputCameraFormat1 = ImageFormat.YUY2;
                            Log.d(TAG, "camera 1 Input PreSupFormat = " + getImageFormatString(InputCameraFormat1.intValue()));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    Log.d(TAG, "default,default,default");
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
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
     * @see ImageFormat
     */
    public static String getImageFormatString(int format) {
        switch (format) {
            case ImageFormat.JPEG:
                return "JPEG";
            case ImageFormat.NV16:
                return "NV16";
            case ImageFormat.NV21:
                return "NV21";
            case ImageFormat.RGB_565:
                return "RGB_565";
            case ImageFormat.YUY2:
                return "YUY2";
            case ImageFormat.YV12:
                return "YV12";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888";
            case ImageFormat.UNKNOWN:
            default:
                return "UNKNOWN";
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

                    if ((camera_id == 0)) {
                        mParameters1 = mCamera[camera_id].getParameters();
                        PreSupSizeList1 = mParameters1.getSupportedPreviewSizes();
                        //if(PreSupSize1 == null){
                        //    Log.d(TAG, "Have no preview size parameters and use the default parameters.");
                        //    PreSupSize1 =  PreSupSizeList1.get(0);
                        //}
                        for (int num = 0; num < PreSupSizeList1.size(); num++) {
                            PreSupSize1 = PreSupSizeList1.get(num);
                            Log.d(TAG, "camera PreviewSizes = " + PreSupSize1.width + "X" + PreSupSize1.height);
                        }
//                        PreSupFormatList1 = mParameters1.getSupportedPreviewFormats();
//                        if(PreSupFormat1 == null){
//                            Log.d(TAG, "Have no preview format parameters and use the default parameters.");
//                            PreSupFormat1 = PreSupFormatList1.get(0);
//                        }
//                        for (int num=0; num<PreSupFormatList1.size(); num++) {
//                            PreSupFormat1 = PreSupFormatList1.get(num);
//                            Log.d(TAG, "camera PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
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
//                            Log.d(TAG, "camera PreviewSizes = " +PreSupSize2.width+ "X" + PreSupSize2.height);
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
                } catch (InterruptedException e) {
                    Log.d(TAG, "Interrupted while trying to lock camera opening.", e);
                }
                try {
                    mSurfaceView[camera_id].setZOrderOnTop(false);
                    mSurfaceView[camera_id].getHolder().setFormat(PixelFormat.TRANSLUCENT);
                    mCamera[camera_id].setPreviewDisplay(mSurfaceView[camera_id].getHolder());
                    if (camera_id == 0) {
                        if ((width0 == 0) || (height0 == 0)) {
                            width0 = 1920;
                            height0 = 1080;
                        }
                        if (PreSupFormat1 == null)
                            PreSupFormat1 = ImageFormat.NV21;
                        Log.d(TAG, "camera " + camera_id + " progressive = " + CameraHalField0);
                        Log.d(TAG, "camera " + camera_id + " PreviewSizes = " + width0 + "X" + height0);
                        Log.d(TAG, "camera " + camera_id + " PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                        Log.d(TAG, "camera " + camera_id + " Input PreviewSizes = " + input_width0 + "X" + input_height0); 
                        Log.d(TAG, "camera " + camera_id + " Input PreSupFormat = " + getImageFormatString(InputCameraFormat0.intValue()));                       
                        mParameters1.setPreviewSize((int) width0, (int) height0);
                        mParameters1.set("CameraHalField", CameraHalField0);
                        mParameters1.set("CameraHalInputWidth", input_width0);
                        mParameters1.set("CameraHalInputHeight", input_height0);
                        mParameters1.set("CameraHalInputFormat", getImageFormatString(InputCameraFormat0.intValue()));
                        mParameters1.setPreviewFormat(Integer.valueOf(PreSupFormat1.intValue()));
                        mCamera[camera_id].setParameters(mParameters1);

                    }
                    if (camera_id == 1) {
                        if ((width1 == 0) || (height1 == 0)) {
                            width1 = 1920;
                            height1 = 1080;
                        }
                        if (PreSupFormat2 == null)
                            PreSupFormat2 = ImageFormat.NV21;
                        Log.d(TAG, "camera " + camera_id + " progressive = " + CameraHalField0);
                        Log.d(TAG, "camera " + camera_id + " PreviewSizes = " + width0 + "X" + height0);
                        Log.d(TAG, "camera " + camera_id + " PreSupFormat = " + getImageFormatString(PreSupFormat1.intValue()));
                        Log.d(TAG, "camera " + camera_id + " Input PreviewSizes = " + input_width0 + "X" + input_height0); 
                        Log.d(TAG, "camera " + camera_id + " Input PreSupFormat = " + getImageFormatString(InputCameraFormat0.intValue()));                       
                        mParameters2.setPreviewSize((int) width1, (int) height1);
                        mParameters2.set("CameraHalField", CameraHalField1);
                        mParameters1.set("CameraHalInputWidth", input_width1);
                        mParameters1.set("CameraHalInputHeight", input_height1);
                        mParameters1.set("CameraHalInputFormat", getImageFormatString(InputCameraFormat1.intValue()));
                        mParameters2.setPreviewFormat(Integer.valueOf(PreSupFormat2.intValue()));
                        mCamera[camera_id].setParameters(mParameters2);
                    }
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
                    mSurfaceView[camera_id].setZOrderOnTop(true);
                    mSurfaceView[camera_id].getHolder().setFormat(PixelFormat.TRANSPARENT);
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
                Log.d(TAG, "camera " + camera_id + " is not open");
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
    }

    @Override
    protected void onResume() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onPause();
        try {
            for (int i = 0; i < 2; i++) {
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
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
        super.onDestroy();
    }


    @Override
    //set togglebutton listener
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.togglebutton1: {
                if (isChecked) {
                    Toast.makeText(MultiCameraActivity.this, "camera 0 ON", 800).show();
                    Log.d(TAG, "ToggleButton camera 0 open");
                    new MultiOpenCameraThread(0).start();
                    mCameraTestSpinner[0].setClickable(false);
                    mCameraTestSpinner[1].setClickable(false);
                    mCameraTestSpinner[2].setClickable(false);
                    mCameraTestSpinner[6].setClickable(false);
                    mCameraTestSpinner[7].setClickable(false);
                } else {
                    Toast.makeText(MultiCameraActivity.this, "camera 0 OFF", 800).show();
                    Log.d(TAG, "ToggleButton camera 0 close");
                    new MultiCloseCameraThread(0).start();
                    mCameraTestSpinner[0].setClickable(true);
                    mCameraTestSpinner[1].setClickable(true);
                    mCameraTestSpinner[2].setClickable(true);
                    mCameraTestSpinner[6].setClickable(true);
                    mCameraTestSpinner[7].setClickable(true);
                }
            }
            break;
            case R.id.togglebutton2: {
                if (isChecked) {
                    Toast.makeText(MultiCameraActivity.this, "camera 1 ON", 800).show();
                    Log.d(TAG, "ToggleButton camera 1 open");
                    new MultiOpenCameraThread(1).start();
                    mCameraTestSpinner[3].setClickable(false);
                    mCameraTestSpinner[4].setClickable(false);
                    mCameraTestSpinner[5].setClickable(false);
                    mCameraTestSpinner[8].setClickable(false);
                    mCameraTestSpinner[9].setClickable(false);
                } else {
                    Toast.makeText(MultiCameraActivity.this, "camera 1 OFF", 800).show();
                    Log.d(TAG, "ToggleButton camera 1 close");
                    new MultiCloseCameraThread(1).start();
                    mCameraTestSpinner[3].setClickable(true);
                    mCameraTestSpinner[4].setClickable(true);
                    mCameraTestSpinner[5].setClickable(true);
                    mCameraTestSpinner[8].setClickable(true);
                    mCameraTestSpinner[9].setClickable(true);
                }
            }
            break;
            default:
                break;
        }
    }

}
