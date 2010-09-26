package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HostList extends ListActivity {
	private MyJson myjson = null;
	private JSONArray json = null;
	private List<IrcHost> hosts = null;

	/**
	 * 文字コードを返す
	 * 
	 * @return charset[]
	 */
	private String getCharsets(int pos) {
		String[] charsets = getResources().getStringArray(R.array.charsets);
		return charsets[pos];
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hostlist);

		// host設定の取り出しと設定
		myjson = new MyJson(getApplicationContext());
		json = myjson.readFile(IrcClient.HOSTS_FILE);
		hosts = new ArrayList<IrcHost>();
		int host_num = json.length();
		for (int i = 0; i < host_num; i++) {
			JSONObject jsobj;
			try {
				jsobj = json.getJSONObject(i);
				hosts.add(new IrcHost(jsobj.getString("name"), jsobj
						.getInt("port"), jsobj.getString("nick"), jsobj
						.getString("login"), getCharsets(jsobj
						.getInt("charset"))));
			} catch (JSONException e) {
			}
		}
		// アダプターにセット
		HostAdapter adapter = new HostAdapter(this, R.layout.hostlist_row,
				hosts);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// 選択されたホスト
		final int host_no = position;
		final IrcHost host = hosts.get(host_no);
		// ダイアログで操作を表示
		final AlertDialog.Builder ad = new AlertDialog.Builder(HostList.this);
		final CharSequence[] menus = { "connect", "edit", "delete" };
		ad.setTitle("Action");
		ad.setItems(menus, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					if (host.isConnected()) {
						host.close();
					} else {
						host.connect();
						host.join("#yusukezzz_test");
						IrcClient.setCurrentHost(host);
						IrcClient.setCurrentChannel(host.getChannel("#yusukezzz_test"));
						setResult(RESULT_OK);
						finish();
					}
					break;
				case 1:
					Intent i = new Intent(HostList.this, EditHost.class);
					i.putExtra("host_no", host_no);
					startActivity(i);
					break;
				case 2:
					HostList.this.removeHost(host_no);
					break;
				default:
					break;
				}
			}
		});
		ad.create().show();
	}

	/**
	 * hostsから設定を削除し、ファイルを更新
	 * 
	 * @param host_no
	 */
	private void removeHost(int host_no) {
		if (!hosts.isEmpty()) {
			try {
				// 削除
				hosts.remove(host_no);
				// 最新のhostsをファイルに保存
				JSONArray tmp = new JSONArray();
				for (int i = 0; i < hosts.size(); i++) {
					tmp.put(hosts.get(i));
				}
				myjson.writeFile(IrcClient.HOSTS_FILE, tmp.toString());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public class HostAdapter extends ArrayAdapter<IrcHost> {
		private List<IrcHost> hosts;
		private LayoutInflater inflater;

		public HostAdapter(Context context, int resourceId, List<IrcHost> items) {
			super(context, resourceId, items);
			hosts = items;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				// 1行分のviewを生成
				view = inflater.inflate(R.layout.hostlist_row, null);
			}
			IrcHost host = hosts.get(position);
			TextView textView = (TextView) view
					.findViewById(R.id.hostlist_row_title);
			textView.setText(host.getHostName());
			return view;
		}
	}
}
