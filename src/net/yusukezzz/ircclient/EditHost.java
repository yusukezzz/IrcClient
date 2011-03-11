package net.yusukezzz.ircclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

public class EditHost extends Activity {
    // edit host view
    private EditText hostname;
    private CheckBox use_ssl;
    private EditText port;
    private EditText pass;
    private EditText nick;
    private EditText login;
    private EditText real;
    private Spinner  charspn;
    private Button   edithostbtn;
    private int      host_no = -1;

    /**
     * 文字コードを返す
     * @param pos
     * @return charset
     */
    private String getCharset(int pos) {
        String[] charsets = getResources().getStringArray(R.array.charsets);
        return charsets[pos];
    }

    /**
     * 文字コードの番号を返す
     * @param charset
     * @return
     */
    private int getCharsetsPos(String charset) {
        String[] charsets = getResources().getStringArray(R.array.charsets);
        for (int i = 0; i < charsets.length; i++) {
            if (charsets[i].equals(charset)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_host);

        // 要素の用意
        hostname = (EditText) findViewById(R.id.edithost_hostname);
        use_ssl = (CheckBox) findViewById(R.id.edithost_use_ssl);
        port = (EditText) findViewById(R.id.edithost_port);
        pass = (EditText) findViewById(R.id.edithost_server_pass);
        nick = (EditText) findViewById(R.id.edithost_nick);
        login = (EditText) findViewById(R.id.edithost_login);
        real = (EditText) findViewById(R.id.edithost_real);
        charspn = (Spinner) findViewById(R.id.edithost_charset);
        edithostbtn = (Button) findViewById(R.id.edithost_btn);

        // インテント取得
        Intent i = getIntent();
        // 更新だったらデータセット
        host_no = i.getIntExtra("host_no", -1);
        if (host_no != -1) {
            try {
                IrcHost host = HostList.getHost(host_no);
                hostname.setText(host.getHostName(), BufferType.NORMAL);
                use_ssl.setChecked(host.getUseSSL());
                port.setText(host.getPort(), BufferType.NORMAL);
                pass.setText(host.getPassword());
                nick.setText(host.getNick(), BufferType.NORMAL);
                login.setText(host.getLogin(), BufferType.NORMAL);
                real.setText(host.getReal(), BufferType.NORMAL);
                charspn.setSelection(getCharsetsPos(host.getCharset()));
            } catch (Exception e) {
                Util.d(e.getMessage());
            }
        }

        // saveを押された時の処理
        edithostbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    int pos = (int) charspn.getSelectedItemId();
                    Log.d("IRC", pos + "=" + getCharset(pos));
                    // 更新の場合は削除してから追加
                    if (host_no != -1) {
                        HostList.removeHost(host_no);
                    }
                    // 追加
                    HostList.addHost(new IrcHost(hostname.getText().toString(),
                            use_ssl.isChecked(), Integer.parseInt(port.getText().toString()), pass
                                    .getText().toString(), nick.getText().toString(), login
                                    .getText().toString(), real.getText().toString(),
                            getCharset(pos)));
                } catch (Exception e) {
                    Util.d(e.getMessage());
                }
                // ホスト一覧に戻る
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
