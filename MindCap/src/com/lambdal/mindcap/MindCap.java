/**
  MindCap - a discreet and always on camera for Google Glass and Lambda Hat.

  This software has been modified from sample code and re-released under the
  Apache License 2.0.

  Modifications are
    by Stephen A. Balaban <s@lambdal.com>
    Copyright (c) 2013 Lambda Labs, Inc.

  Original license and code
    From The Busy Coder's Guide to Advanced Android Development
      http://commonsware.com/AdvAndroid
    Copyright (c) 2008-2012 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

 */

package com.lambdal.mindcap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import android.content.Context;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import java.lang.Runnable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class MindCap extends Activity {

private WakeLock m_wakeLock;
//  See: /mnt/sdcard/mindcap.properties
public String m_settingsFile = Environment.getExternalStorageDirectory() 
+ File.separator + "mindcap.properties";
public int m_interval = 10 * 1000; // Default (Also read during onCreate from m_settingsFile)
private Handler m_handler;
private Runnable m_photoLooper = new Runnable()
       {
            @Override 
            public void run() {
                Log.e("Cam", "Taking picture");
                takePicture();
                Log.e("Cam", "Recursive call. With delay of " + m_interval + "ms.ad");
                m_handler.postDelayed(m_photoLooper, m_interval);
            }
       };

  private SurfaceView preview=null;
  private SurfaceHolder previewHolder=null;
  private Camera camera=null;
  private boolean inPreview=false;
  private boolean cameraConfigured=false;

  public void releaseWakeLock() {
      //Release all held WakeLocks
      if (m_wakeLock != null && m_wakeLock.isHeld()) {
          m_wakeLock.release();
          m_wakeLock = null;
      }
  }
  
  // echo MindCapWakelock > /sys/power/wake_lock
  // An attempt at automating what wakelock/wakelock_ctrl.sh does.
  // This is currently not being used.
  private void setupSystemWakelock() {
	  /*
	ProcessBuilder p;
	Process pr;
	try {
		String cmd = "\"" + "echo MCWL > /sys/power/wake_lock" + "\"";
		p = new ProcessBuilder("su", "-c", cmd);
		Log.d("Command", "Command: " + cmd);
		p.start();
		pr = Runtime.getRuntime().exec("su");

	    DataOutputStream os = new DataOutputStream(pr.getOutputStream());
        os.writeBytes("echo MCWL2 > /sys/power/wake_lock"+"\n");
        os.writeBytes("exit\n");
        os.flush();
        Log.e("Lambda", "Command: " + cmd);
	} catch (IOException e) {
		e.printStackTrace();
		Log.e("Lambda", "Error", e);
	}
	*/
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // load default time from prop file.
    Properties mcprops = new Properties();
    try {
		mcprops.load(new FileInputStream(m_settingsFile));
		m_interval = Integer.parseInt(mcprops.getProperty("interval_ms"));
	    Log.e("Lambda", "m_interval <- " + m_interval + " ms.");
    } catch (FileNotFoundException e) {
		// We don't have a prop file, just use default.
	} catch (IOException e) {
		// Some error reading the file, just use default.
	}
    Log.e("Lambda", "m_interval is: " + m_interval + " ms.");
    
    // Should try to get rid of this eventually.
    preview=(SurfaceView)findViewById(R.id.preview);
    previewHolder=preview.getHolder();
    previewHolder.addCallback(surfaceCallback);
    previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    // Do it!
    m_handler = new Handler();
    
    setupSystemWakelock();
  }

  @Override
  public void onResume() {
    super.onResume();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      Camera.CameraInfo info=new Camera.CameraInfo();

      for (int i=0; i < Camera.getNumberOfCameras(); i++) {
        Camera.getCameraInfo(i, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
          camera=Camera.open(i);
        }
      }
    }

    if (camera == null) {
    	camera = Camera.open();
    }

    startPreview();
    takePicturesForever();
  }

  /*
   * If we want to use the wakeLock and capture in the background,
   * we continue to use the camera and not release the wake lock.
   */
  @Override
  public void onPause() {
      /*
    if (inPreview) {
      camera.stopPreview();
    }

    camera.release();
    camera=null;
    inPreview=false;

    releaseWakeLock();

    */
    super.onPause();
  }

  @Override
  public void onStop() {
      releaseWakeLock();
      super.onStop();
  }

  @Override
  public void onDestroy() {
      releaseWakeLock();
      super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    new MenuInflater(this).inflate(R.menu.options, menu);

    return(super.onCreateOptionsMenu(menu));
  }

  private void takePicturesForever() {
	  Log.e("Cam", "Starting to take pictures forever.");
      releaseWakeLock();
      Log.e("Cam", "Releasing wakelock.");
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      Log.e("Cam", "Setting wakelock");
      // We can also use SCREEN_DIM_WAKE_LOCK   - Cpu + dim screen
      // or              PARTIAL_WAKE_LOCK      - Cpu + no screen
      m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MindCapWakeLock");
      m_wakeLock.acquire();
      Log.e("Cam", "Acquiring wakelock");

      m_photoLooper.run();
  }

  private void takePicture() {
      if (inPreview) {
        camera.takePicture(null, null, photoCallback);
        inPreview=false;
      }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.camera) {
        //takePicturesForever();
    	// We no longer do anything as takePicturesForever()
    	// is called on startup.
    }

    return(super.onOptionsItemSelected(item));
  }

  private Camera.Size getBestPreviewSize(int width, int height,
                                         Camera.Parameters parameters) {
    Camera.Size result=null;

    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
      if (size.width <= width && size.height <= height) {
        if (result == null) {
          result=size;
        }
        else {
          int resultArea=result.width * result.height;
          int newArea=size.width * size.height;

          if (newArea > resultArea) {
            result=size;
          }
        }
      }
    }

    return(result);
  }

  private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
    Camera.Size result=null;

    for (Camera.Size size : parameters.getSupportedPictureSizes()) {
      if (result == null) {
        result=size;
      }
      else {
        int resultArea=result.width * result.height;
        int newArea=size.width * size.height;

        if (newArea < resultArea) {
          result=size;
        }
      }
    }

    return(result);
  }
  private Camera.Size getLargestPictureSize(Camera.Parameters parameters) {
	    Camera.Size result=null;

	    for (Camera.Size size : parameters.getSupportedPictureSizes()) {
	      if (result == null) {
	        result=size;
	      }
	      else {
	        int resultArea=result.width * result.height;
	        int newArea=size.width * size.height;

	        if (newArea >= resultArea) {
	          result=size;
	        }
	      }
	    }

	    return(result);
	  }

  private void initPreview(int width, int height) {
    if (camera != null && previewHolder.getSurface() != null) {
      try {
        camera.setPreviewDisplay(previewHolder);
      }
      catch (Throwable t) {
        Log.e("PreviewDemo-surfaceCallback",
              "Exception in setPreviewDisplay()", t);
        Toast.makeText(MindCap.this, t.getMessage(),
                       Toast.LENGTH_LONG).show();
      }

      if (!cameraConfigured) {
        Camera.Parameters parameters=camera.getParameters();
        Camera.Size size=getBestPreviewSize(width, height, parameters);
        Camera.Size pictureSize = getLargestPictureSize(parameters);
        int rotation = 270;
        Log.e("CameraRotation", "Setting camera rotation to : " + rotation);
        parameters.setRotation(rotation);
        if (size != null && pictureSize != null) {
          parameters.setPreviewSize(size.width, size.height);
          parameters.setPictureSize(pictureSize.width,
                                    pictureSize.height);
          parameters.setPictureFormat(ImageFormat.JPEG);
          camera.setParameters(parameters);
          cameraConfigured=true;
        }
      }
    }
  }

  /* called when the camera is initialized */
  private void startPreview() {
    if (cameraConfigured && camera != null) {
      camera.startPreview();
      inPreview=true;
    }
  }

  SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
    public void surfaceCreated(SurfaceHolder holder) {
      // no-op -- wait until surfaceChanged()
    }

    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
      initPreview(width, height);
      startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
      // no-op
    }
  };

  Camera.PictureCallback photoCallback=new Camera.PictureCallback() {
    public void onPictureTaken(byte[] data, Camera camera) {
      new SavePhotoTask().execute(data);
      camera.startPreview();
      inPreview=true;
    }
  };

  class SavePhotoTask extends AsyncTask<byte[], String, String> {
    private boolean mkdirSafe(String dirPath) {
    	try {
    		File imageDir = new File(dirPath);
    		return imageDir.mkdirs();
    	} catch (Exception e) {
    		return false;
    	}
    }
	@Override
    protected String doInBackground(byte[]... jpeg) {
      Date now = new Date();
      String dateString = now.toString().replace(' ', '-').replace(':', '_');
      String outDir = Environment.getExternalStorageDirectory() 
                    + File.separator + "MindCap" + File.separator;
      mkdirSafe(outDir); // mkdir -p $outDir
      
      String photoPath = outDir + dateString + ".jpg";
      File photo=
          new File(photoPath);

      Log.e("Cam", "Created photo at path: " + photoPath);
      if (photo.exists()) {
        photo.delete();
      }

      try {
    	photo.createNewFile();
        FileOutputStream fos=new FileOutputStream(photo.getPath());

        fos.write(jpeg[0]);
        fos.close();
      }
      catch (java.io.IOException e) {
        Log.e("MindCap", "Exception in photoCallback", e);
      }

      return(null);
    }
  }
}
