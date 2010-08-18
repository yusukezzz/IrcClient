package net.yusukezzz.ircviewer;

import java.util.Calendar;
import java.util.HashMap;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class IrcViewer extends Activity {
    private String     HOST    = "irc.friend-chat.jp";
    private Integer    PORT    = 6660;                // なんか6667は混んでるらしいので
    private String     NICK    = "androzzz";
    private String     LOGIN   = "androzzz";
    private String     CHANNEL = "#yusukezzz_test";

    private ScrollView scroll;
    private TextView   recieve;
    private EditText   sendtxt;
    private Button     postbtn;
    private Handler    handler;
    private Irc        irc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.viewer);
        // viewの部品準備
        scroll = (ScrollView) this.findViewById(R.id.ScrollView01);
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
                        // 現在時刻の取得
                        Calendar now = Calendar.getInstance();
                        int h = now.get(Calendar.HOUR_OF_DAY);
                        int m = now.get(Calendar.MINUTE);
                        String time = h + ":" + m;
                        // 出力
                        Boolean toBtm = false;
                        Integer pos = recieve.getBottom() - scroll.getScrollY();
                        if (pos < 800) {// TODO: 画面サイズ取得
                            toBtm = true;
                        }
                        recieve.setText(recieve.getText() + time + " "
                                + msg.obj.toString() + "(" + pos + ")\n");
                        if (toBtm) {
                            scrollToBottom();
                            toBtm = false;
                        }
                }
                super.handleMessage(msg);
            }
        };
        // 通信開始
        try {
            this.irc = new Irc(this.HOST, this.PORT, this.NICK, this.handler);
            this.irc.nick(NICK);
            this.irc.user(LOGIN, "host", "server", "yusukezzz");
            this.irc.join(CHANNEL);
        } catch (Exception e) {
            // TODO: handle exception
            recieve.setText(e.getMessage());
        }
    }

    /**
     * テキストをIRCサーバに送信
     */
    private void postText(String ch, String text) {
        this.irc.privmsg(ch, text);
    }
    
    private void scrollToBottom() {
        scroll.post(new Runnable() {
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}