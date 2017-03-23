package com.idv.napchen.asynctest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by napchen on 2017/3/15.
 */

public class ExportActivity extends AppCompatActivity {

    private TextView tvTest;
    private Button btnSave,toFileList;
    private EditText etFileName;
    private final static String TAG = "ExportActivity";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private SharedPreferences sharedPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savecsvfile);




        findViews();
    }

    private void findViews(){
        etFileName = (EditText)findViewById(R.id.etFileName);

        // TEST
        tvTest = (TextView)findViewById(R.id.prefTest);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String dbIp = sharedPref.getString("mainIPAddress", null);

        tvTest.setText(dbIp);

        toFileList = (Button)findViewById(R.id.gotoFileList);
        toFileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExportActivity.this, CSVFileListActivity.class);
                startActivity(intent);
            }
        });

        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = etFileName.getText().toString();
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                saveFile(dir, fileName + ".csv");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }
    private void saveFile(File dir, String fileName){
        if (!isStorageMounted()) {
            Toast.makeText(this, getString(R.string.msg_ExternalStorageNotFound), Toast.LENGTH_SHORT).show();
            return;
        }
        // Init BufferedWritter and FileOutputStream
        BufferedWriter bw = null;
        FileOutputStream fop = null;
        String content = "No,ID,Temperature,Humidity,Time\n" +
                "1,6511,23.4,78.9,18:29:05\n" +
                "2,6511,23.4,78.9,18:29:08\n" +
                "3,6511,23.4,78.9,18:29:11\n" +
                "4,6511,23.4,78.9,18:29:14\n" +
                "5,6511,23.4,78.9,18:29:17";
        try {
            // 若此資料夾不存在
            if (!dir.exists()) {
                // 若此資料夾沒被建立
                if (!dir.mkdirs()) {
                    Toast.makeText(this, getString(R.string.msg_DirectoryNotCreated), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File file = new File(dir, fileName);
            fop = new FileOutputStream(file);
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        }catch (IOException ie){
            Log.e(TAG,ie.toString());
        }finally {
            try {
                if (fop != null) {
                    fop.close();
                }
                if (bw != null){
                    bw.close();
                }
            }catch (IOException ie){
                Log.e(TAG,ie.toString());
            }
        }
        Toast.makeText(ExportActivity.this, fileName + " saved", Toast.LENGTH_SHORT).show();
    }


    private boolean isStorageMounted() {
        String result = Environment.getExternalStorageState();
        // MEDIA_MOUNTED代表可對外部媒體進行存取
        return result.equals(Environment.MEDIA_MOUNTED);
    }
}