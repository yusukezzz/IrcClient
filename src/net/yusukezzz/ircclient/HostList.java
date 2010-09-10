package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HostList extends ListActivity {
    private ListView  hostlist;
    private Button    newhostbtn;
    private MyJson    myjson = null;
    private JSONArray json   = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostlist);
        
        myjson = new MyJson(getApplicationContext());
        json = myjson.readFile(IrcClient.HOSTS_FILE);
        List<HostListItem> items = new ArrayList<HostListItem>();
        int host_num = json.length();
        Log.e("IRC", "host num " + host_num);
        for (int i = 0; i < host_num; i++) {
            JSONObject jsobj;
            try {
                jsobj = json.getJSONObject(i);
                items.add(new HostListItem(jsobj.getString("name")));
            } catch (JSONException e) {
            }
        }
        HostAdapter adapter = new HostAdapter(this, R.layout.hostlist_row,
                items);
        ListView hostListView = (ListView) this.findViewById(R.id.list);
        if (hostListView == null) {
            Log.e("IRC", "null");
        }
        hostListView.setAdapter(adapter);
    }

    public class HostAdapter extends ArrayAdapter<HostListItem> {
        private List<HostListItem> items;
        private LayoutInflater     inflater;

        public HostAdapter(Context context, int resourceId,
                List<HostListItem> items) {
            super(context, resourceId, items);
            this.items = items;
            this.inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                // 1行分のviewを生成
                view = inflater.inflate(R.layout.hostlist_row, null);
            }
            HostListItem item = items.get(position);
            TextView textView = (TextView) view
                    .findViewById(R.id.hostlist_row_title);
            textView.setText(item.getTitle());
            // TODO: 接続、編集、削除などのボタン用意
            return view;
        }
    }
}
