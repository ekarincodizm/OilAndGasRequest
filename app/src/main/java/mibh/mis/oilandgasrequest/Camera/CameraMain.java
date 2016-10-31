package mibh.mis.oilandgasrequest.Camera;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import mibh.mis.oilandgasrequest.AllService.CallWebService;
import mibh.mis.oilandgasrequest.Data.PreferencesManager;
import mibh.mis.oilandgasrequest.Database.IMG_OILANDGAS;
import mibh.mis.oilandgasrequest.R;

public class CameraMain extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener, AutoFocusCallback {

    Camera mCamera;
    SurfaceView mPreview;
    Sensor mAccelerometer;
    SensorManager mSensorManager;
    PictureCallback rawCallback;
    ShutterCallback shutterCallback;
    PictureCallback jpegCallback;
    ImageView captBtn, btnComment;
    String fileName, from, modeName, comment = "";
    String FUEL_DOCID, TYPE_IMG;
    Bitmap bmpRear, bmpFront, bmp;
    Dialog dialog;
    TextView camMode;
    IMG_OILANDGAS imgMain;
    static final int FOCUS_AREA_SIZE = 300;
    int camRear = Camera.CameraInfo.CAMERA_FACING_BACK;
    int camFront = Camera.CameraInfo.CAMERA_FACING_FRONT;
    int currentCam;
    int rotat, firstCapRotate = 90;
    PreferencesManager prefManage;
    OrientationEventListener cOrientationEventListener;

