package com.timefleeting.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class RecoverNotification extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(SystemUtils.isAppAlive(context, "com.timefleeting.app")){
            Log.d("TimeFleeting", "the app process is alive");
            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent editIntent = new Intent(context, EditPastActivity.class);
            editIntent.putExtra("isOld", true);
            editIntent.putExtra("Title", intent.getStringExtra("TITLE"));
            editIntent.putExtra("Content", intent.getStringExtra("CONTENT"));
            editIntent.putExtra("RemindTime", intent.getStringExtra("REMINDTIME"));
            editIntent.putExtra("Type", intent.getStringExtra("Type"));
//			context.startActivityForResult(intent, 2);

            Intent[] intents = {mainIntent, editIntent};

            context.startActivities(intents);
        }else {
            //���app�����Ѿ���ɱ��������������app����DetailActivity��������������Intent�У���������
            //SplashActivity����MainActivity����ʱapp�ĳ�ʼ���Ѿ���ɣ���MainActivity�оͿ��Ը��ݴ���             //������ת��DetailActivity��ȥ��
            Log.i("TimeFleeting", "the app process is dead");
            Intent launchIntent = context.getPackageManager().
                    getLaunchIntentForPackage("com.timefleeting.app");
            launchIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Bundle args = new Bundle();
            args.putBoolean("isOld", true);
            args.putString("Title", intent.getStringExtra("TITLE"));
            args.putString("Content", intent.getStringExtra("CONTENT"));
            args.putString("RemindTime", intent.getStringExtra("REMINDTIME"));
            args.putString("Type", intent.getStringExtra("Type"));
            launchIntent.putExtra("BUNDLE", args);
            context.startActivity(launchIntent);
        }
	}

}