package net.yusukezzz.ircclient;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class IrcClient extends Activity {
    // Menu item ID
    private static final int MENU_ID_HOSTS = (Menu.FIRST + 1);
    private static final int MENU_ID_JOIN = (Menu.FIRST + 2);

    private final int UPDATE_INTERVAL = 1000;
    private Handler handler = new Handler();

    // channel view
    private TextView title;
    private TextView recieve;
    private EditText sendtxt;
    private Button postbtn;
    private ListView user_list;
    private UserListAdapter adapter;

    private ArrayList<User> users = new ArrayList<User>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // レイアウトをchannel画面に
        try {
            setContentView(R.layout.channel_with_user_list);
        } catch (Exception e) {
            Util.d(e.getStackTrace());
        }
        // channelの部品準備
        title = (TextView) findViewById(R.id.title);
        recieve = (TextView) this.findViewById(R.id.recieve);
        postbtn = (Button) this.findViewById(R.id.post_btn);
        sendtxt = (EditText) this.findViewById(R.id.send_test);
        // 送信ボタンにイベントをセット
        postbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // サーバに送信
                IrcClient.this.postText(sendtxt.getText().toString());
            }
        });
        // ユーザーリストの準備
        adapter = new UserListAdapter(getApplicationContext(), R.layout.user_list_row, users);
        user_list = (ListView) this.findViewById(R.id.user_list);
        user_list.setAdapter(adapter);

        // 定期的に受信テキストの表示を更新
        Runnable looper = new Runnable() {
            public void run() {
                if (HostList.currentHost != null) {
                    String str = HostList.currentCh == null ? HostList.currentHost.getRecieve()
                            : HostList.currentCh.getRecieve();
                    recieve.setText(str);
                    updateTitle();
                    updateUserList();
                    // UPDATE_INTERVAL ms後に再描画
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        };
        // 初回表示
        handler.post(looper);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                setResult(RESULT_OK);
                finish();
                break;
            case MENU_ID_JOIN:
                if (HostList.currentHost == null) {
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
                    public void onClick(DialogInterface dialog, int which) {
                        String ch = edit.getText().toString();
                        HostList.setCurrentCh(HostList.currentHost.join(ch));
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // ホストのリストを表示
            setResult(RESULT_OK);
            finish();
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * タイトルを更新する 実際にはトピックを表示する
     */
    public void updateTitle() {
        String str = HostList.currentHost.getHostName()
                + (HostList.currentCh != null ? HostList.currentCh.getName() : "");
        title.setText(str);
    }

    public void updateUserList() {
        if (HostList.currentHost != null && HostList.currentCh != null) {
            adapter = new UserListAdapter(getApplicationContext(), R.layout.user_list_row,
                    HostList.currentCh.getUsers());
            user_list.setAdapter(adapter);
        }
    }

    /**
     * テキストをIRCサーバに送信
     * @param text
     */
    private void postText(String text) {
        if (HostList.currentHost != null && HostList.currentCh != null) {
            HostList.currentHost.privmsg(HostList.currentCh.getName(), text);
            sendtxt.setText("");
        }
    }
}
