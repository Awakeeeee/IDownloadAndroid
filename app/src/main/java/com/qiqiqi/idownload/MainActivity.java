package com.qiqiqi.idownload;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
  getSystemService(DOWNLOAD_SERVICE) //获取下载器

  downloadID = downloadMgr.enqueue(request); //开始下载

  Cursor checker = downloadMgr.query(query); //查询下载
  int columnWholeSize = checker.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
  int columnCurrentSize = checker.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);

  IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE); //下载完成
  registerReceiver(downloadReceiver, filter);
 */

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "IDownload";

    //Uri image_uri = Uri.parse("https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/Beauty.jpg");
    //Uri data_uri = Uri.parse("https://www.w3.org/TR/PNG/iso_8859-1.txt");
    Uri data_uri = Uri.parse("https://speed.hetzner.de/100MB.bin");
    DownloadManager downloadMgr;
    long downloadId; //代表当前下载项的id

    Button download_data;
    Button btn_cancel;
    Button btn_check;

    MyNotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = new MyNotificationManager(this);
        downloadId = -1;

        download_data = findViewById(R.id.btn_download_data);
        download_data.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(downloadId == -1)
                    DownloadData(data_uri);
                else
                    Toast.makeText(MainActivity.this, "Download is already in process!", Toast.LENGTH_LONG).show();
            }
        });

        btn_cancel = (Button)findViewById(R.id.cancel_download);
        btn_cancel.setEnabled(false);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMgr.remove(downloadId);
                btn_cancel.setEnabled(false);
            }
        });

        btn_check = (Button)findViewById(R.id.check_status);
        btn_check.setEnabled(false);
        btn_check.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                OnClickCheckStatus(downloadId);
            }
        });

        //注册方法来接收下载完成的广播
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    //定义一个广播接收器BroadcastReceiver来处理下载完成的广播
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long refId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1); //在收到的Intent的Extra数据的Long类型数据中,查找名为EXTRA_DOWNLOAD_ID的这个数据
            Log.d(TAG, "download complete intent received, refID:" + refId);
            btn_cancel.setEnabled(false);

            //判断完成是否是因为下载成功
            if(IsDownloadCompleteSuccessfully(downloadId))
                notificationManager.SendSystemNotification("Your Download is Ready!", "Yes indeed, it is called Lothric");
            else
                notificationManager.SendSystemNotification("Your Download is Failed or Cancelled!", "The homeland of Lords of Cinder");
            downloadId = -1;
        }
    };

    private boolean IsDownloadCompleteSuccessfully(long downloadId)
    {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cur = downloadMgr.query(query);
        if(cur.moveToFirst())
        {
            int columnStatus = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cur.getInt(columnStatus);
            return status == DownloadManager.STATUS_SUCCESSFUL;
        }
        Toast.makeText(this, "The query id is not found: " + downloadId, Toast.LENGTH_SHORT).show();
        return false;
    }

    //进行下载的方法,可以关联在ui交互上
    private long DownloadData(Uri uri) //View先简单理解为任意UI控件的基类
    {
        downloadMgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE); //Context.getSystemService(name)获取一个系统层级服务,用string或预设名来制定,返回object,自己来cast
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Download title");
        request.setDescription("Download description");

        Log.d(TAG, "enter download data");
        Toast.makeText(this, "Begin Download Data", Toast.LENGTH_SHORT).show();
        request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "dataFile.binary"); //后两个参数是主路径+子路径
        Log.d(TAG, "download save to: " + Environment.DIRECTORY_DOWNLOADS); //这个路径Log是"Download",因为这个预设就只是一个string,实际全路径在手机上看是 内部存储设备/Android/data/com.qiqiqi.idownload/files/Download

        downloadId = downloadMgr.enqueue(request);

        //点击下载后,激活查看状态和取消的按钮
        btn_cancel.setEnabled(true);
        btn_check.setEnabled(true);

        return downloadId;
    }

    private void OnClickCheckStatus(long downloadID)
    {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);

        Cursor checker = downloadMgr.query(query); //query方法需要Query参数来知道构造哪个下载的状态数据, 返回一个Cursor,类似某种数据库查询结果的查看器,来访问状态数据
        if(checker.moveToFirst())
        {
            ShowDownloadStatus(checker);
        }else{
            Toast.makeText(MainActivity.this, "Download process of:" + downloadID + " is not found", Toast.LENGTH_SHORT).show();
        }
    }
    private void ShowDownloadStatus(Cursor checker)
    {
        //想象数据库是一个表格,row代表一条下载,column代表某个下载的各个属性如当前状态/处在这个状态的原因/储存位置...
        //上面已经把checker指定为downloadId的这个row,下面来查看各个column
        int columnStatus = checker.getColumnIndex(DownloadManager.COLUMN_STATUS); //看这个column
        int status = checker.getInt(columnStatus); //取这个column上的值
        int columnReason = checker.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = checker.getInt(columnReason);
        int columnFile = checker.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String fileName = checker.getString(columnFile);
        int columnWholeSize = checker.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
        int columnCurrentSize = checker.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);

        Log.d(TAG, Float.toString(checker.getFloat(columnCurrentSize)));
        Log.d(TAG, Float.toString(checker.getFloat(columnWholeSize)));
        float progress = checker.getFloat(columnCurrentSize) / checker.getFloat(columnWholeSize);
        Log.d(TAG, Float.toString(progress));

        String statusText = "";
        String reasonText = "";

        switch (status)
        {
            case DownloadManager.STATUS_FAILED:
                statusText = "DOWNLOAD FAILED";
                switch(reason)
                {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "UNKONWN ERROR";
                        break;
                    default:
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "DOWNLOAD PAUSED";
                switch (reason)
                {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "UNKNOWN REASON AND PAUSED";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                    default:
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "DOWNLOAD PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "DOWNLOAD RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "DOWNLOAD SUCCESSFUL";
                reasonText = "SAVED AS: " + fileName;
                break;
            default:
                break;
        }

        String percentProgress = String.format("%.2f", progress * 100f) + "%";
        Toast toast = Toast.makeText(MainActivity.this,
                "Download status:\n" + statusText + "\n\n" + "Status reason:\n" + reasonText + "\n\n" + "Progress: " + percentProgress,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();
    }
}
