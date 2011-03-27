package net.yusukezzz.ircclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * ホスト一覧を描画するクラス
 * @author yusuke
 */
public class HostList extends ListActivity {
    // Activity request code
    public static final int SHOW_CHANNEL = 0;
    public static final int SHOW_EDITHOST = 1;
    // Context Menu Items id
    private static final int MENU_CONNECT = Menu.FIRST;
    private static final int MENU_DISCONNECT = Menu.FIRST + 1;
    private static final int MENU_EDITHOST = Menu.FIRST + 2;
    private static final int MENU_REMOVEHOST = Menu.FIRST + 3;

    public static IrcHost currentHost = null;
    public static IrcChannel currentCh = null;
    private static ArrayList<IrcHost> hosts = null;
    public static final String HOSTS_FILE = "hosts.json";
    private JSONArray json;
    private static MyJson myjson = null;
    private HostAdapter adapter;
    private boolean mIsBind = false;

    private static IrcConnectionService localService = null;

    private ServiceConnection mServConn = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            localService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            localService = ((IrcConnectionService.LocalBinder) service).getService();
            mIsBind = true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_list);

        // host設定の読み込み
        myjson = new MyJson(getApplicationContext());
        json = myjson.readFile(HOSTS_FILE);
        int host_num = json.length();
        hosts = new ArrayList<IrcHost>();
        for (int i = 0; i < host_num; i++) {
            JSONObject jsobj;
            try {
                jsobj = json.getJSONObject(i);
                IrcHost host = this.getHostByJsobj(jsobj);
                hosts.add(host);
            } catch (JSONException e) {
                Util.d(e.getStackTrace());
            }
        }

        // アダプターにセット
        adapter = new HostAdapter(getApplicationContext(), R.layout.host_list_row, hosts);
        setListAdapter(adapter);
        // ロングタップメニュー登録
        registerForContextMenu(getListView());
        
        // IrcConnectionService開始
        if (mIsBind == false) {
            Intent intent = new Intent(this, IrcConnectionService.class);
            bindService(intent, mServConn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private IrcHost getHostByJsobj(JSONObject jsobj) throws JSONException {
        return new IrcHost(jsobj.getString("setting_name"), jsobj.getString("hostname"), jsobj.getBoolean("use_ssl"),
                jsobj.getInt("port"), jsobj.getString("pass"), jsobj.getString("nick"), jsobj.getString("login"),
                jsobj.getString("real"), jsobj.getString("charset"));
    }
    
    /**
     * Serviceを返す
     * @return IrcConnectionService
     */
    public static IrcConnectionService getLocalService() {
        return localService;
    }

    private void showChannel() {
        Intent i = new Intent(this, IrcClient.class);
        startActivityForResult(i, SHOW_CHANNEL);
    }

    /**
     * シングルタップで接続
     */
    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        super.onListItemClick(l, v, pos, id);
        IrcHost host = hosts.get(pos);
        if (host.connection().isConnected() == false) {
            host.connection().connect();
        }
        setCurrentHost(host);
        showChannel();
    }

    /**
     * ロングタップでmenu表示
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // 選択された位置を取得
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
        int pos = mi.position;
        // hostを取得
        IrcHost host = hosts.get(pos);
        // 接続状況で表示切替
        if (!host.connection().isConnected()) {
            menu.add(Menu.NONE, MENU_CONNECT, Menu.NONE, "connect");
        } else {
            menu.add(Menu.NONE, MENU_DISCONNECT, Menu.NONE, "disconnect");
        }
        menu.add(Menu.NONE, MENU_EDITHOST, Menu.NONE, "edit");
        menu.add(Menu.NONE, MENU_REMOVEHOST, Menu.NONE, "remove");
        menu.setHeaderTitle("Action");
    }

    /**
     * ロングタップメニューの動作
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        IrcHost host = hosts.get(pos);
        switch (item.getItemId()) {
            case MENU_CONNECT:
                host.connection().connect();
                setCurrentHost(host);
                showChannel();
                break;
            case MENU_DISCONNECT:
                host.connection().close();
                try {
                    hosts.set(pos, this.getHostByJsobj(json.getJSONObject(pos)));
                } catch (JSONException e) {
                    Util.d(e.getStackTrace());
                }
                updateList();
                break;
            case MENU_EDITHOST:
                Intent edit_host = new Intent(this, EditHost.class);
                edit_host.putExtra("host_no", pos);
                startActivityForResult(edit_host, SHOW_EDITHOST);
                break;
            case MENU_REMOVEHOST:
                removeHost(pos);
                updateList();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "add host");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                // EditHostへ
                Intent intent = new Intent(this, EditHost.class);
                startActivityForResult(intent, SHOW_EDITHOST);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case SHOW_CHANNEL:
                if (resCode == RESULT_OK) {
                    updateList();
                }
                break;
            case SHOW_EDITHOST:
                if (resCode == RESULT_OK) {
                    // リストの更新
                    updateList();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // ConnectionService停止
        unbindService(mServConn);
        super.onDestroy();
    }

    /**
     * 表示に使用するホストとチャンネル(最後に見ていたもの)を設定する
     * @param host
     */
    public static void setCurrentHost(IrcHost host) {
        currentHost = host;
        currentCh = currentHost.connection().getLastChannel();
    }

    /**
     * 表示に使用するチャンネルを設定する
     * @param ch
     */
    public static void setCurrentCh(IrcChannel ch) {
        currentCh = ch;
    }

    /**
     * HostListを更新する
     */
    private void updateList() {
        // adapter.notifyDataSetChanged();
        // アダプターにセット
        adapter = new HostAdapter(this, R.layout.host_list_row, hosts);
        setListAdapter(adapter);
    }

    /**
     * ホストを返す
     * @param pos
     * @return IrcHost
     */
    public static IrcHost getHost(int pos) {
        IrcHost host = null;
        try {
            host = hosts.get(pos);
        } catch (NullPointerException e) {
            Util.d(e.getStackTrace());
        }
        return host;
    }

    /**
     * ホストのリストを返す
     * @return ArrayList<IrcHost>
     */
    public static ArrayList<IrcHost> getHosts() {
        return hosts;
    }

    /**
     * ホストを追加する
     * @param host
     */
    public static void addHost(IrcHost host) {
        if (host != null) {
            hosts.add(host);
            updateJson();
        }
    }

    /**
     * hostsから設定を削除し、ファイルを更新
     * @param host_no
     */
    public static void removeHost(int host_no) {
        if (!hosts.isEmpty()) {
            try {
                IrcHost host = hosts.get(host_no);
                if (host.connection().isConnected()) {
                    host.connection().close();
                }
                // 削除
                hosts.remove(host_no);
                updateJson();
            } catch (Exception e) {
                Util.d(e.getStackTrace());
            }
        }
    }

    /**
     * 最新のhostsをファイルに保存
     */
    private static void updateJson() {
        if (!hosts.isEmpty()) {
            JSONArray json = new JSONArray();
            for (IrcHost tmp : hosts) {
                json.put(tmp.toJson());
            }
            myjson.writeFile(HOSTS_FILE, json.toString());
        }
    }

    /**
     * JSONのファイル入出力を行う
     */
    private class MyJson {
        private Context context;

        public MyJson(Context context) {
            this.context = context;
        }

        public JSONArray readFile(String filename) {
            JSONArray json = null;
            try {
                FileInputStream fis = this.context.openFileInput(filename);
                byte[] readByte = new byte[fis.available()];
                fis.read(readByte);
                json = new JSONArray(new String(readByte));
            } catch (FileNotFoundException e) {
                // ファイルがなければ空のJSON
                return json = new JSONArray();
            } catch (IOException e) {
                Util.d(e.getStackTrace());
            } catch (JSONException e) {
                Util.d(e.getStackTrace());
            }
            return json;
        }

        public boolean writeFile(String filename, String src) {
            try {
                FileOutputStream fos = this.context.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(src.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                Util.d(e.getStackTrace());
                return false;
            } catch (IOException e) {
                Util.d(e.getStackTrace());
                return false;
            }
            return true;
        }
    }

    /**
     * HostListに詰められるデータ
     */
    public class HostAdapter extends ArrayAdapter<IrcHost> {
        private List<IrcHost> hosts;
        private LayoutInflater inflater;

        public HostAdapter(Context context, int resourceId, List<IrcHost> items) {
            super(context, resourceId, items);
            hosts = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                // 1行分のviewを生成
                view = inflater.inflate(R.layout.host_list_row, null);
            }
            IrcHost host = hosts.get(position);
            TextView textView = (TextView) view.findViewById(R.id.hostlist_row_title);
            textView.setText(host.getSettingName());
            TextView connectivity = (TextView) view.findViewById(R.id.connectivity);
            // 接続状態を表示
            connectivity.setText("Disconnected");
            connectivity.setTextColor(Color.RED);
            if (host.connection() != null) {
                if (host.connection().isConnected()) {
                    connectivity.setText("Connected");
                    connectivity.setTextColor(Color.GREEN);
                }
            }
            return view;
        }
    }
}
