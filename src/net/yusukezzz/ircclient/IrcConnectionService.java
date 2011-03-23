package net.yusukezzz.ircclient;

import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

public class IrcConnectionService extends Service {

    private static Map<String, IrcHost> hosts = null;
    private IIrcConnectionService.Stub binder = new IIrcConnectionService.Stub() {
        public boolean addHost(String setting, String host, int port, String pass, String nick, String login,
                String real, String charset) throws RemoteException {
            hosts.put(setting, new IrcHost(setting, host, false, port, pass, nick, login, real, charset));
            return true;
        }

        public boolean connectHost(String setting) throws RemoteException {
            IrcHost host = hosts.get(setting);
            if (host == null) {
                return false;
            }
            host.connect();
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IrcConnectionService.class.getName().equals(intent.getAction())) {
            return binder;
        }
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(this, "IrcConnection has been started.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "IrcConnection has been terminated.", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
    
    class IrcConnection extends Thread {
        
    }
}
