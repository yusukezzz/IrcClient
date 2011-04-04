package net.yusukezzz.ircclient;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChannelListAdapter extends ArrayAdapter<IrcChannel> {
    private List<IrcChannel> channels;
    private LayoutInflater inflater;

    public ChannelListAdapter(Context context, int resource, List<IrcChannel> items) {
        super(context, resource, items);
        channels = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            // 1行分のviewを生成
            view = inflater.inflate(R.layout.channel_list_row, null);
        }
        final IrcChannel ch = channels.get(position);
        TextView textView = (TextView) view.findViewById(R.id.channel_list_row_name);
        textView.setText(ch.getName());
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    HostList.setCurrentCh(ch);
                }
                return true;
            }
        });
        return view;
    }
}
