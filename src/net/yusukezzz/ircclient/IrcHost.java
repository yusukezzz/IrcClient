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
import android.os.Handler;
import android.os.Message;

public class IrcHost extends Thread {
    private String                      HOST;
    private Integer                     PORT;
    private String                      NICK;
    private String                      LOGIN;
    private String                      CHARSET;
    private Socket                      socket   = null;
    private Handler                     handler;
    private BufferedWriter              bw;
    private BufferedReader              br;

    private HashMap<String, IrcChannel> channels = new HashMap<String, IrcChannel>();

    public IrcHost(String host, Integer port, String nick, String login, String charset) {
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
            this.sendMsg("", this.HOST + " connecting...");
            socket = new Socket(this.HOST, this.PORT);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
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
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            // 1行ずつ処理
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
                            this.sendMsg("", " * " + res[1]);
                            break;
                        case IrcReply.RID_JOIN:
                            this.sendMsg(res[1], " * join " + res[1]);
                            break;
                        case IrcReply.RID_PRIVMSG:
                            this.sendMsg(res[2], "<" + res[1] + "> " + res[3]);
                            break;
                        case IrcReply.RID_NAMES:
                            this.sendMsg(res[1], " * names " + res[2]);
                            break;
                        default:
                            // this.sendMsg("", current);
                            break;
                    }
                } catch (IndexOutOfBoundsException e) {
                    this.sendMsg("", "[Err]" + e.getMessage());
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接続の状態を返す
     *
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
     *
     * @return String
     */
    public String getHostName() {
        return HOST;
    }

    /**
     * ping に返信
     *
     * @param daemon
     */
    public void pong(String daemon) {
        this.write("PONG " + daemon + "\n");
    }

    /**
     * ニックネームを変更する
     *
     * @param nick
     */
    public void nick(String nick) {
        this.write("NICK " + nick + "\n");
    }

    /**
     * ircサーバにユーザー情報を登録する
     *
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
        }
        this.write("USER " + NICK + " " + hostname + " " + HOST + " " + LOGIN + "\n");
    }

    /**
     * 指定channelに参加する
     *
     * @param ch
     */
    public void join(String ch) {
        this.write("JOIN " + ch + "\n");
        // チャンネルの追加
        channels.put(ch, new IrcChannel(ch));
        // メンバーの取得
        // this.names(ch);
    }

    /**
     * ユーザーリストを要求する
     *
     * @param ch
     */
    public void names(String ch) {
        this.write("NAMES " + ch + "\n");
    }

    /**
     * 指定channelに発言する
     *
     * @param ch
     * @param str
     */
    public void privmsg(String ch, String str) {
        this.write("PRIVMSG " + ch + " " + str + "\n");
        this.sendMsg(ch, "<" + NICK + "> " + str);
    }

    /**
     * 実際にbufferWriterで書き込むメソッド
     *
     * @param cmd
     */
    private void write(String cmd) {
        try {
            bw.write(cmd);
            bw.flush();
        } catch (IOException e) {
            // TODO: handle exception
        }
    }

    /**
     * handlerにテキストを送る
     *
     * @param ch
     * @param text
     */
    private void sendMsg(String ch, String text) {
        Message msg;
        msg = new Message();
        msg.obj = text;
        msg.what = 0;
        handler.sendMessage(msg);
    }
}
