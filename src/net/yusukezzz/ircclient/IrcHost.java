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

public class IrcHost extends Thread {
    private String SETTING_NAME;
    private String HOST;
    private boolean USE_SSL;
    private int PORT;
    private String PASS;
    private String NICK;
    private String LOGIN;
    private String REAL;
    private String CHARSET;
    // thread 稼働フラグ
    private boolean running = false;
    private Socket socket = null;
    private BufferedWriter bw;
    private BufferedReader br;
    // ch指定のないテキストを格納
    private String receive = "";
    // 最後に表示されていたchannel
    private IrcChannel last_channel = null;

    private HashMap<String, IrcChannel> channels = new HashMap<String, IrcChannel>();

    public IrcHost(String setting_name, String host, boolean use_ssl, int port, String pass, String nick, String login,
            String real, String charset) {
        SETTING_NAME = setting_name;
        HOST = host;
        USE_SSL = use_ssl;
        PORT = port;
        PASS = pass;
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
            Util.d(e.getStackTrace());
        } catch (UnknownHostException e) {
            Util.d(e.getStackTrace());
        } catch (IOException e) {
            Util.d(e.getStackTrace());
        }
        running = true;
        this.start();
        if (PASS != "") {
            this.pass(PASS);
        }
        this.changeNick(NICK);
        this.user();
    }

    /**
     * socket等を閉じる
     */
    private void disconnect() {
        try {
            // 入出力ストリーム切断
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
            // ソケット切断
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Util.d(e.getStackTrace());
        }
    }

    /**
     * ホストから切断する
     */
    public void close() {
        // Ircサーバーに quit 送信
        this.quit();
        // thread 停止
        running = false;
        try {
            // thread 停止待ち
            this.join();
        } catch (InterruptedException e) {
            Util.d(e.getStackTrace());
        }
    }

    /**
     * 受信したメッセージを処理
     */
    @Override
    public void run() {
        try {
            String current = null;
            while ((current = br.readLine()) != null && running) {
                // IRCサーバからの応答を識別する
                IrcReply reply = IrcReplyParser.parse(current);
                int reply_id = reply.getId();
                String body = reply.getBody();
                String channel = reply.getChannel();
                String from = reply.getFrom();
                switch (reply_id) {
                    case IrcReplyParser.RID_PING:
                        this.pong(channel);
                        break;
                    case IrcReplyParser.RID_SYSMSG:
                        this.updateMsg("", " * " + body);
                        break;
                    case IrcReplyParser.RID_MOTD:
                        this.updateMsg("", body);
                        break;
                    case IrcReplyParser.RID_JOIN:
                        this.updateMsg(channel, " * join " + channel);
                        break;
                    case IrcReplyParser.RID_PRIVMSG:
                        this.updateMsg(channel, "<" + from + "> " + body);
                        break;
                    case IrcReplyParser.RID_NAMES:
                        this.updateMsg(channel, " * names " + body);
                        this.updateUsers(channel, body);
                        break;
                    default:
                        this.updateMsg("", current);
                        break;
                }
            }
        } catch (UnknownHostException e) {
            Util.d(e.getStackTrace());
        } catch (IOException e) {
            Util.d(e.getStackTrace());
        } finally {
            // 切断
            this.disconnect();
        }
    }

    private void updateUsers(String ch_name, String users) {
        IrcChannel ch = getChannel(ch_name);
        ch.updateUsers(users);
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

    public String getSettingName() {
        return SETTING_NAME;
    }

    /**
     * ホスト名を返す
     * @return String
     */
    public String getHostName() {
        return HOST;
    }

    public boolean getUseSSL() {
        return USE_SSL;
    }

    public String getPort() {
        return String.valueOf(PORT);
    }

    public String getPassword() {
        return PASS;
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
     * パスワード登録
     * @param password
     */
    public void pass(String password) {
        this.write("PASS " + password);
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
            Util.d(e.getStackTrace());
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
            Util.d(e.getStackTrace());
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
            jsobj.put("setting_name", SETTING_NAME);
            jsobj.put("hostname", HOST);
            jsobj.put("use_ssl", USE_SSL);
            jsobj.put("port", PORT);
            jsobj.put("pass", PASS);
            jsobj.put("nick", NICK);
            jsobj.put("login", LOGIN);
            jsobj.put("real", REAL);
            jsobj.put("charset", CHARSET);
        } catch (JSONException e) {
            Util.d(e.getStackTrace());
        }
        return jsobj;
    }
}
