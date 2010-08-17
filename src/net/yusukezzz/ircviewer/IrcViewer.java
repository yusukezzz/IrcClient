package net.yusukezzz.ircviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class IrcViewer extends Activity {
    private String   HOST    = "irc.friend-chat.jp";
    private Integer  PORT    = 6660;                // なんか6667は混んでるらしいので
    private String   NICK    = "androzzz";
    private String   LOGIN   = "androzzz";
    private String   CHANNEL = "#yusukezzz_test";

    private TextView recieve;
    private EditText sendtxt;
    private Button   postbtn;
    private Handler  handler;
    private Irc irc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.viewer);
        // viewの部品準備
        recieve = (TextView) this.findViewById(R.id.recieve);
        postbtn = (Button) this.findViewById(R.id.Button01);
        sendtxt = (EditText) this.findViewById(R.id.EditText01);
        // 送信ボタンにイベントをセット
        postbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IrcViewer.this.postText(CHANNEL, sendtxt.getText().toString());
            }
        });
        // 受信したテキストをTextViewに出力するhandler
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // 出力
                        HashMap<String,String> chat = (HashMap<String,String>) msg.obj;
                        recieve.setText(recieve.getText() + chat.get("text").toString()
                                + "\n");
                }
                super.handleMessage(msg);
            }
        };
        // 通信開始
        this.irc = new Irc(this.HOST, this.PORT, this.handler);
        this.irc.join(CHANNEL);
    }
    
    /**
     * テキストをIRCサーバに送信
     */
    private void postText(String ch, String text) {
        this.irc.privmsg(ch, text);
    }
}