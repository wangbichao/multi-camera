package com.example.tim.multicamera;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class NCameraActivity extends AppCompatActivity {

    public final String TAG = NCameraActivity.class.getSimpleName();
    private Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    private Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    private ListView listViewBasic = null;
    private String[] listViewData = new String[]{
            "2 camera","6 camera","8 camera",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        setContentView(R.layout.activity_ncamera);
        initView();
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
                ActivityCompat.requestPermissions(NCameraActivity.this, new String[]{permission}, id);
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

//permission End


    private void initView(){
        listViewBasic = (ListView)super.findViewById(R.id.listViewBasic);
        //设置listview中的内容
        listViewBasic.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,listViewData));

        //为listView对象进行监听：当点击子项目的时候触发
        listViewBasic.setOnItemClickListener(new ItemClickEvent());
    }

    //继承OnItemClickListener，当子项目被点击的时候触发
    private final class ItemClickEvent implements AdapterView.OnItemClickListener{
        @Override
        //这里需要注意的是第三个参数arg2，这是代表单击第几个选项
        public void onItemClick(AdapterView arg0, View arg1, int arg2,
                                long arg3) {
            Log.d(TAG, "position======"+ arg2 + "id======" + arg3 );
            if(arg2 == -1) {
                return;
            }
            switch (arg2) {
                case 0:
                    //Toast.makeText(getApplicationContext(), "2camera", 1).show();
                    startActivity(new Intent(NCameraActivity.this,MultiCameraActivity.class));
                    //startActivity(new Intent(NCameraActivity.this,SixCameraActivity.class));
                    break;
                case 1:
                    //Toast.makeText(getApplicationContext(), "6camera", 1).show();
                    startActivity(new Intent(NCameraActivity.this,SixCameraActivity.class));
                    //startActivity(new Intent(NCameraActivity.this,EightCameraActivity.class));
                    break;
                case 2:
                    //Toast.makeText(getApplicationContext(), "8camera", 1).show();
                    startActivity(new Intent(NCameraActivity.this,EightCameraActivity.class));
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