    float motionX = 0;
    float motionY = 0;
    float motionZ = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_main);

        prefManage = PreferencesManager.getInstance();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mPreview = (SurfaceView) findViewById(R.id.surfaceView);
        mPreview.getHolder().addCallback(this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        captBtn = (ImageView) findViewById(R.id.captBtn);
        btnComment = (ImageView) findViewById(R.id.btnComment);
        camMode = (TextView) findViewById(R.id.camMode);

        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("FUEL_DOCID")) {
            FUEL_DOCID = extras.getString("FUEL_DOCID");
        }
        if (extras.containsKey("MODE")) {
            modeName = extras.getString("MODE");
        }
        if (extras.containsKey("From")) {
            from = extras.getString("From");
        }
        if (extras.containsKey("Type_Img")) {
            TYPE_IMG = extras.getString("Type_Img");
        }

        imgMain = new IMG_OILANDGAS(CameraMain.this);

        fileName = imgMain.Gen_imgName(FUEL_DOCID, from);
        Log.d("FileName", fileName);

        camMode.setText(modeName);
        initCapture();
        currentCam = camRear;
        captBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (currentCam == camRear) {
                        focusOnTouch(event);
                    }
                }
                return false;
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(CameraMain.this);
                alert.setTitle("ใส่ข้อความ");

                View view = CameraMain.this.getLayoutInflater().inflate(R.layout.dialog_comment, null);
                final AutoCompleteTextView input = (AutoCompleteTextView) view.findViewById(R.id.inputComment);
                input.setThreshold(1);
                input.setLines(3);
                input.setPaddingRelative(16, 0, 16, 0);
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String filter = s.toString().toLowerCase();
                        ArrayList<String> listItems = new ArrayList<>();
                        int countItems = 0;
                        for (String listItem : prefManage.getValue(prefManage.COMMENT).split("&")) {
                            if (listItem.toLowerCase().contains(filter)) {
                                if (countItems >= 3) {
                                    break;
                                }
                                listItems.add(listItem);
                                countItems++;
                            }

                        }
                        ArrayAdapter<String> adapt = new ArrayAdapter<>(CameraMain.this, android.R.layout.simple_list_item_1, listItems);
                        input.setAdapter(adapt);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                alert.setView(view);
                alert.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        comment = input.getText().toString();
                        if (prefManage.getValue(prefManage.COMMENT).equals("")) {
                            prefManage.setValue(prefManage.COMMENT, comment);
                        } else {
                            String str = comment;
                            List<String> items = Arrays.asList(prefManage.getValue(prefManage.COMMENT).split("&"));
                            if (!items.contains(comment) && !comment.equals("")) {
                                for (int x = 0, i = 0; x < items.size(); ++x, i++) {
                                    if (i >= 20) {
                                        break;
                                    }
                                    str += "&";
                                    str += items.get(x);
                                }
                                prefManage.setValue(prefManage.COMMENT, str);
                            }
                        }
                    }
                });
                alert.show();
            }
        });
    }

    private void focusOnTouch(MotionEvent event) {
        try {
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getMaxNumMeteringAreas() > 0) {

                    Rect rect = calculateFocusArea(event.getX(), event.getY());

                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                    meteringAreas.add(new Camera.Area(rect, 800));
                    parameters.setFocusAreas(meteringAreas);

                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(this);
                } else {
                    mCamera.autoFocus(this);
                }
            }
        } catch (Exception e) {
            Log.d("Error Touch Focus", e.toString());
        }

    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    public void onResume() {
        //Log.d("System", "onResume");
        super.onResume();
        mCamera = Camera.open();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (cOrientationEventListener == null) {
            cOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                public void onOrientationChanged(int orientation) {
                    if (orientation == ORIENTATION_UNKNOWN) return;
                    Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                    android.hardware.Camera.getCameraInfo(currentCam, info);
                    orientation = (orientation + 45) / 90 * 90;
                    int rotation = 0;
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        rotation = (info.orientation - orientation + 360) % 360;
                    } else {  // back-facing camera
                        rotation = (info.orientation + orientation) % 360;
                    }
                    rotat = rotation;
                }

            };
        }
        if (cOrientationEventListener.canDetectOrientation()) {
            cOrientationEventListener.enable();
        }
    }

    public void onPause() {
        Log.d("System", "onPause");
        super.onPause();
        if (imgMain != null) {
            imgMain.close();
        }
        closeCamera();
        //mCamera.release();
        mSensorManager.unregisterListener(this, mAccelerometer);

    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent event) {
        /*if (Math.abs(event.values[0] - motionX) > 1
                || Math.abs(event.values[1] - motionY) > 1
                || Math.abs(event.values[2] - motionZ) > 1) {
            Log.d("Camera System", "Refocus");
            try {
                mCamera.autoFocus(this);
            } catch (RuntimeException e) {
            }

            motionX = event.values[0];
            motionY = event.values[1];
            motionZ = event.values[2];
        }*/
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.d("CameraSystem", "surfaceChanged");
        mCamera.setDisplayOrientation(90);
        Camera.Parameters params = mCamera.getParameters();
        Camera.Parameters pm = mCamera.getParameters();
        int pheight = pm.getPictureSize().height;
        int pwidth = pm.getPictureSize().width;
        int CamViewW = 0;
        int CamViewH = 0;
        try {
                /* Get current screen size */
            int disp_width = 0;
            int disp_height = 0;
            FrameLayout CameraLayout = (FrameLayout) findViewById(R.id.frameCam);

            if (CameraLayout != null) {
                disp_width = CameraLayout.getWidth();
                disp_height = CameraLayout.getHeight();
            } else {
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = (size.x);
                int height = (size.y);
                disp_width = width;
                disp_height = height;
            }
            CamViewW = (int) ((float) disp_width);
            CamViewH = (int) ((float) disp_height * 0.8);

            if (CamViewH > disp_height) {
                CamViewH = disp_height;
                CamViewW = ((CamViewH * pwidth) / pheight);
            }

            ViewGroup.LayoutParams mp = mPreview.getLayoutParams();

            mp.width = CamViewW;
            mp.height = CamViewH;
            pm.setJpegQuality(100);
            pm.setPictureSize(640, 480);
            Log.d("Picture size Rear", pm.getPictureSize().width + " " + pm.getPictureSize().height);
            mCamera.setParameters(pm);
            mPreview.setLayoutParams(mp);
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.startPreview();
        } catch (IOException e) { /*e.printStackTrace();*/ }
    }

    public void surfaceCreated(SurfaceHolder arg0) {
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
    }

    public void onAutoFocus(boolean success, Camera camera) {
    }

    private void initCapture() {
        shutterCallback = new ShutterCallback() {
            public void onShutter() { /* No operation */
            }
        };

        rawCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) { /* No operation */
            }
        };

        jpegCallback = new PictureCallback() {
            public void onPictureTaken(final byte[] data, Camera camera) {
                System.gc();
                if (currentCam == camRear) {
                    bmpRear = captureRear(data);
                    bmpRear = RotateBitmap(bmpRear, rotat, (float) 1);
                    firstCapRotate = rotat;
                    System.gc();
                } else if (currentCam == camFront) {
                    bmpFront = captureFront(data);
                    bmpFront = RotateBitmap(bmpFront, rotat, (float) 1);
                    System.gc();
                    showPreview();
                }

            }
        };
    }

    private void showPreview() {
        System.gc();
        bmp = DrawBitmapborder(bmpRear);
        bmpRear.recycle();
        int x = bmp.getByteCount();
        Log.d("Size2", bmp.getByteCount() + "");
        if (x > 10000000) {
            bmp = RotateBitmap(bmp, 0, (float) (0.2));
        } else if (x > 4000000 && x <= 10000000) {
            bmp = RotateBitmap(bmp, 0, (float) (4000000.0 / x));
        }
        Log.d("Size3", bmp.getByteCount() + "");
        dialog = new Dialog(CameraMain.this);
        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.camera_preview);
        dialog.setCancelable(false);
        final ImageView previewImg = (ImageView) dialog.findViewById(R.id.previewPic);
        Button yes = (Button) dialog.findViewById(R.id.yes);
        Button no = (Button) dialog.findViewById(R.id.no);
        previewImg.setImageBitmap(bmp);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int imageNum = 0;
                Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "/DCIM/OILANDGAS");
                imagesFolder.mkdirs();

                File output = new File(imagesFolder, fileName);
                Log.i("PATH", output.toString());
                while (output.exists()) {
                    imageNum++;
                    fileName += "_" + String.valueOf(imageNum);
                }

                output = new File(imagesFolder, fileName);

                Uri uri = Uri.fromFile(output);
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                ContentValues image = new ContentValues();
                String dateTaken = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                image.put(MediaStore.Images.Media.TITLE, output.toString());
                image.put(MediaStore.Images.Media.DISPLAY_NAME, output.toString());
                image.put(MediaStore.Images.Media.DATE_ADDED, dateTaken);
                image.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
                image.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken);
                image.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                image.put(MediaStore.Images.Media.ORIENTATION, 90);
                String path = output.getParentFile().toString().toLowerCase();
                String name = output.getParentFile().getName().toLowerCase();
                image.put(MediaStore.Images.ImageColumns.BUCKET_ID, path.hashCode());
                image.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
                image.put(MediaStore.Images.Media.SIZE, output.length());
                image.put(MediaStore.Images.Media.DATA, output.getAbsolutePath());

                OutputStream os;

                try {
                    os = getContentResolver().openOutputStream(uri);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    Log.d("", e.toString());
                }
                //closeCamera();
                imgMain.SaveImg(FUEL_DOCID, from, TYPE_IMG, fileName, "", prefManage.getValue(prefManage.latitude), prefManage.getValue(prefManage.longtitude), comment);
                new Loading().execute();
                if (dialog != null) dialog.dismiss();
                imgMain.close();
                bmpFront.recycle();
                bmpRear.recycle();
                System.gc();
                Intent intent = new Intent();
                intent.putExtra("fileName", fileName);
                setResult(RESULT_OK, intent);
                CameraMain.this.finish();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) dialog.dismiss();
                bmpFront.recycle();
                bmpRear.recycle();
                System.gc();
                flipcamera();
            }
        });
        dialog.show();
    }

    private Bitmap captureFront(byte[] data) {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bm;
    }

    private Bitmap captureRear(byte[] data) {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        flipcamera();
        return bm;
    }

    private void capture() {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private void flipcamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (currentCam == camRear) {
            mCamera = Camera.open(camFront);
            currentCam = camFront;
        } else if (currentCam == camFront) {
            mCamera = Camera.open(camRear);
            currentCam = camRear;
        }

        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            Camera.Parameters params = mCamera.getParameters();
            Camera.Parameters pm = mCamera.getParameters();
            int pheight = pm.getPictureSize().height;
            int pwidth = pm.getPictureSize().width;
            int CamViewW = 0;
            int CamViewH = 0;
            try {
                int disp_width = 0;
                int disp_height = 0;
                FrameLayout CameraLayout = (FrameLayout) findViewById(R.id.frameCam);

                if (CameraLayout != null) {
                    disp_width = CameraLayout.getWidth();
                    disp_height = CameraLayout.getHeight();
                } else {
                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = (size.x);
                    int height = (size.y);
                    disp_width = width;
                    disp_height = height;
                }
                CamViewW = (int) ((float) disp_width);
                CamViewH = (int) ((float) disp_height * 0.8);
                if (CamViewH > disp_height) {
                    CamViewH = disp_height;
                    CamViewW = ((CamViewH * pwidth) / pheight);
                }
                ViewGroup.LayoutParams mp = mPreview.getLayoutParams();
                mp.width = CamViewW;
                mp.height = CamViewH;
                List<Camera.Size> pictureSize = pm.getSupportedPictureSizes();
                pm.setJpegQuality(100);
                //pm.setPictureSize(pm.getPictureSize().width, pm.getPictureSize().height);
                pm.setPictureSize(640, 480);
                Log.d("Picture size Front", pm.getPictureSize().width + " " + pm.getPictureSize().height);
                mCamera.setParameters(pm);
                mPreview.setLayoutParams(mp);

                mCamera.setPreviewDisplay(mPreview.getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (currentCam == camFront) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        capture();
                    }
                }, 2000);
            }

        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle, float rescale) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scaleWidth = (float) .4;
        float scaleHeight = (float) .4;
        scaleWidth = rescale;
        scaleHeight = rescale;
        System.gc();

		    /* CREATE A MATRIX FOR THE MANIPULATION */
        Matrix matrix = new Matrix();

		    /* RESIZE THE BIT MAP */
        matrix.postScale(scaleWidth, scaleHeight);
        if (angle != 0) {
            matrix.postRotate(angle);
        }
            /* "RECREATE" THE NEW BITMAP */
        return Bitmap.createBitmap(source, 0, 0, width, height, matrix, false);
    }

    private Bitmap DrawBitmapborder(Bitmap src) {

        System.gc();
        Bitmap workingBitmap = Bitmap.createBitmap(src);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);  // Bitmap.createBitmap(workingBitmap);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int rw = mutableBitmap.getWidth();
        int rh = mutableBitmap.getHeight();
        if (firstCapRotate == 0 || firstCapRotate == 180) {
            rw = mutableBitmap.getHeight();
            rh = mutableBitmap.getWidth();
        }
        int w = mutableBitmap.getWidth();
        int h = mutableBitmap.getHeight();

        workingBitmap.recycle();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = date.format(c.getTime());
        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.US);
        String formattedTime = time.format(c.getTime());

        int globalOffsetR = (int) (w * 0.98);
        int globalOffsetL = (int) (w * 0.02);

        Paint paintPic = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintPic.setAlpha(200);
        Rect destinationRect = new Rect();
        destinationRect.set(0, 0, (int) (w * 0.2), (int) (h * 0.2));
        destinationRect.offsetTo((int) (w * 0.02), ((int) (h * 0.98) - (int) (h * 0.2)));
        canvas.drawBitmap(bmpFront, null, destinationRect, paintPic);
        bmpFront.recycle();

        int clr = Color.parseColor("#FFFFFF");

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(clr);
        paint.setTextSize((int) (rh * 0.04));
        paint.setShadowLayer((float) 0.02, 1, 1, Color.BLACK);
        canvas.drawText(formattedDate, globalOffsetR, (int) (h * 0.06), paint);

        paint.setTextSize((int) (rh * 0.04));
        canvas.drawText(formattedTime, globalOffsetR, (int) (h * 0.10), paint);

        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText(modeName, globalOffsetR, (int) (h * 0.15), paint);

        int x = globalOffsetR, y = (int) (h * 0.16);

        int Start = 0, End = 0;
        String stationName = prefManage.getValue(prefManage.STATIONNAME);
        for (int i = 0; i <= stationName.length(); ++i) {
            if (i % 22 == 0 || i == stationName.length()) {
                canvas.drawText(stationName.substring(Start, i), x, y, paint);
                if (i != stationName.length()) {
                    y += -paint.ascent() + paint.descent();
                }
                Start = i;
            }
        }
        //y += -paint.ascent() + paint.descent();
        y += (int) (h * 0.01);
        if (comment != null && !comment.equalsIgnoreCase("")) {
            paint.setTextSize((int) (rh * 0.03));
            if (comment.contains("\n")) {
                y += -paint.ascent() + paint.descent();
                for (String line : comment.split("\n")) {
                    canvas.drawText(line, x, y, paint);
                    y += -paint.ascent() + paint.descent();
                }
            } else {
                Start = 0;
                End = 0;
                for (int i = 0; i <= comment.length(); ++i) {
                    if (i % 22 == 0 || i == comment.length()) {
                        canvas.drawText(comment.substring(Start, i), x, y, paint);
                        y += -paint.ascent() + paint.descent();
                        Start = i;
                    }
                }
            }
        }

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText(FUEL_DOCID, globalOffsetL, (int) (h * 0.05), paint);

        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText(prefManage.getValue(prefManage.NAME), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.80), paint);

        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText(prefManage.getValue(prefManage.EMP_ID), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.84), paint);

        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText(prefManage.getValue(prefManage.TEL), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.88), paint);

        paint.setTextSize((int) (rh * 0.025));
        canvas.drawText(String.format("GPS: %.5f,%.5f", Double.parseDouble(prefManage.getValue(prefManage.latitude)), Double.parseDouble(prefManage.getValue(prefManage.longtitude))), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.935), paint);

        paint.setTextSize((int) (rh * 0.025));
        canvas.drawText(prefManage.getValue(prefManage.locationname), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.97), paint);

        /* Paint edge */
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((int) (h * 0.02));
        paint.setShadowLayer((float) 0.00, 0, 0, Color.BLACK);
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, w, h, paint);

        System.gc();
        return mutableBitmap;
    }

    private class Loading extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String[] params) {
            String locatName;
            if (prefManage.getValue(prefManage.locationname).length() > 200) {
                locatName = prefManage.getValue(prefManage.locationname).substring(0, 198);
            } else locatName = prefManage.getValue(prefManage.locationname);
            Log.d("test DoInbackGround", "TEST");
            /*if (comment.length() >= 296) {
                comment = comment.substring(0, s295) + "..";
            }*/

            Log.d("savestate", prefManage.getValue(prefManage.FUELID) + " " +
                    String.format("%.5f,%.5f", Double.parseDouble(prefManage.getValue(prefManage.latitude)), Double.parseDouble(prefManage.getValue(prefManage.longtitude))) + " " +
                    locatName + " " +
                    TYPE_IMG + " " +
                    prefManage.getValue(prefManage.EMP_ID) + " " +
                    prefManage.getValue(prefManage.NAME) + " " +
                    fileName + " " +
                    comment);
            String result = new CallWebService().saveStateFuel(
                    prefManage.getValue(prefManage.FUELID),
                    String.format("%.5f,%.5f", Double.parseDouble(prefManage.getValue(prefManage.latitude)), Double.parseDouble(prefManage.getValue(prefManage.longtitude))),
                    locatName,
                    TYPE_IMG,
                    prefManage.getValue(prefManage.EMP_ID),
                    prefManage.getValue(prefManage.NAME),
                    fileName,
                    comment);
            Log.d("Result savestate", result);
            return null;
        }
    }

}
