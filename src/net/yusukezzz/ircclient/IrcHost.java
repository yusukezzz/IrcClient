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

import android.util.Log;

public class IrcHost extends Thread {
    private String                      HOST;
    private int                         PORT;
    private String                      NICK;
    private String                      LOGIN;
    private String                      REAL;
    private String                      CHARSET;
    private Socket                      socket       = null;
    private BufferedWriter              bw;
    private BufferedReader              br;
    // ch指定のないテキストを格納
    private String                      receive      = "";
    // 最後に表示されていたchannel
    private IrcChannel                  last_channel = null;

    private HashMap<String, IrcChannel> channels     = new HashMap<String, IrcChannel>();

    public IrcHost(String host, int port, String nick, String login, String real, String charset) {
        HOST = host;
        PORT = port;
        NICK = nick;
        LOGIN = login;
        REAL = real;
        CHARSET = charset;
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
        this.changeNick(NICK);
        this.user();
    }

    /**
     * ホストから切断する
     */
    public void close() {
        if (socket != null) {
            try {
                // quit 送信
                this.quit();
                // 入出力ストリーム切断
                br.close();
                bw.close();
                // ソケット切断
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
                            this.updateMsg("", current);
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
            br = null;
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

    public String getReal() {
        return REAL;
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
    public IrcChannel getLastChannel() {
        return last_channel;
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
        this.write("PONG " + daemon);
    }

    /**
     * ニックネームを変更する
     * @param nick
     */
    public void changeNick(String nick) {
        this.write("NICK " + nick);
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
        this.write("USER " + LOGIN + " " + hostname + " " + HOST + " :" + REAL);
    }

    /**
     * 指定channelに参加する
     * @param ch
     * @return IrcChannel
     */
    public IrcChannel join(String ch_name) {
        IrcChannel ch = null;
        if (!channels.containsKey(ch_name)) {
            this.write("JOIN " + ch_name);
            // チャンネルの追加
            ch = new IrcChannel(ch_name);
            channels.put(ch_name, ch);
        } else {
            ch = channels.get(ch_name);
        }
        // 最後に表示したchannel
        last_channel = ch;
        return ch;
    }

    /**
     * ユーザーリストを要求する
     * @param ch
     */
    public void names(String ch) {
        this.write("NAMES " + ch);
    }

    /**
     * 退室メッセージ
     * @param ch
     */
    public void part(String ch) {
        this.write("PART " + ch);
    }

    public void quit() {
        this.write("QUIT :Leaving...");
    }

    /**
     * 指定channelに発言する
     * @param ch
     * @param str
     */
    public void privmsg(String ch, String str) {
        this.write("PRIVMSG " + ch + " " + str);
        this.updateMsg(ch, "<" + NICK + "> " + str);
    }

    /**
     * 実際にbufferWriterで書き込むメソッド
     * @param cmd
     */
    private void write(String cmd) {
        try {
            bw.write(cmd + "\n");
            bw.flush();
        } catch (IOException e) {
            Log.e("IRC", e.getMessage());
        }
    }

    /**
     * 受信テキストを更新する
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
    }

    /**
     * ホストの設定値をJSONObjectとして返す
     * @return JSONObject
     */
    public JSONObject toJson() {
        JSONObject jsobj = new JSONObject();
        try {
            jsobj.put("name", HOST);
            jsobj.put("port", PORT);
            jsobj.put("nick", NICK);
            jsobj.put("login", LOGIN);
            jsobj.put("real", REAL);
            jsobj.put("charset", CHARSET);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsobj;
    }
}
