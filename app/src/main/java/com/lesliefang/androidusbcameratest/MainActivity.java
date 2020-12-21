package com.lesliefang.androidusbcameratest;

import android.Manifest;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.serenegiant.usb.widget.CameraViewInterface;

public class MainActivity extends AppCompatActivity {
    CameraViewInterface mUVCCameraView;
    TextureView mTextureView;
    UVCCameraHelper mCameraHelper;
    boolean isPreview;
    boolean isRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = findViewById(R.id.textureview);
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(mCallback);
        mCameraHelper = UVCCameraHelper.getInstance();
        // set default preview size
        mCameraHelper.setDefaultPreviewSize(1280, 720);
        // set default frame format，defalut is UVCCameraHelper.Frame_FORMAT_MPEG
        // if using mpeg can not record mp4,please try yuv
        // mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, mDevConnectListener);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraHelper.registerUSB();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraHelper.unregisterUSB();
    }

    private CameraViewInterface.Callback mCallback = new CameraViewInterface.Callback() {
        @Override
        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
            // must have
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView);
                isPreview = true;
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
            // must have
            if (isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.stopPreview();
                isPreview = false;
            }
        }
    };
    private UVCCameraHelper.OnMyDevConnectListener mDevConnectListener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission(must have)
            Toast.makeText(MainActivity.this, device.getDeviceName() + " attach", Toast.LENGTH_SHORT).show();
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera(must have)
            Toast.makeText(MainActivity.this, device.getDeviceName() + " detach", Toast.LENGTH_SHORT).show();
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            Toast.makeText(MainActivity.this, device.getDeviceName() + " onConnect", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            Toast.makeText(MainActivity.this, device.getDeviceName() + " onDisConnect", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraHelper.closeCamera();
        mCameraHelper.release();
    }
}