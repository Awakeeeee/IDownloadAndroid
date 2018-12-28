package com.qiqiqi.idownload;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.qiqiqi.idownload.R;

public class MyNotificationManager
{
    private static final String TAG = "IDownload";

    public String NOTIFICATION_CHANNEL_QIQIQI = "channel_qi";

    private Context context;
    private int notificationId;

    public MyNotificationManager(Context _context) //使用这个mgr类是要告诉构造器context是什么,如MainActivity中构造并传入this. 好比普通类里获取一个MonoBehaviour引用一样.
    {
        context = _context;
        notificationId = 0;
        CreateNotificationChannel();
    }

    public void CreateNotificationChannel()
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) //API level 26+才有Notification channel的概念
            return;

        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int priority = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_QIQIQI, name, priority);
        channel.setDescription(description);

        NotificationManager nMgr = context.getSystemService(NotificationManager.class); //像DownloadManager一样,很多服务都有built-in系统预设,不需要开发者去接触更底层的Service
        nMgr.createNotificationChannel(channel);
    }

    public void SendSystemNotification(String title, String content)
    {
        //创建一个即用即抛通知制作器
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_QIQIQI); //要制定发出通知的context, 第二个channel参数在老的api下会被忽略
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        //用intent来定义点击通知后的行为(如转到某个activity)
        Intent intent = new Intent(context, AlertDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //intent同样可以做一些配置,具体要查文档
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0); //TODO pendingIntent是啥
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true); //点击后即删除通知

        //把消息推出去
        NotificationManagerCompat nMgrC = NotificationManagerCompat.from(context);
        nMgrC.notify(notificationId, mBuilder.build());
        notificationId++;
    }
}









