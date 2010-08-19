package net.yusukezzz.ircclient;

import java.text.DecimalFormat;
import java.util.Calendar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class IrcClient extends Activity {
    private String     HOST    = "irc.friend-chat.jp";
    private Integer    PORT    = 6660;                    // なんか6667は混んでるらしいので
    private String     NICK    = "androzzz";
    private String     LOGIN   = "androzzz";
    private String     CHANNEL = "#yusukezzz_test";

    private ScrollView scroll;
    private TextView   recieve;
    private EditText   sendtxt;
    private Button     postbtn;
    private Handler    handler;
    private IrcHost    ircHost;
    private Integer Height;
    private Integer Width;

    @Override
    public void onResume() {
        super.onResume();

        // 画面情報取得
        WindowManager windowMng = getWindowManager();
        Display display = windowMng.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        Height = display.getHeight();
        Width = display.getWidth();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        // viewの部品準備
        scroll = (ScrollView) this.findViewById(R.id.ScrollView01);
        recieve = (TextView) this.findViewById(R.id.TextView01);
        postbtn = (Button) this.findViewById(R.id.Button01);
        sendtxt = (EditText) this.findViewById(R.id.EditText01);
        // 送信ボタンにイベントをセット
        postbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IrcClient.this.postText(CHANNEL, sendtxt.getText().toString());
            }
        });
        // 受信したテキストをTextViewに出力するhandler
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // 現在時刻の取得
                        DecimalFormat df = new DecimalFormat();
                        df.applyLocalizedPattern("00");
                        Calendar now = Calendar.getInstance();
                        int h = now.get(Calendar.HOUR_OF_DAY);
                        int m = now.get(Calendar.MINUTE);
                        String time = df.format(h) + ":" + df.format(m);
                        // 現在のスクロール位置を取得
                        Integer pos = recieve.getBottom() - scroll.getScrollY();
                        // 最下行付近チェック
                        Boolean toBtm = false;
                        if (pos < Height) {
                            toBtm = true;
                        }
                        // 出力
                        recieve.setText(recieve.getText() + time + " " + msg.obj.toString() + "\n");
                        // 最下行付近なら新規書き込みに追従させる
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
            this.ircHost = new IrcHost(this.HOST, this.PORT, this.NICK, this.handler);
            this.ircHost.nick(NICK);
            this.ircHost.user(LOGIN, "host", "server", "yusukezzz");
            this.ircHost.join(CHANNEL);
        } catch (Exception e) {
            recieve.setText(e.getMessage());
        }
    }

    /**
     * テキストをIRCサーバに送信
     */
    private void postText(String ch, String text) {
        this.ircHost.privmsg(ch, text);
    }

    private void scrollToBottom() {
        scroll.post(new Runnable() {
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}