package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private static final int          MENU_ID_HOSTS  = (Menu.FIRST + 1);
    private static final int          MENU_ID_JOIN   = (Menu.FIRST + 2);

    // Activity request code
    public static final int           SHOW_HOSTLIST  = 0;
    public static final int           SHOW_EDITHOST  = 1;

    private static IrcHost            currentHost    = null;
    private static IrcChannel         currentChannel = null;
    private static Handler            handler        = null;

    // channel view
    private TextView                  recieve;
    private EditText                  sendtxt;
    private Button                    postbtn;

    private static MyJson             myjson         = null;
    private static ArrayList<IrcHost> hosts          = null;
    public static final String        HOSTS_FILE     = "hosts.json";

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
                        if (currentHost != null) {
                            // 出力
                            String str = currentChannel == null ? currentHost.getRecieve()
                                    : currentChannel.getRecieve();
                            recieve.setText(str);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        // host設定の読み込み
        myjson = new MyJson(getApplicationContext());
        JSONArray json = myjson.readFile(HOSTS_FILE);
        hosts = new ArrayList<IrcHost>();
        int host_num = json.length();
        for (int i = 0; i < host_num; i++) {
            JSONObject jsobj;
            try {
                jsobj = json.getJSONObject(i);
                hosts.add(new IrcHost(jsobj.getString("name"), jsobj.getInt("port"), jsobj
                        .getString("nick"), jsobj.getString("login"), getCharsets(jsobj
                        .getInt("charset"))));
            } catch (JSONException e) {
            }
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case SHOW_HOSTLIST:
                if (resCode == RESULT_OK) {
                    // host channel
                    // Intent intent = new Intent(this, IrcClient.class);
                    // startActivityForResult(intent, SHOW_HOSTRECIEVE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handlerを返す
     * 
     * @return handler
     */
    public static Handler getHandler() {
        return IrcClient.handler;
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
                startActivityForResult(intent, SHOW_HOSTLIST);
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

    public static List<IrcHost> getHosts() {
        return hosts;
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

    /**
     * ホストを追加する
     * 
     * @param host
     */
    public static void addHost(IrcHost host) {
        if (host != null) {
            hosts.add(host);
            updateJson();
        }
    }

    /**
     * hostsから設定を削除し、ファイルを更新
     * 
     * @param host_no
     */
    public static void removeHost(int host_no) {
        if (!hosts.isEmpty()) {
            try {
                IrcHost host = hosts.get(host_no);
                if (host.isConnected()) {
                    host.close();
                }
                // 削除
                hosts.remove(host_no);
                updateJson();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * 最新のhostsをファイルに保存
     */
    private static void updateJson() {
        if (!hosts.isEmpty()) {
            JSONArray json = new JSONArray();
            for (IrcHost tmp : hosts) {
                json.put(tmp);
            }
            myjson.writeFile(HOSTS_FILE, json.toString());
        }
    }

    /**
     * 文字コードを返す
     * 
     * @return charset[]
     */
    private String getCharsets(int pos) {
        String[] charsets = getResources().getStringArray(R.array.charsets);
        return charsets[pos];
    }
}