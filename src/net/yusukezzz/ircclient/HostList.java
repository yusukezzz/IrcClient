package net.yusukezzz.ircclient;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class HostList extends ListActivity {
    private ListView hostlist;
    private Button newhostbtn;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostlist);
        
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
}
