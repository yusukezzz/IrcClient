package net.yusukezzz.ircclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class AddHost extends Activity {
    private String HOSTS_FILE = "hosts.json";
    // add host view
    private EditText hostname;
    private EditText port;
    private EditText nick;
    private EditText login;
    private String charset;
    private Button addhostbtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addhost);

        // 要素の用意
        this.hostname = (EditText) this.findViewById(R.id.addhost_hostname);
        this.port = (EditText) this.findViewById(R.id.addhost_port);
        this.nick = (EditText) this.findViewById(R.id.addhost_nick);
        this.login = (EditText) this.findViewById(R.id.addhost_login);
        Spinner charspn = (Spinner) this.findViewById(R.id.addhost_charset);
        this.charset = charspn.getSelectedItem().toString();
        this.addhostbtn = (Button) this.findViewById(R.id.addhost_addbtn);
        // 追加ボタンにイベントをセット
        addhostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ホストを保存する
                // TODO: 入力チェック
                try {
                    // 読み込み
                    JSONArray json = readJSON(HOSTS_FILE);
                    // 追加
                    JSONObject new_host = new JSONObject();
                    new_host.put("name", hostname.getText());
                    new_host.put("port", port.getText());
                    new_host.put("nick", nick.getText());
                    new_host.put("login", login.getText());
                    new_host.put("charset", charset);
                    json.put(new_host);
                    // 書き込み
                    writeFile(HOSTS_FILE, json.toString());
                } catch (JSONException e) {
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    public JSONArray readJSON(String file) {
        JSONArray json = null;
        try {
            FileInputStream fis = openFileInput(file);
            byte[] readByte = new byte[fis.available()];
            fis.read(readByte);
            json = new JSONArray(new String(readByte));
        } catch (FileNotFoundException e) {
            // ファイルがなければ空のJSON
            return json = new JSONArray();
        } catch (IOException e) {
        } catch (JSONException e) {
        }
        return json;
    }
    
    public boolean writeFile(String file, String src) {
        try {
            FileOutputStream fos = openFileOutput(file, MODE_PRIVATE);
            fos.write(src.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
