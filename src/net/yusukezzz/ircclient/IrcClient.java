package net.yusukezzz.ircclient;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class IrcClient extends Activity {

	// Activity request code
	private static final int SHOW_ADDHOST = 0;
	private static final int SHOW_HOSTLIST = 1;
	public static final String HOSTS_FILE = "hosts.json";

	private static IrcHost currentHost = null;
	private static IrcChannel currentChannel = null;

	// channel view
	private TextView recieve;
	private EditText sendtxt;
	private Button postbtn;

	private static Handler handler = null;

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
		Log.e("IRC", "start");

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
					// this.renderChannel();
				}
			}
		default:
			break;
		}
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