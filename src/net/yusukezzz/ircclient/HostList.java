package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HostList extends ListActivity {
    public static final String HOSTS_FILE       = "hosts.json";
    private MyJson             myjson           = null;
    private JSONArray          json             = null;
    private List<IrcHost>      hosts            = null;

    // Context Menu Items id
    private static final int   MENU_CONNECT     = Menu.FIRST;
    private static final int   MENU_DISCONNECT  = Menu.FIRST + 1;
    private static final int   MENU_EDITHOST    = Menu.FIRST + 2;
    private static final int   MENU_REMOVEHOST  = Menu.FIRST + 3;

    // Activity request code
    public static final int    SHOW_EDITHOST    = 0;
    public static final int    SHOW_HOSTRECIEVE = 1;

    /**
     * 文字コードを返す
     * 
     * @return charset[]
     */
    private String getCharsets(int pos) {
        String[] charsets = getResources().getStringArray(R.array.charsets);
        return charsets[pos];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostlist);

        // host設定の取り出しと設定
        myjson = new MyJson(getApplicationContext());
        json = myjson.readFile(HOSTS_FILE);
        hosts = new ArrayList<IrcHost>();
        int host_num = json.length();
        for (int i = 0; i < host_num; i++) {
            JSONObject jsobj;
            try {
                jsobj = json.getJSONObject(i);
                hosts.add(new IrcHost(jsobj.getString("name"), jsobj.getInt("port"), jsobj
                        .getString("nick"), jsobj.getString("login"), getCharsets(jsobj
                        .getInt("charset"))));
            } catch (JSONException e) {
            }
        }
        // アダプターにセット
        HostAdapter adapter = new HostAdapter(this, R.layout.hostlist_row, hosts);
        setListAdapter(adapter);
        // ロングタップメニュー登録
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }

    /**
     * ロングタップ時のmenu表示
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
        if (!host.isConnected()) {
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
        return true;
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case SHOW_EDITHOST:
                if (resCode == RESULT_OK) {
                    // host channel
                    Intent intent = new Intent(this, IrcClient.class);
                    startActivityForResult(intent, SHOW_HOSTRECIEVE);
                }
                break;
            case SHOW_HOSTRECIEVE:
                if (resCode == RESULT_OK) {
                }
                break;
            default:
                break;
        }
    }

    /**
     * hostsから設定を削除し、ファイルを更新
     * 
     * @param host_no
     */
    private void removeHost(int host_no) {
        if (!hosts.isEmpty()) {
            try {
                // 削除
                hosts.remove(host_no);
                // 最新のhostsをファイルに保存
                JSONArray tmp = new JSONArray();
                for (int i = 0; i < hosts.size(); i++) {
                    tmp.put(hosts.get(i));
                }
                myjson.writeFile(HOSTS_FILE, tmp.toString());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * HostListに詰められるデータ
     */
    public class HostAdapter extends ArrayAdapter<IrcHost> {
        private List<IrcHost>  hosts;
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
                view = inflater.inflate(R.layout.hostlist_row, null);
            }
            IrcHost host = hosts.get(position);
            TextView textView = (TextView) view.findViewById(R.id.hostlist_row_title);
            textView.setText(host.getHostName());
            return view;
        }
    }
}
