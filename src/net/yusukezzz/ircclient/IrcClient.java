package net.yusukezzz.ircclient;

import java.text.DecimalFormat;
import java.util.Calendar;
import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
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

    // Activity request code
    private static final int   SHOW_ADDHOST  = 0;
    private static final int   SHOW_HOSTLIST = 1;
    public static final String HOSTS_FILE    = "hosts.json";

    private static IrcHost            currentHost = null;
    private static IrcChannel         currentChannel = null;

    // channel view
    private ScrollView         scroll;
    private TextView           recieve;
    private EditText           sendtxt;
    private Button             postbtn;

    private static Handler     handler       = null;
    private Integer            Height;
    
    public IrcClient() {
    	// handler の用意
        IrcClient.handler = new Handler() {
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
        Log.e("IRC", "start");
    }

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
        MyJson myjson = new MyJson(getApplicationContext());
        JSONArray hosts = myjson.readFile(HOSTS_FILE);
        boolean exists_hosts = (hosts.length() > 0) ? true : false;
        if (exists_hosts) {
            // ホストのリストを表示
            Intent intent = new Intent(IrcClient.this, HostList.class);
            startActivityForResult(intent, SHOW_HOSTLIST);
        } else {
            // ホスト追加/編集
            Intent intent = new Intent(IrcClient.this, EditHost.class);
            startActivityForResult(intent, SHOW_ADDHOST);
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case SHOW_ADDHOST:
                if (resCode == RESULT_OK) {
                    // ホスト一覧へ
                    Intent intent = new Intent(IrcClient.this, HostList.class);
                    startActivityForResult(intent, SHOW_HOSTLIST);
                }
                break;
            case SHOW_HOSTLIST:
                if (resCode == RESULT_OK) {
                    if (currentHost != null) {
                    	this.renderChannel("");
                    }
                }
            default:
                break;
        }
    }
    
    /**
     * 表示に使用するホストを設定する
     * @param host
     */
    public static void setCurrentHost(IrcHost host) {
    	currentHost = host;
    }
    
    public void setCurrentChannel(IrcChannel ch) {
    	currentChannel = ch;
    }

    /**
     * Handlerを返す
     * 
     * @return handler
     */
    public static Handler getHandler() {
        return IrcClient.handler;
    }

    /**
     * テキストをIRCサーバに送信
     */
    private void postText(String text) {
    	if (currentHost != null && currentChannel != null) {
    		currentHost.privmsg(currentChannel.getName(), text);
    	}
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
                    IrcClient.this.postText(sendtxt.getText().toString());
                }
            }
        });
    }
}