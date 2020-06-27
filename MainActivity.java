package com.imran.smssendandreceive;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public Button btnSend, btnRefresh;
    private EditText etPhone, etMsg;
    private final static int REQUEST_CODE_PERMISSION_SEND_SMS = 123;

    private ListView lvSMS;
    private final static int REQUEST_CODE_PERMISSION_READ_SMS = 456;
    ArrayList<String> smsMsgList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    public static MainActivity instance;

    public static  MainActivity Instance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        btnRefresh = findViewById(R.id.btn_refresh);
        btnSend = findViewById(R.id.btnSend);
        etMsg = findViewById(R.id.etMsg);
        etPhone = findViewById(R.id.etPhone);

        btnSend.setEnabled(false);

        //Setting ListView for Message
        lvSMS = findViewById(R.id.lv_sms);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, smsMsgList);
        lvSMS.setAdapter(arrayAdapter);

        //one part
        if(checkPermission(Manifest.permission.READ_SMS)){
            refreshInbox();
        }else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    (Manifest.permission.READ_SMS)}, REQUEST_CODE_PERMISSION_READ_SMS);
        }

        //One Part
        if(checkPermission(Manifest.permission.SEND_SMS)){
            btnSend.setEnabled(true);
        }else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    (Manifest.permission.SEND_SMS)}, REQUEST_CODE_PERMISSION_SEND_SMS);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etMsg.getText().toString();
                String phoneNumber = etPhone.getText().toString();

                SmsManager smsMan = SmsManager.getDefault();
                smsMan.sendTextMessage(phoneNumber, null, msg, null, null);
                Toast.makeText(MainActivity.this, "Send SMS", Toast.LENGTH_LONG).show();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshInbox();
            }
        });
    }

    private boolean checkPermission(String permission){
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_SEND_SMS:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }


    public void refreshInbox(){
        ContentResolver cResolver = getContentResolver();
        Cursor smsIndexCursor = cResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);

        int indexBody = smsIndexCursor.getColumnIndex("body");
        int indexAddress = smsIndexCursor.getColumnIndex("address");

        if(indexBody < 0 || !smsIndexCursor.moveToFirst()) return;
        arrayAdapter.clear();

        do{
            String str = "From: " + smsIndexCursor.getString(indexAddress) + "\n";
            str += "Message: " + smsIndexCursor.getString(indexBody);
            arrayAdapter.add(str);
        }while (smsIndexCursor.moveToNext());
    }

    public void updateList(final String smsMsg){
        arrayAdapter.insert(smsMsg, 0);
        arrayAdapter.notifyDataSetChanged();
    }
}
