package net.yusukezzz.ircclient;

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
    // edit host view
    private EditText hostname;
    private EditText port;
    private EditText nick;
    private EditText login;
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
        for (int i=0; i<charsets.length; i++) {
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
        this.hostname = (EditText) this.findViewById(R.id.edithost_hostname);
        this.port = (EditText) this.findViewById(R.id.edithost_port);
        this.nick = (EditText) this.findViewById(R.id.edithost_nick);
        this.login = (EditText) this.findViewById(R.id.edithost_login);
        charspn = (Spinner) this.findViewById(R.id.edithost_charset);
        this.edithostbtn = (Button) this.findViewById(R.id.edithost_btn);

        Intent i = getIntent();
        host_no = i.getIntExtra("host_no", -1);
        if (host_no != -1) {
            try {
                IrcHost host = IrcClient.getHost(host_no);
                hostname.setText(host.getHostName(), BufferType.NORMAL);
                port.setText(host.getPort(), BufferType.NORMAL);
                nick.setText(host.getNick(), BufferType.NORMAL);
                login.setText(host.getLogin(), BufferType.NORMAL);
                charspn.setSelection(getCharsetsPos(host.getCharset()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        edithostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ホストを保存する
                try {
                    charset = (int) charspn.getSelectedItemId();
                    Log.d("IRC", getCharsets(charset));
                    // 更新の場合は削除してから追加
                    if (host_no != -1) {
                        IrcClient.removeHost(host_no);
                    }
                    IrcClient.addHost(new IrcHost(hostname.getText().toString(), Integer
                            .parseInt(port.getText().toString()), nick.getText().toString(), login
                            .getText().toString(), getCharsets(charset)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
