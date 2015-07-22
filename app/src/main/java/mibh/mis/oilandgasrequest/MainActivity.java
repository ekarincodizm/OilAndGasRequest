package mibh.mis.oilandgasrequest;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import mibh.mis.oilandgasrequest.AllService.CallWebService;
import mibh.mis.oilandgasrequest.Data.PreferencesManager;
import mibh.mis.oilandgasrequest.Data.ResultData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnIntentScan, btnSearch;
    ImageButton btnCamera, btnSetting;
    FrameLayout btnCam;
    RelativeLayout borderColor, publicLayout;
    EditText txtSearch;
    TextView txtResult;
    MediaPlayer mp;
    ProgressBar loadingProgress;
    ResultData data;
    PreferencesManager prefManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManage = PreferencesManager.getInstance();

        btnIntentScan = (Button) findViewById(R.id.btnIntentScan);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnCam = (FrameLayout) findViewById(R.id.frameCam);
        btnSetting = (ImageButton) findViewById(R.id.btnSetting);
        borderColor = (RelativeLayout) findViewById(R.id.borderColor);
        publicLayout = (RelativeLayout) findViewById(R.id.publicLayout);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtResult = (TextView) findViewById(R.id.txtcontent);
        loadingProgress = (ProgressBar) findViewById(R.id.loadingPcontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        borderColor.setBackgroundColor(Color.RED);

        txtSearch.setText(prefManage.getValue(prefManage.FUELID));

        publicLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                return true;
            }
        });

        btnSearch.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnIntentScan.setOnClickListener(this);
        btnSetting.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        AlertDialog.Builder builderSingle;
        Intent intent;

        switch (view.getId()) {

            case R.id.btnSetting:
                builderSingle = new AlertDialog.Builder(MainActivity.this);
                builderSingle.setTitle("ตั้งค่า");
                builderSingle.setCancelable(true);
                view = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_setting, null);
                final CheckBox enSound = (CheckBox) view.findViewById(R.id.enSound);
                final CheckBox enVibrate = (CheckBox) view.findViewById(R.id.enVibrate);
                enSound.setChecked(prefManage.getBoolValue(prefManage.SOUND));
                enVibrate.setChecked(prefManage.getBoolValue(prefManage.VIBRATE));
                builderSingle.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefManage.setBoolValue(prefManage.SOUND, enSound.isChecked());
                        prefManage.setBoolValue(prefManage.VIBRATE, enVibrate.isChecked());
                        dialog.dismiss();
                    }
                });
                builderSingle.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setView(view);
                builderSingle.show();
                break;

            case R.id.btnIntentScan:
                intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.setPackage("com.google.zxing.client.android");
                intent.putExtra("SCAN_FORMATS", "CODE_39,CODE_93,CODE_128,DATA_MATRIX,ITF,CODABAR,EAN_13,EAN_8,UPC_A,QR_CODE");
                if (isCallable(intent)) {
                    startActivityForResult(intent, 0);
                } else {
                    builderSingle = new AlertDialog.Builder(MainActivity.this);
                    builderSingle.setMessage("ไม่สามารถใช้งานในส่วนนี้ได้ ต้องทำการติดตั้ง Barcode Sacnner เพื่อใช้งาน");
                    builderSingle.setCancelable(false);
                    builderSingle.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DownloadFileFromURL().execute();
                            dialog.dismiss();
                        }
                    });
                    builderSingle.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });
                    builderSingle.show();
                }
                break;

            case R.id.btnSearch:
                txtResult.setText("");
                if (prefManage.getValue(prefManage.latitude).equals("") || prefManage.getValue(prefManage.longtitude).equals("")) {
                    builderSingle = new AlertDialog.Builder(MainActivity.this);
                    builderSingle.setMessage("กรุณารอข้อมูล GPS สักครู่");
                    builderSingle.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builderSingle.show();
                } else {
                    View focusView = this.getCurrentFocus();
                    if (focusView != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    new Loading(txtSearch.getText().toString()).execute();
                }
                break;

            case R.id.btnCamera:
                if (prefManage.getValue(prefManage.latitude).equals("") || prefManage.getValue(prefManage.longtitude).equals("")) {
                    builderSingle = new AlertDialog.Builder(MainActivity.this);
                    builderSingle.setMessage("กรุณารอข้อมูล GPS สักครู่");
                    builderSingle.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builderSingle.show();
                } else if (prefManage.getValue(prefManage.FUELID).equals("")) {
                    builderSingle = new AlertDialog.Builder(MainActivity.this);
                    builderSingle.setMessage("กรุณาทำการตวจใบเบิกก่อนใช้งาน");
                    builderSingle.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builderSingle.show();
                } else {
                    intent = new Intent(MainActivity.this, ImgList.class);
                    startActivity(intent);
                }
                break;
        }
    }

    public void playSound(Context context, int resId) {
        stopIfPlaying();
        if (prefManage.getBoolValue(prefManage.SOUND)) {
            mp = MediaPlayer.create(context, resId);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
        if (prefManage.getBoolValue(prefManage.VIBRATE)) {
            Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
        }
    }

    public void stopIfPlaying() {
        try {
            if (mp != null && mp.isPlaying()) {
                mp.stop();
                mp.release();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result Barcode", resultCode + "");
        if (requestCode == 0 && resultCode == -1) {
            String contents = data.getStringExtra("SCAN_RESULT");
            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
            txtSearch.setText(contents);
            Log.d("", requestCode + " " + resultCode + " " + contents + " " + format);
            txtResult.setText("");
            new Loading(txtSearch.getText().toString()).execute();
        }
    }

    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void DeleteFile(String path, String file) {
         /*Main directory (SD card directory) */
        File sdDir = Environment.getExternalStorageDirectory();
        String sdImageMainDirectory = (sdDir.toString() + path);
        try {
            File fXmlFile = new File(sdImageMainDirectory + file);
            fXmlFile.delete();
        } catch (Exception e) {
        }
    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        Dialog dialog = new Dialog(MainActivity.this);
        ProgressBar pbDownload;
        String urlDownload = "http://www.mibholding.com/Views/Shared/AppTMS/download/BarcodeScanner-4.7.3.apk";
        String appName = "BarcodeScanner-4.7.3";
        static final int MAX_APP_FILE_SIZE = 40 * 1024;
        int downloadedSize = 0, downloadtotalSize = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_download);
            dialog.setCancelable(false);
            pbDownload = (ProgressBar) dialog.findViewById(R.id.pbDownload);
            pbDownload.setMax(100);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            try {
                URL url = new URL(urlDownload);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);
                urlConnection.connect();
                String PATH = Environment.getExternalStorageDirectory() + "/download/";
                DeleteFile(PATH, appName);
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, appName);
                FileOutputStream fileOutput = new FileOutputStream(outputFile);
                InputStream inputStream = urlConnection.getInputStream();
                downloadtotalSize = urlConnection.getContentLength();
                Log.d("Version", "File size = " + downloadtotalSize);
                byte[] buffer = new byte[MAX_APP_FILE_SIZE];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    if (pbDownload != null) {
                        int Progress = (downloadedSize * 100) / downloadtotalSize;
                        publishProgress("" + (int) Progress);
                    }
                }
                fileOutput.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("Version", "Download error: " + e.toString());
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Version", "Download error: " + e.toString());
                System.exit(0);
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pbDownload.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + appName)), "application/vnd.android.package-archive");
            startActivity(intent);
            //System.exit(0);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopIfPlaying();
    }

    private class Loading extends AsyncTask<Void, Void, String> {

        private String FUELID;

        public Loading(String FUELID) {
            this.FUELID = FUELID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            convertData(result);
            loadingProgress.setVisibility(View.GONE);
            if (result.equalsIgnoreCase("error")) {
                borderColor.setBackgroundColor(Color.RED);
                txtResult.setText(Html.fromHtml("<p align='center'><font color='RED'><big>การเชื่อมต่อผิดพลาด กรุณาลองอีกครั้ง</big></font><p>"));
            } else {
                prefManage.setValue(prefManage.FUELID, FUELID);
                if (Integer.parseInt(data.getRESULT_CODE()) == 20) {
                    borderColor.setBackgroundColor(Color.GREEN);
                } else borderColor.setBackgroundColor(Color.RED);
                txtResult.setText(Html.fromHtml(data.getRETURN_RESULT()));
                String fname = "type_" + data.getRESULT_CODE();
                int resID = getResources().getIdentifier(fname, "raw", getPackageName());
                playSound(MainActivity.this, resID);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            return new CallWebService().callForResult(FUELID, prefManage.getValue(prefManage.ID_CARD), prefManage.getValue(prefManage.NAME), String.format("%.5f,%.5f", Double.parseDouble(prefManage.getValue(prefManage.latitude)), Double.parseDouble(prefManage.getValue(prefManage.longtitude))));
        }

    }

    private void convertData(String result) {
        data = new ResultData();
        try {
            JSONArray Arr = new JSONArray(result);
            JSONObject c = Arr.getJSONObject(0);
            data.setRETURN_RESULT(c.getString("RETURN_RESULT"));
            data.setRESULT_CODE(c.getString("RESULT_CODE"));
        } catch (Exception e) {
            Log.d("error convertresult", e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //prefManage.remove(prefManage.FUELID);
        prefManage.setBoolValue(prefManage.SENTSTATION, true);
    }


}
