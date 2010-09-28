package net.yusukezzz.ircclient;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class IrcClient extends Activity {
    // Menu item ID
    private static final int  MENU_ID_HOSTS  = (Menu.FIRST + 1);
    private static final int  MENU_ID_JOIN   = (Menu.FIRST + 2);


    private static IrcHost    currentHost    = null;
    private static IrcChannel currentChannel = null;

    // channel view
    private TextView          recieve;
    private EditText          sendtxt;
    private Button            postbtn;

    private static Handler    handler        = null;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // レイアウトをchannel画面に
        setContentView(R.layout.main);
        // channelの部品準備
        recieve = (TextView) this.findViewById(R.id.TextView01);
        postbtn = (Button) this.findViewById(R.id.Button01);
        sendtxt = (EditText) this.findViewById(R.id.EditText01);
        // 送信ボタンにイベントをセット
        postbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // サーバに送信
                IrcClient.this.postText(sendtxt.getText().toString());
            }
        });

        // handler の用意
        IrcClient.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // 発言を更新
                        // String[] arr = msg.obj.toString().split("#");
                        if (currentHost != null && currentChannel != null) {
                            // 出力
                            String str = currentChannel.getRecieve();
                            recieve.setText(str);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_HOSTS, Menu.NONE, "hosts");
        menu.add(Menu.NONE, MENU_ID_JOIN, Menu.NONE, "join");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_HOSTS:
                // ホストのリストを表示
                Intent intent = new Intent(IrcClient.this, HostList.class);
                startActivityForResult(intent, HostList.SHOW_HOSTRECIEVE);
                break;
            case MENU_ID_JOIN:
                // join dialog
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 表示に使用するホストを設定する
     * 
     * @param host
     */
    public static void setCurrentHost(IrcHost host) {
        currentHost = host;
    }

    /**
     * 表示に使用するチャンネルを設定する
     * 
     * @param ch
     */
    public static void setCurrentChannel(IrcChannel ch) {
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
     * 
     * @param text
     */
    private void postText(String text) {
        if (currentHost != null && currentChannel != null) {
            currentHost.privmsg(currentChannel.getName(), text);
            sendtxt.setText("");
        }
    }
}