package net.yusukezzz.ircclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IrcHost extends Thread {
    private String                      HOST;
    private int                         PORT;
    private String                      NICK;
    private String                      LOGIN;
    private String                      CHARSET;
    private Socket                      socket      = null;
    private Handler                     handler;
    private BufferedWriter              bw;
    private BufferedReader              br;
    // ch指定のないテキストを格納
    private String                      receive     = "";
    // 最後に表示されていたchannel
    private IrcChannel                  lastChannel = null;

    private HashMap<String, IrcChannel> channels    = new HashMap<String, IrcChannel>();

    public IrcHost(String host, int port, String nick, String login, String charset) {
        HOST = host;
        PORT = port;
        NICK = nick;
        LOGIN = login;
        CHARSET = charset;
        handler = IrcClient.getHandler();
    }

    /**
     * ホストに接続する
     */
    public void connect() {
        try {
            this.updateMsg("", this.HOST + " connecting...");
            socket = new Socket(this.HOST, this.PORT);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET));
        } catch (UnsupportedEncodingException e) {
            Log.e("IRC", e.getMessage());
        } catch (UnknownHostException e) {
            Log.e("IRC", e.getMessage());
        } catch (IOException e) {
            Log.e("IRC", e.getMessage());
        }
        this.start();
        this.nick();
        this.user();
    }

    /**
     * ホストから切断する
     */
    public void close() {
        if (socket != null) {
            try {
                // TODO: send leave msg
                socket.close();
            } catch (IOException e) {
                Log.e("IRC", e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try {
            // 受信したメッセージを処理
            String current = null;
            while ((current = br.readLine()) != null) {
                // IRCサーバからの応答を識別する
                IrcReply reply = new IrcReply(current);
                int reply_id = reply.parse();
                String[] res = reply.get();
                try {
                    switch (reply_id) {
                        case IrcReply.RID_PING:
                            this.pong(res[1]);
                            break;
                        case IrcReply.RID_SYSMSG:
                            this.updateMsg("", " * " + res[1]);
                            break;
                        case IrcReply.RID_MOTD:
                            this.updateMsg("", res[1]);
                            break;
                        case IrcReply.RID_END_MOTD:
                            Log.d("IRC", "end motd");
                            break;
                        case IrcReply.RID_JOIN:
                            this.updateMsg(res[1], " * join " + res[1]);
                            break;
                        case IrcReply.RID_PRIVMSG:
                            this.updateMsg(res[2], "<" + res[1] + "> " + res[3]);
                            break;
                        case IrcReply.RID_NAMES:
                            this.updateMsg(res[1], " * names " + res[2]);
                            break;
                        default:
                            // this.updateMsg("", current);
                            break;
                    }
                } catch (IndexOutOfBoundsException e) {
                    Log.e("IRC", e.getMessage());
                }
            }
        } catch (UnknownHostException e) {
            Log.e("IRC", e.getMessage());
        } catch (IOException e) {
            Log.e("IRC", e.getMessage());
        }
    }

    /**
     * 接続の状態を返す
     * @return boolean
     */
    public boolean isConnected() {
        if (socket == null) {
            return false;
        } else {
            return socket.isConnected();
        }
    }

    /**
     * ホスト名を返す
     * @return String
     */
    public String getHostName() {
        return HOST;
    }

    public String getPort() {
        return String.valueOf(PORT);
    }

    public String getNick() {
        return NICK;
    }

    public String getLogin() {
        return LOGIN;
    }

    public String getCharset() {
        return CHARSET;
    }

    /**
     * 受信したテキストを返す
     * @return　String
     */
    public String getRecieve() {
        return receive;
    }
    
    /**
     * 最後に表示されたchannelを返す
     * @return IrcChannel
     */
    public IrcChannel getLastChanel() {
        return lastChannel;
    }

    /**
     * 指定したチャンネルのオブジェクトを返す
     * @param name
     * @return IrcChannel
     */
    public IrcChannel getChannel(String name) {
        return channels.get(name);
    }

    /**
     * ping に返信
     * @param daemon
     */
    public void pong(String daemon) {
        this.write("PONG " + daemon + "\n");
    }

    public void nick() {
        this.write("NICK " + NICK + "\n");
    }

    /**
     * ニックネームを変更する
     * @param nick
     */
    public void changeNick(String nick) {
        this.write("NICK " + nick + "\n");
    }

    /**
     * ircサーバにユーザー情報を登録する
     * @param user
     * @param hostname
     * @param server
     * @param realname
     */
    public void user() {
        String hostname = "";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Log.e("IRC", e.getMessage());
        }
        this.write("USER " + NICK + " " + hostname + " " + HOST + " " + LOGIN + "\n");
    }

    /**
     * 指定channelに参加する
     * @param ch
     * @return IrcChannel
     */
    public IrcChannel join(String ch_name) {
        this.write("JOIN " + ch_name + "\n");
        // チャンネルの追加
        IrcChannel ch = new IrcChannel(ch_name);
        channels.put(ch_name, ch);
        lastChannel = ch;
        return ch;
    }

    /**
     * ユーザーリストを要求する
     * @param ch
     */
    public void names(String ch) {
        this.write("NAMES " + ch + "\n");
    }

    /**
     * 退室メッセージ
     * @param ch
     */
    public void leave(String ch) {
        this.write("");
    }

    /**
     * 指定channelに発言する
     * @param ch
     * @param str
     */
    public void privmsg(String ch, String str) {
        this.write("PRIVMSG " + ch + " " + str + "\n");
        this.updateMsg(ch, "<" + NICK + "> " + str);
    }

    /**
     * 実際にbufferWriterで書き込むメソッド
     * @param cmd
     */
    private void write(String cmd) {
        try {
            bw.write(cmd);
            bw.flush();
        } catch (IOException e) {
            Log.e("IRC", e.getMessage());
        }
    }

    /**
     * 受信テキストを更新し、handlerに描画指示を送る
     * @param ch
     * @param text
     */
    private void updateMsg(String ch, String text) {
        String line = Util.getTime() + " " + text + "\n";
        IrcChannel channel = null;
        try {
            channel = channels.get(ch);
        } catch (NullPointerException e) {
            channel = null;
        }
        if (channel == null) {
            receive += line;
        } else {
            channel.addRecieve(line);
        }
        // 表示の更新を指示
        handler.sendEmptyMessage(0);
    }

    public JSONObject toJson() {
        JSONObject jsobj = new JSONObject();
        try {
            jsobj.put("name", HOST);
            jsobj.put("port", PORT);
            jsobj.put("nick", NICK);
            jsobj.put("login", LOGIN);
            jsobj.put("charset", CHARSET);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsobj;
    }
}
