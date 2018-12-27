package com.qiqiqi.idownload;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "IDownload";

    Uri image_uri = Uri.parse("https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/Beauty.jpg");
    Uri text_uri = Uri.parse("https://www.w3.org/TR/PNG/iso_8859-1.txt");
    DownloadManager downloadMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button download_data = findViewById(R.id.btn_download_data);
        download_data.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DownloadData(text_uri, view);
            }
        });

        Button download_img = findViewById(R.id.btn_download_img);
        download_img.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DownloadData(image_uri, view);
            }
        });
    }

    //进行下载的方法,可以关联在ui交互上
    private long DownloadData(Uri uri, View view) //View先简单理解为任意UI控件的基类
    {
        long downloadID;
        downloadMgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE); //Context.getSystemService(name)获取一个系统层级服务,用string或预设名来制定,返回object,自己来cast
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Download title");
        request.setDescription("Download description");

        if(view.getId() == R.id.btn_download_data) //R是自动生成的表示工程资源的类
        {
            Log.d(TAG, "enter download data");
            Toast.makeText(this, "Begin Download Data", Toast.LENGTH_LONG);
            request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "dataFile.txt"); //后两个参数是主路径+子路径
        }else if(view.getId() == R.id.btn_download_img)
        {
            Log.d(TAG, "enter download img");
            Toast.makeText(this, "Begin Download Image", Toast.LENGTH_LONG);
            request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "imgFile.png");
        }
        Log.d(TAG, "download save to: " + Environment.DIRECTORY_DOWNLOADS); //这个路径Log是"Download",因为这个预设就只是一个string,实际全路径在手机上看是 内部存储设备/Android/data/com.qiqiqi.idownload/files/Download

        //点击下载后,激活这俩按钮,使用查看进度和取消的功能
        Button btn_cancel = (Button)findViewById(R.id.cancel_download);
        btn_cancel.setEnabled(true);
        Button btn_check = (Button)findViewById(R.id.check_status);
        btn_check.setEnabled(true);

        downloadID = downloadMgr.enqueue(request);
        return downloadID;
    }
}
