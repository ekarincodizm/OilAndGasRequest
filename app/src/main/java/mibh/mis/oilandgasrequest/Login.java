package mibh.mis.oilandgasrequest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import mibh.mis.oilandgasrequest.AllService.CallWebService;
import mibh.mis.oilandgasrequest.Data.PreferencesManager;
import mibh.mis.oilandgasrequest.Location.GLocation;


public class Login extends AppCompatActivity {

    private View focusView = null;
    private Button btnLogin;
    private View btnRegister;
    private EditText empid_lgn, idcard_lgn;
    private EditText emp_id, id_card, fname, lname, tel;
    private View loginLayout;
    private Dialog dialog;
    private AlertDialog.Builder alert;
    private PreferencesManager prefManage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        PreferencesManager.initializeInstance(Login.this);
        prefManage = PreferencesManager.getInstance();

        //startService(new Intent(this, GetLocation.class));
        new GLocation(this);

        loginLayout = findViewById(R.id.layoutLogin);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        empid_lgn = (EditText) findViewById(R.id.empid_lgn);
        idcard_lgn = (EditText) findViewById(R.id.idcard_lgn);

        empid_lgn.setText(prefManage.getValue(prefManage.EMP_ID));
        idcard_lgn.setText(prefManage.getValue(prefManage.ID_CARD));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateLogin();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(Login.this);
                dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.register);
                dialog.setCancelable(true);
                emp_id = (EditText) dialog.findViewById(R.id.emp_id);
                id_card = (EditText) dialog.findViewById(R.id.id_card);
                fname = (EditText) dialog.findViewById(R.id.fname);
                lname = (EditText) dialog.findViewById(R.id.lname);
                tel = (EditText) dialog.findViewById(R.id.tel);
                Button btnRegister = (Button) dialog.findViewById(R.id.btnRegister);
                btnRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validateRegister();
                    }
                });
                dialog.show();
            }
        });

        loginLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                return true;
            }
        });

    }

    public void validateLogin() {

        empid_lgn.setError(null);
        idcard_lgn.setError(null);

        String emp = empid_lgn.getText().toString();
        String idcard = idcard_lgn.getText().toString();

        boolean cancel = false;

        if (TextUtils.isEmpty(emp)) {
            empid_lgn.setError("กรุณาระบุรหัสพนักงาน");
            focusView = empid_lgn;
            cancel = true;
        } else if (TextUtils.isEmpty(idcard)) {
            idcard_lgn.setError("กรุณาระบุรหัสบัตรประชาชน");
            focusView = idcard_lgn;
            cancel = true;
        } else if (idcard.length() != 13) {
            idcard_lgn.setError("กรุณาระบุรหัสบัตรประชาชนให้ถูกต้อง");
            focusView = idcard_lgn;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            new LoadingLogin().execute(idcard, emp);
            /*Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);*/
        }
    }

    private class LoadingLogin extends AsyncTask<String, String, String> {

        ProgressDialog pd = new ProgressDialog(Login.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("กำลังทำการตวจสอบข้อมูล");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            if (s.equals("False")) {
                alert = new AlertDialog.Builder(Login.this);
                alert.setMessage("เข้าสูระบบผิดพลาดกรุณาลองใหม่");
                alert.setNegativeButton("ลองใหม่", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            } else if (s.equals("error")) {
                alert = new AlertDialog.Builder(Login.this);
                alert.setMessage("การเชื่อมต่อผิดพลาดกรุณาลองใหม่");
                alert.setNegativeButton("ลองใหม่", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            } else {
                convertData(s);
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected String doInBackground(String... s) {
            Log.d("TEST result login", s[0] + " " + s[1]);
            String resultLogin = new CallWebService().checkLogin(s[0], s[1]);
            Log.d("TEST result login", resultLogin);
            return resultLogin;
        }
    }

    private class LoadingRegister extends AsyncTask<String, String, String> {

        String empid, idcard, name, tel;
        ProgressDialog pd = new ProgressDialog(Login.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("กำลังทำการตวจสอบข้อมูล");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            if (s.equals("True")) {
                dialog.dismiss();
                saveShared(empid, idcard, name, tel);
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (s.equals("False")) {
                alert = new AlertDialog.Builder(Login.this);
                alert.setMessage("ระบบผิดพลาดกรุณาลองใหม่");
                alert.setNegativeButton("ลองใหม่", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });
                alert.show();
            } else if (s.equals("error")) {
                alert = new AlertDialog.Builder(Login.this);
                alert.setMessage("การเชื่อมต่อผิดพลาดกรุณาลองใหม่");
                alert.setNegativeButton("ลองใหม่", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        }

        @Override
        protected String doInBackground(String... s) {
            idcard = s[0];
            empid = s[1];
            name = s[2] + " " + s[3];
            tel = s[4];
            Log.d("TEST result regist", s[0] + " " + s[1] + " " + s[2] + " " + s[3] + " " + s[4]);
            String resultRegist = new CallWebService().saveRegister(s[0], s[1], s[2], s[3], s[4]);
            Log.d("TEST result regist", resultRegist);
            return resultRegist;
        }
    }

    private void validateRegister() {

        emp_id.setError(null);
        id_card.setError(null);
        fname.setError(null);
        lname.setError(null);
        tel.setError(null);

        String Empid = emp_id.getText().toString();
        String Idcard = id_card.getText().toString();
        String Fname = fname.getText().toString();
        String Lname = lname.getText().toString();
        String Tel = tel.getText().toString();

        boolean cancel = false;

        if (TextUtils.isEmpty(Empid)) {
            emp_id.setError("กรุณาระบุรหัสพนักงาน");
            focusView = emp_id;
            cancel = true;
        } else if (TextUtils.isEmpty(Idcard)) {
            id_card.setError("กรุณาระบุรหัสประจำตัวประชาชน");
            focusView = id_card;
            cancel = true;
        } else if (Idcard.length() != 13) {
            id_card.setError("กรุณาระบุเลขบัตรประชาชนให้ถูกต้อง");
            focusView = id_card;
            cancel = true;
        } else if (TextUtils.isEmpty(Fname)) {
            fname.setError("กรุณาระบุชื่อพนักงาน");
            focusView = fname;
            cancel = true;
        } else if (TextUtils.isEmpty(Lname)) {
            lname.setError("กรุณาระบุนามกุล");
            focusView = lname;
            cancel = true;
        } else if (TextUtils.isEmpty(Tel)) {
            tel.setError("กรุณาระบุเบอร์โทรศัพท์");
            focusView = tel;
            cancel = true;
        } else if (Tel.length() != 10) {
            tel.setError("กรุณาระบุเบอร์โทรศัพท์ให้ถูกต้อง");
            focusView = tel;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            new LoadingRegister().execute(Idcard, Empid, Fname, Lname, Tel);
        }
    }

    private void convertData(String result) {
        try {
            JSONArray Arr = new JSONArray(result);
            for (int i = 0; i < Arr.length(); i++) {
                JSONObject c = Arr.getJSONObject(i);
                saveShared(c.getString("EMP_ID"),
                        c.getString("ID_CARD"),
                        c.getString("FNAME") + " " + c.getString("LNAME"),
                        c.getString("TEL"));
            }
        } catch (Exception e) {
            Log.d("error convertresult", e.toString());
        }
    }

    private void saveShared(String empid, String idcard, String name, String tel) {
        prefManage.setValue(prefManage.EMP_ID, empid);
        prefManage.setValue(prefManage.ID_CARD, idcard);
        prefManage.setValue(prefManage.NAME, name);
        prefManage.setValue(prefManage.TEL, tel);
    }

}