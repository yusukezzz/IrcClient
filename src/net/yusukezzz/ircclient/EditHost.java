package net.yusukezzz.ircclient;

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
    // edit host view
    private EditText hostname;
    private EditText port;
    private EditText nick;
    private EditText login;
    private EditText real;
    private Spinner  charspn;
    private int      charset;
    private Button   edithostbtn;
    private int      host_no = -1;

    /**
     * 文字コードを返す
     * @param pos
     * @return charset
     */
    private String getCharsets(int pos) {
        String[] charsets = getResources().getStringArray(R.array.charsets);
        return charsets[pos];
    }

    /**
     * 文字コードの番号を返す
     * @param charset
     * @return
     */
    private int getCharsetsPos(String charset) {
        int pos = 0;
        String[] charsets = getResources().getStringArray(R.array.charsets);
        for (int i = 0; i < charsets.length; i++) {
            if (charsets[i] == charset) {
                pos = i;
            }
        }
        return pos;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edithost);

        // 要素の用意
        hostname = (EditText) findViewById(R.id.edithost_hostname);
        port = (EditText) findViewById(R.id.edithost_port);
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
                port.setText(host.getPort(), BufferType.NORMAL);
                nick.setText(host.getNick(), BufferType.NORMAL);
                login.setText(host.getLogin(), BufferType.NORMAL);
                real.setText(host.getReal(), BufferType.NORMAL);
                charspn.setSelection(getCharsetsPos(host.getCharset()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // saveを押された時の処理
        edithostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    charset = (int) charspn.getSelectedItemId();
                    // 更新の場合は削除してから追加
                    if (host_no != -1) {
                        HostList.removeHost(host_no);
                    }
                    // 追加
                    HostList.addHost(new IrcHost(hostname.getText().toString(), Integer
                            .parseInt(port.getText().toString()), nick.getText().toString(), login
                            .getText().toString(), real.getText().toString(), getCharsets(charset)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // ホスト一覧に戻る
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
