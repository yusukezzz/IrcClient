package net.yusukezzz.ircclient;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Paint.Join;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class HostList extends ListActivity {
    private ListView hostlist;
    private Button newhostbtn;
    private MyJson myjson = null;
    private JSONArray json = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostlist);
        
        myjson = new MyJson(getApplicationContext());
        json = myjson.readFile(IrcClient.HOSTS_FILE);
        
        hostlist = (ListView) this.findViewById(R.id.hostlist);
        // TODO: リストに要素追加
        newhostbtn = (Button) this.findViewById(R.id.newhostbtn);
        // ホスト追加画面に遷移
        newhostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: ホスト追加画面へ
            }
        });
    }
    
    public class HostAdapter extends ArrayAdapter<JSONObject> {
        private List<JSONObject> items;
        private LayoutInflater inflater;
        public HostAdapter(Context context, int resourceId, List<JSONObject> items) {
            super(context, resourceId, items);
            this.items = items;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }
}
