package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HostList extends ListActivity {
    private MyJson    myjson = null;
    private JSONArray json   = null;
    private List<IrcHost> hosts = null;
    
    /**
     * 文字コードの配列を返す
     * @return charset[]
     */
    private String[] getCharsets() {
        return getResources().getStringArray(R.array.charsets);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostlist);

        // host設定の取り出しと設定
        myjson = new MyJson(getApplicationContext());
        json = myjson.readFile(IrcClient.HOSTS_FILE);
        hosts = new ArrayList<IrcHost>();
        int host_num = json.length();
        for (int i = 0; i < host_num; i++) {
            JSONObject jsobj;
            try {
                jsobj = json.getJSONObject(i);
                hosts.add(new IrcHost(jsobj.getString("name"),
                        jsobj.getInt("port"),
                        jsobj.getString("nick"),
                        jsobj.getString("login"),
                        getCharsets()[jsobj.getInt("charset")]));
            } catch (JSONException e) {
            }
        }
        // アダプターにセット
        HostAdapter adapter = new HostAdapter(this, R.layout.hostlist_row, hosts);
        setListAdapter(adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // 接続、編集、削除ダイアログ表示
    }

    public class HostAdapter extends ArrayAdapter<IrcHost> {
        private List<IrcHost> items;
        private LayoutInflater     inflater;

        public HostAdapter(Context context, int resourceId, List<IrcHost> items) {
            super(context, resourceId, items);
            this.items = items;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                // 1行分のviewを生成
                view = inflater.inflate(R.layout.hostlist_row, null);
            }
            IrcHost host = items.get(position);
            TextView textView = (TextView) view.findViewById(R.id.hostlist_row_title);
            textView.setText(host.getHostName());
            // TODO: 接続、編集、削除などのボタン用意
            return view;
        }
    }
}
