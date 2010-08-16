package net.yusukezzz.ircviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.TextView;

public class IrcViewer extends Activity {
    private String   HOST    = "irc.friend-chat.jp";
    private Integer  PORT    = 6660; // なんか6667は混んでるらしいので
    private String   NICK    = "androzzz";
    private String   LOGIN   = "androzzz";
    private String   CHANNEL = "#yusukezzz_test";

    private TextView recieve;
    private Handler  handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.viewer);
        // TexxtViewの準備
        recieve = (TextView) this.findViewById(R.id.recieve);
        recieve.setText("");
        // 受信したテキストをTextViewに出力するhandler
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // 出力
                        recieve.setText(msg.obj.toString() + "\n"
                                + recieve.getText() + "\n");
                }
                super.handleMessage(msg);
            }
        };

        // 通信用thread
        (new Thread() {
            public void run() {
                try {
                    connect(HOST, PORT);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }).start();
    }

    /**
     * IRCサーバに接続し、データを受信する
     *
     * @param host
     * @param port
     */
    private void connect(String host, Integer port) {
        try {
            // 通信開始
        	this.showMessage(host + " connecting...");
            Socket irc = new Socket(host, port);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    irc.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    irc.getInputStream(), "ISO-2022-JP"));// とりあえず文字コード決め打ち
            // ログイン
            bw.write("NICK " + NICK + "\n");
            bw.write("USER " + LOGIN + " test by yusukezzz\n");
            bw.flush();
            // チャンネルに参加
            bw.write("JOIN " + CHANNEL + "\n");
            bw.flush();
            // 1行ずつ処理
            String current = null;
            while ((current = br.readLine()) != null) {
                // PING PONG
                Pattern pingRegex = Pattern.compile("^PING",
                        Pattern.CASE_INSENSITIVE);
                Matcher ping = pingRegex.matcher(current);
                if (ping.find()) {
                    bw.write("PONG " + CHANNEL + "\n");
                    bw.write("PRIVMSG " + CHANNEL + " PONG!\n");
                    bw.flush();
                }
                // JOIN の表示
                Pattern joinRegex = Pattern.compile("JOIN :" + CHANNEL,
                		Pattern.CASE_INSENSITIVE);
                Matcher join = joinRegex.matcher(current);
                if (join.find()) {
                	this.showMessage(CHANNEL + " joined.");
                }
                // PRIVMSG のテキストをhandlerへ
                Pattern pmsgRegex = Pattern.compile(
                		":([a-zA-Z0-9_]+?)!.+? PRIVMSG #[a-zA-Z0-9_]+? :(.+)");
                Matcher pmsg = pmsgRegex.matcher(current);
                if (pmsg.find()) {
                	String text = "<" + pmsg.group(1) + "> " + pmsg.group(2);
                	this.showMessage(text);
                }
            }
        } catch (IOException e) {
            this.showMessage("[Err]" + e.getMessage());
        }
    }

    /**
     * handlerに表示するテキストを送る
     *
     * @param text
     */
    private void showMessage(String text) {
        Message msg;
        msg = new Message();
        msg.obj = text;
        msg.what = 0;
        IrcViewer.this.handler.sendMessage(msg);
    }
}