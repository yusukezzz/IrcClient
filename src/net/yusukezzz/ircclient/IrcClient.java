package net.yusukezzz.ircclient;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
                // 発言表示を更新
                if (currentHost != null) {
                    // 出力
                    String str = currentChannel == null ? currentHost.getRecieve() : currentChannel
                            .getRecieve();
                    recieve.setText(str);
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
                        .getString("nick"), jsobj.getString("login"), jsobj.getString("charset")));
            } catch (JSONException e) {
                Log.e("IRC", e.getMessage());
            }
        }

        // ホスト一覧へ
        Intent intent = new Intent(this, HostList.class);
        startActivityForResult(intent, SHOW_HOSTLIST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case SHOW_HOSTLIST:
                if (resCode != RESULT_OK) {
                    Log.e("IRC", reqCode + ": " + resCode);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handlerを返す
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
                if (currentHost == null) {
                    break;
                }
                // join dialog
                final EditText edit = new EditText(this);
                edit.setSingleLine();
                edit.setWidth(100);
                edit.setText("#");
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("input channel name.\nLike #hoge");
                dialog.setView(edit);
                dialog.setPositiveButton("join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ch = edit.getText().toString();
                        setCurrentChannel(currentHost.join(ch));
                    }
                });
                dialog.setNegativeButton("cancel", null);
                dialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 表示に使用するホストとチャンネル(最後に見ていたもの)を設定する
     * @param host
     */
    public static void setCurrentHost(IrcHost host) {
        currentHost = host;
        currentChannel = host.getLastChanel();
        // view の更新
        IrcClient.handler.sendEmptyMessage(0);
    }

    /**
     * 表示に使用するチャンネルを設定する
     * @param ch
     */
    public static void setCurrentChannel(IrcChannel ch) {
        currentChannel = ch;
        // view の更新
        IrcClient.handler.sendEmptyMessage(0);
    }

    /**
     * ホストを返す
     * @param pos
     * @return IrcHost
     */
    public static IrcHost getHost(int pos) {
        IrcHost host = null;
        try {
            host = hosts.get(pos);
        } catch (Exception e) {
            Log.e("IRC", e.getMessage());
        }
        return host;
    }

    /**
     * ホストのリストを返す
     * @return ArrayList<IrcHost>
     */
    public static ArrayList<IrcHost> getHosts() {
        return hosts;
    }

    /**
     * テキストをIRCサーバに送信
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
                Log.e("IRC", e.getMessage());
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
                json.put(tmp.toJson());
            }
            myjson.writeFile(HOSTS_FILE, json.toString());
        }
    }
}