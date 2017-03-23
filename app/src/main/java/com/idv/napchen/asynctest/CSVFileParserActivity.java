package com.idv.napchen.asynctest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Created by denny on 2017/3/21.
 */

public class CSVFileParserActivity extends AppCompatActivity {
    private ListView listView;
    private ItemArrayAdapter itemArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csvparser);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String fileName = bundle.getString("FileName");
        findViews(fileName);
    }
    private void findViews(String selectedFileName){


        listView = (ListView) findViewById(R.id.csvlistView);
        itemArrayAdapter = new ItemArrayAdapter(getApplicationContext(), R.layout.csvitem_layout);

        Parcelable state = listView.onSaveInstanceState();
        listView.setAdapter(itemArrayAdapter);
        listView.onRestoreInstanceState(state);

        // Get CSV file
//       InputStream inputStream = getResources().openRawResource(R.raw.temp);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File selectedFile = new File(dir,selectedFileName);


        InputStream is;
        try {
            is = new FileInputStream(selectedFile);
            CSVFile csvFile = new CSVFile(is);
            List<String[]> valueList = csvFile.read();

            for (String[] valueData : valueList) {
                itemArrayAdapter.add(valueData);
            }
        }catch (IOException ie){
            Toast.makeText(CSVFileParserActivity.this,"File Not Found", Toast.LENGTH_SHORT).show();
        }
        //InputStream inputStream = getResources().openRawResource(R.raw.temp);



    }
}
