package net.yusukezzz.ircclient;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class HostList extends ListActivity {
    private List<IrcHost> hosts;
    // Context Menu Items id
    private static final int   MENU_CONNECT     = Menu.FIRST;
    private static final int   MENU_DISCONNECT  = Menu.FIRST + 1;
    private static final int   MENU_EDITHOST    = Menu.FIRST + 2;
    private static final int   MENU_REMOVEHOST  = Menu.FIRST + 3;
    
    private HostAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostlist);
        
        // host設定取得
        hosts = IrcClient.getHosts();
        
        // アダプターにセット
        adapter = new HostAdapter(this, R.layout.hostlist_row, hosts);
        setListAdapter(adapter);
        // ロングタップメニュー登録
        registerForContextMenu(getListView());
    }

    /**
     * シングルタップで接続
     */
    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        super.onListItemClick(l, v, pos, id);
        IrcHost host = hosts.get(pos);
        if (!host.isConnected()) {
            host.connect();
        }
        IrcClient.setCurrentHost(host);
        setResult(RESULT_OK);
        finish();
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        IrcHost host = hosts.get(pos);
        switch (item.getItemId()) {
            case MENU_CONNECT:
                host.connect();
                IrcClient.setCurrentHost(host);
                setResult(RESULT_OK);
                finish();
                break;
            case MENU_DISCONNECT:
                host.close();
                break;
            case MENU_EDITHOST:
                Intent edit_host = new Intent(this, EditHost.class);
                edit_host.putExtra("host_no", pos);
                startActivityForResult(edit_host, IrcClient.SHOW_EDITHOST);
                break;
            case MENU_REMOVEHOST:
                IrcClient.removeHost(pos);
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
                startActivityForResult(intent, IrcClient.SHOW_EDITHOST);
                break;
            default:
                break;
        }
        return true;
    }
    
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case IrcClient.SHOW_EDITHOST:
                if (resCode == RESULT_OK) {
                    // リストの更新
                    updateList();
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * HostListを更新する
     */
    private void updateList() {
        hosts = IrcClient.getHosts();
        adapter.notifyDataSetChanged();
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
