package net.yusukezzz.ircclient;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

public class AddHost extends Activity {
    // add host view
    private String                   hostname;
    private String                  port;
    private String                   nick;
    private String                   login;
    private String                   charset;
    private Button                   addhostbtn;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addhost);
        
        // 要素の用意
        this.hostname = this.findViewById(R.id.addhost_hostname).toString();
        this.port = this.findViewById(R.id.addhost_port).toString();//Integer.parseInt();
        this.nick = this.findViewById(R.id.addhost_nick).toString();
        this.login = this.findViewById(R.id.addhost_login).toString();
        Spinner charspn = (Spinner) this.findViewById(R.id.addhost_charset);
        this.charset = charspn.getSelectedItem().toString();
        this.addhostbtn = (Button) this.findViewById(R.id.addhost_addbtn);
        // 追加ボタンにイベントをセット
        addhostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ホストの追加
                // TODO: 入力チェック
                Intent intent = new Intent(AddHost.this, HostList.class);
                String[] result = {hostname, port, nick, login, charset};
                intent.putExtra("HOST", result);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
