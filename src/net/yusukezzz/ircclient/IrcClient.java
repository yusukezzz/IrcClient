package net.yusukezzz.ircclient;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class IrcClient extends Activity {
    // private String HOST = "chat.freenode.net";//"irc.friend-chat.jp";
    // private Integer PORT = 6667;
    // private String CHARSET = "ISO-2022-JP";
    // private String NICK = "androzzz";
    // private String LOGIN = "androzzz";
    // private String CHANNEL = "#yusukezzz_test";
    private IrcHost                  currentHost;
    private IrcChannel               currentChannel;
    private HashMap<String, IrcHost> hosts = new HashMap<String, IrcHost>();

    // channel view
    private ScrollView               scroll;
    private TextView                 recieve;
    private EditText                 sendtxt;
    private Button                   postbtn;

    // add host view
    private String                   hostname;
    private Integer                  port;
    private String                   nick;
    private String                   login;
    private String                   charset;
    private Button                   addhostbtn;
    
    // host list
    private ListView hostlist;
    private Button newhostbtn;

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
        boolean exists_hosts = false;
        if (exists_hosts) {
            // TODO: ホストのリストを表示
        } else {
            // ホスト追加画面
            this.renderAddHost();
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

    /**
     * ホスト追加画面
     */
    private void renderAddHost() {
        // レイアウトをホスト追加画面に
        setContentView(R.layout.addhost);
        // 要素の用意
        this.hostname = this.findViewById(R.id.addhost_hostname).toString();
        this.port = Integer.parseInt(this.findViewById(R.id.addhost_port)
                .toString());
        this.nick = this.findViewById(R.id.addhost_nick).toString();
        this.login = this.findViewById(R.id.addhost_login).toString();
        Spinner charspn = (Spinner) this.findViewById(R.id.addhost_charset);
        this.charset = charspn.getSelectedItem().toString();
        this.addhostbtn = (Button) this.findViewById(R.id.addhost_addbtn);
        // 追加ボタンにイベントをセット
        addhostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ホストの追加
                // TODO: 入力チェック
                hosts.put(hostname, new IrcHost(hostname, port, nick, login,
                        charset, handler));
                renderHostList();
            }
        });
    }

    /**
     * 登録済みホスト一覧画面
     */
    private void renderHostList() {
        setContentView(R.layout.hostlist);
        hostlist = (ListView) this.findViewById(R.id.hostlist);
        newhostbtn = (Button) this.findViewById(R.id.newhostbtn);
        // ホスト追加画面に遷移
        newhostbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                renderAddHost();
            }
        });
    }
    
//    public class HostListAdapter extends ArrayAdapter {
//    }
}