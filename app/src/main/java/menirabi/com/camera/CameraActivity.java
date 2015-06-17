package menirabi.com.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import menirabi.com.authenticator.LoginActivity;
import menirabi.com.doggydogapp.R;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    Preview preview;
    Camera camera;
    Activity act;
    Context ctx;
    Button buttonClick1;
    Button buttonClick2;
    Button buttonClick3;
    Button buttonClick6;
    String def = "defaullt";
    private int barksType = 0;
    Context context;
    private final ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };
    //some comments

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera);
        context = getBaseContext();

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);

        final ShutterCallback shutterCallback = new ShutterCallback() {
            public void onShutter() {
                Log.d(TAG, "onShutter'd");
            }
        };

        // Dog Bark Sounds
        buttonClick1 = (Button) findViewById(R.id.buttonClick1);
        buttonClick1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

//                sound.playShortResource(R.raw.dogbark);
                MediaPlayer mp;
                barksType = getSharedPreferences("DoggyDog_BGU", MODE_PRIVATE).getInt(def, 0);
                switch (barksType) {
                    case 0:
                        mp = MediaPlayer.create(getApplicationContext(), R.raw.bark1);
//                        Toast.makeText(getApplicationContext(),"this is 1", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        mp = MediaPlayer.create(getApplicationContext(), R.raw.bark2);
//                        Toast.makeText(getApplicationContext(),"this is 2", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        mp = MediaPlayer.create(getApplicationContext(), R.raw.bark3);
//                        Toast.makeText(getApplicationContext(),"this is 3", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        mp = MediaPlayer.create(getApplicationContext(), R.raw.bark1);
//                        Toast.makeText(getApplicationContext(),"this is default", Toast.LENGTH_LONG).show();
                        break;
                }
                mp.start();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.buttonClick4);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bark_array, R.layout.spinner_ddd);
        adapter.setDropDownViewResource(R.layout.spinner_ddd);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = getSharedPreferences("DoggyDog_BGU", MODE_PRIVATE);
                prefs.edit().putInt(def, position).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonClick6 = (Button) findViewById(R.id.buttonClick6);
        buttonClick6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                List<String> pList = camera.getParameters().getSupportedFlashModes();
                Camera.Parameters p =camera.getParameters();
                if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                    if (pList.contains(Camera.Parameters.FLASH_MODE_TORCH) && (android.os.Build.MANUFACTURER.contains("htc"))) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    }
                    else
                    if (pList.contains(Camera.Parameters.FLASH_MODE_ON)) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    }
                    camera.setParameters(p);
                    Toast.makeText(context,"Flash On",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context,"No flash on device",Toast.LENGTH_LONG).show();
                }
            }
        });


        buttonClick3 = (Button) findViewById(R.id.buttonClick3);
        buttonClick3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent mainIntent = new Intent(CameraActivity.this, AndroidVideoCaptureExample.class);
                CameraActivity.this.startActivity(mainIntent);
            }
        });

        buttonClick2 = (Button) findViewById(R.id.buttonClick2);
        buttonClick2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                int rotation = CameraActivity.this.getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;
                switch (rotation) {
                    case Surface.ROTATION_0: degrees = 0; break; //Natural orientation
                    case Surface.ROTATION_90: degrees = 90; break; //Landscape left
                    case Surface.ROTATION_180: degrees = 180; break;//Upside down
                    case Surface.ROTATION_270: degrees = 270; break;//Landscape right
                }
                int rotate = (info.orientation - degrees + 360) % 360;

//STEP #2: Set the 'rotation' parameter
                Camera.Parameters params = camera.getParameters();
                params.setRotation(rotate);

                List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
                int max = 0;
                int index = 0;

                for (int i = 0; i < supportedSizes.size(); i++){
                    Size s = supportedSizes.get(i);
                    int size = s.height * s.width;
                    if (size > max) {
                        index = i;
                        max = size;
                    }
                }
                Camera.Size sizePicture = (supportedSizes.get(index));
                params.setPictureSize(sizePicture.width, sizePicture.height);

                camera.setParameters(params);
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);

            }
        });


//        		buttonClick2.setOnLongClickListener(new View.OnLongClickListener(){
//        			@Override
//        			public boolean onLongClick(View arg0) {
//        				camera.autoFocus(new Camera.AutoFocusCallback(){
//        					@Override
//        					public void onAutoFocus(boolean arg0, Camera arg1) {
//        						camera.takePicture(shutterCallback, rawCallback, jpegCallback);
//        					}
//        				});
//        				return true;
//        			}
//        		});
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open(0);
                camera.startPreview();
                preview.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);

//            Drawable image = new BitmapDrawable(BitmapFactory.decodeByteArray(data, 0, data.length));
            Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
            image = Bitmap.createScaledBitmap(image,1600,1600,false);
            findViewById(R.id.previewImage).setBackground(new BitmapDrawable(getResources(),image));
            findViewById(R.id.previewImage).setVisibility(View.VISIBLE);

            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/PincturePhothos");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

    }


    private static class CustomAdapter<T> extends ArrayAdapter<String> {
        public CustomAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText("");
            return view;
        }
    }
}