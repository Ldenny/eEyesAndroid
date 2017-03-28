package com.idv.napchen.asynctest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by denny on 2017/3/18.
 */

public class CSVFileListActivity extends AppCompatActivity {
    private static ListView lvCSV;
    private static List<String> csvFileList;
    private static CSVAdapter csvAdapter;
    private final static String TAG = "CSVFileListActivity";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static File selectedFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csvfilelist);
        findViews();
    }
    private void findViews(){
        lvCSV = (ListView)findViewById(R.id.lvCSV);
        csvAdapter = new CSVAdapter();
        loadDocument();
        lvCSV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = (String)parent.getItemAtPosition(position);
                Toast.makeText(CSVFileListActivity.this,fileName, Toast.LENGTH_SHORT).show();


                // bundle file name
                Intent intent = new Intent(CSVFileListActivity.this, CSVFileParserActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("FileName",fileName);
                intent.putExtras(bundle);
                // Turn to next page
                startActivity(intent);
            }
        });
        lvCSV.setAdapter(csvAdapter);
        lvCSV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = (String)parent.getItemAtPosition(position);
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                selectedFile = new File(dir,fileName);

                // Show Alert
                AlertFragment alertFragment = new AlertFragment();
                FragmentManager fm = getSupportFragmentManager();
                alertFragment.show(fm, "alert");

//                if (file.delete()){
//                    loadDocument();
//                    csvAdapter.notifyDataSetChanged();
//                    return true;
//                }else {
//                    return false;
//                }
                    loadDocument();
                    csvAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }
    private boolean isStorageMounted() {
        String result = Environment.getExternalStorageState();
        // MEDIA_MOUNTED代表可對外部媒體進行存取
        return result.equals(Environment.MEDIA_MOUNTED);
    }
    private class CSVAdapter extends BaseAdapter{
        private LayoutInflater layoutInflater;
        public  CSVAdapter(){
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return csvFileList.size();
        }

        @Override
        public Object getItem(int position) {
            return csvFileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_csv,parent,false);
                holder.tvCSV = (TextView)convertView.findViewById(R.id.tvCSVFileName);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }
            String csvFileName = csvFileList.get(position);
            holder.tvCSV.setText(csvFileName);
            return convertView;
        }

    }
    // Load Files in the Documents
    private static void loadDocument(){
        //  Get the path of documents
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        Log.e(TAG,documentsDir.toString());
        String dirContents[] = documentsDir.list();
        Log.e(TAG,dirContents.toString());
        // Check the document if it is empty
        if (!documentsDir.isDirectory())
            Log.e(TAG,"Not a directory");
        else if (dirContents.length == 0)
            Log.d(TAG,"Is empty");
        csvFileList = new ArrayList<>();
        for (int i = 0; i < dirContents.length; i++){
            csvFileList.add(dirContents[i]);
        }

    }
    private class ViewHolder{
        TextView tvCSV;
    }

    // Alert to make sure if user really want to delete files
    public static class AlertFragment extends DialogFragment implements DialogInterface.OnClickListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.alert_title)
                    //設定圖示
                    .setIcon(R.drawable.warning)
                    //設定訊息內容
                    .setMessage(getString(R.string.delete_alert)+selectedFile.getName().toString()+"?")
                    //設定確認鍵 (positive用於確認)
                    .setPositiveButton(R.string.text_btYes, this)
                    //設定取消鍵 (negative用於取消)
                    .setNegativeButton(R.string.text_btNo, this)
                    .create();
            return alertDialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    selectedFile.delete();
                    loadDocument();
                    csvAdapter.notifyDataSetChanged();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
                default:
                    break;
            }
        }
    }
}
