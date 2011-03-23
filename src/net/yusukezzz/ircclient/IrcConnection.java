package net.yusukezzz.ircclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

public class IrcConnection extends Service {

    private IIrcConnection.Stub binder = new IIrcConnection.Stub() {
        @Override
        public boolean addHost(String hostname, String nick, String user, String real,
                String charset) throws RemoteException {
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "IrcConnectionService has been created.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IrcConnection.class.getName().equals(intent.getAction())) {
            return binder;
        }
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(this, "IrcConnectionService has been started.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "IrcConnectionService has been terminated.", Toast.LENGTH_SHORT)
                .show();
        super.onDestroy();
    }
}
