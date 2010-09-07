package net.yusukezzz.ircclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

public class EditHost extends Activity {
    // add host view
    private EditText  hostname;
    private EditText  port;
    private EditText  nick;
    private EditText  login;
    private Spinner   charspn;
    private long      charset;
    private Button    edithostbtn;
    private int       host_no = -1;

    private MyJson    myjson  = null;
    private JSONArray json    = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edithost);

        // host設定読み込み
        this.myjson = new MyJson(getApplicationContext());
        this.json = myjson.readFile(IrcClient.HOSTS_FILE);

        // 要素の用意
        this.hostname = (EditText) this.findViewById(R.id.edithost_hostname);
        this.port = (EditText) this.findViewById(R.id.edithost_port);
        this.nick = (EditText) this.findViewById(R.id.edithost_nick);
        this.login = (EditText) this.findViewById(R.id.edithost_login);
        charspn = (Spinner) this.findViewById(R.id.edithost_charset);
        this.edithostbtn = (Button) this.findViewById(R.id.edithost_btn);

        Intent i = getIntent();
        host_no = i.getIntExtra("host_no", -1);
        if (host_no != -1 && !json.isNull(host_no)) {
            try {
                JSONObject host = json.getJSONObject(host_no);
                hostname.setText(host.get("name").toString(), BufferType.NORMAL);
                port.setText(host.get("port").toString(), BufferType.NORMAL);
                nick.setText(host.get("nick").toString(), BufferType.NORMAL);
                login.setText(host.get("login").toString(), BufferType.NORMAL);
                charspn.setSelection(host.getInt("charset"));
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
                    charset = charspn.getSelectedItemId();
                    new_host.put("charset", charset);
                    if (host_no != -1) {
                        json.put(host_no, new_host);
                    } else {
                        json.put(new_host);
                    }
                    // 書き込み
                    myjson.writeFile(IrcClient.HOSTS_FILE, json.toString());
                } catch (JSONException e) {
                    Log.e("yusukezzz", e.getMessage().toString());
                }
                myjson = null;
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
