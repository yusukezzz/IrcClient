package net.yusukezzz.ircclient;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    // private String HOST = "chat.freenode.net";//"irc.friend-chat.jp";
    // private Integer PORT = 6667;
    // private String CHARSET = "ISO-2022-JP";
    // private String NICK = "androzzz";
    // private String LOGIN = "androzzz";
    // private String CHANNEL = "#yusukezzz_test";

    // Activity request code
    private static final int         SHOW_ADDHOST  = 0;
    private static final int         SHOW_HOSTLIST = 1;
    public static final String       HOSTS_FILE    = "hosts.json";
    private EditText                 addhostname;

    private IrcHost                  currentHost;
    private IrcChannel               currentChannel;
    private HashMap<String, IrcHost> hosts         = new HashMap<String, IrcHost>();

    // channel view
    private ScrollView               scroll;
    private TextView                 recieve;
    private EditText                 sendtxt;
    private Button                   postbtn;

    private Handler                  handler;
    private IrcHost                  ircHost;
    private Integer                  Height;

    @Override
    public void onResume() {
        super.onResume();

        // 画面情報取得
        WindowManager windowMng = getWindowManager();
        Display display = windowMng.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        Height = display.getHeight();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 登録済みホストがあればホスト一覧へ なければホスト追加画面へ
        boolean exists_hosts = false;
        if (exists_hosts) {
            // TODO: ホストのリストを表示
        } else {
            // ホスト追加
            // this.addHost();
            Intent intent = new Intent(IrcClient.this, EditHost.class);
            startActivityForResult(intent, SHOW_ADDHOST);
        }
        // 受信したテキストをTextViewに出力するhandler
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // 現在のスクロール位置を取得
                        Integer pos = recieve.getBottom() - scroll.getScrollY();
                        // 最下行付近チェック
                        Boolean toBtm = false;
                        if (pos < Height) {
                            toBtm = true;
                        }
                        // 出力
                        recieve.setText(recieve.getText() + getTime() + " "
                                + msg.obj.toString() + "\n");
                        // 最下行付近なら新規書き込みに追従させる
                        if (toBtm) {
                            scrollToBottom();
                            toBtm = false;
                        }
                }
                super.handleMessage(msg);
            }
        };
        // // 通信開始
        // try {
        // this.ircHost = new IrcHost(this.HOST, this.PORT, this.NICK,
        // this.CHARSET, this.handler);
        // this.ircHost.nick(NICK);
        // this.ircHost.user();
        // this.ircHost.join(CHANNEL);
        // } catch (Exception e) {
        // recieve.setText(e.getMessage());
        // }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case SHOW_ADDHOST:
                if (resCode == RESULT_OK) {
                    // ホスト一覧へ
                    setContentView(R.layout.main);
                }
                break;
            case SHOW_HOSTLIST:
                if (resCode == RESULT_OK) {
                    // default
                }
            default:
                break;
        }
    }

    /**
     * テキストをIRCサーバに送信
     */
    private void postText(String ch, String text) {
        this.ircHost.privmsg(ch, text);
    }

    /**
     * 一番下までスクロールする
     */
    private void scrollToBottom() {
        scroll.post(new Runnable() {
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /**
     * hh:mm形式の現在時間文字列を返す
     * 
     * @return time hh:mm
     */
    private String getTime() {
        // 2桁で表示するため
        DecimalFormat df = new DecimalFormat();
        df.applyLocalizedPattern("00");
        // 現在時刻の取得
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        String time = df.format(h) + ":" + df.format(m);
        return time;
    }

    public void addHost() {
        addhostname = new EditText(this);
        new AlertDialog.Builder(this).setTitle("put new hostname")
                .setView(addhostname)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(IrcClient.this,
                                EditHost.class);
                        intent.putExtra("hostname", addhostname.getText()
                                .toString());
                        startActivityForResult(intent, SHOW_ADDHOST);
                    }
                }).show();
    }

    /**
     * channel画面
     * 
     * @param ch
     */
    private void renderChannel(String ch) {
        // レイアウトをchannel画面に
        setContentView(R.layout.main);
        // channelの部品準備
        scroll = (ScrollView) this.findViewById(R.id.ScrollView01);
        recieve = (TextView) this.findViewById(R.id.TextView01);
        postbtn = (Button) this.findViewById(R.id.Button01);
        sendtxt = (EditText) this.findViewById(R.id.EditText01);
        // 送信ボタンにイベントをセット
        postbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.toString() == "channel") {
                    IrcClient.this.postText(currentChannel.getName(), sendtxt
                            .getText().toString());
                }
            }
        });
    }
}