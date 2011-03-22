package net.yusukezzz.ircclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class IrcConnectionService extends Service {
    @Override
    public void onCreate() {
        Toast.makeText(this, "IrcConnectionService has been created.", Toast.LENGTH_SHORT).show();
        Util.d("create");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "IrcConnectionService has been started.", Toast.LENGTH_SHORT).show();
        Util.d("start");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "IrcConnectionService has been terminated.", Toast.LENGTH_SHORT)
                .show();
        Util.d("end");
        super.onDestroy();
    }
}
