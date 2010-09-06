package net.yusukezzz.ircclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

public class EditHost extends Activity {
    // add host view
    private EditText hostname;
    private EditText port;
    private EditText nick;
    private EditText login;
    private String charset;
    private Button edithostbtn;

    private MyJson myjson;
    private JSONArray json;
    
    public EditHost() {
        this.myjson = new MyJson(this);
        // 読み込み
        this.json = myjson.readFile(IrcClient.HOSTS_FILE);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edithost);
        
        // 要素の用意
        this.hostname = (EditText) this.findViewById(R.id.edithost_hostname);
        this.port = (EditText) this.findViewById(R.id.edithost_port);
        this.nick = (EditText) this.findViewById(R.id.edithost_nick);
        this.login = (EditText) this.findViewById(R.id.edithost_login);
        Spinner charspn = (Spinner) this.findViewById(R.id.edithost_charset);
        this.charset = charspn.getSelectedItem().toString();
        this.edithostbtn = (Button) this.findViewById(R.id.edithost_btn);
        
        Intent i = getIntent();
        int host_no = i.getIntExtra("host_no", -1);
        if (host_no != -1) {
            try {
                JSONObject host = json.getJSONObject(host_no);
                hostname.setText(host.get("name").toString(), BufferType.NORMAL);
            } catch (JSONException e) {
            }
        }
        
        edithostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ホストを保存する
                // TODO: 入力チェック
                try {
                    // 追加
                    JSONObject new_host = new JSONObject();
                    new_host.put("name", hostname.getText());
                    new_host.put("port", port.getText());
                    new_host.put("nick", nick.getText());
                    new_host.put("login", login.getText());
                    new_host.put("charset", charset);
                    json.put(new_host);
                    // 書き込み
                    myjson.writeFile(IrcClient.HOSTS_FILE, json.toString());
                } catch (JSONException e) {
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
