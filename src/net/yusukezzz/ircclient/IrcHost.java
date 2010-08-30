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
    private String                  HOST;
    private Integer                 PORT;
    private String                  NICK;
    private String                  LOGIN;
    private String                  CHARSET;
    private Handler                 handler;
    private BufferedWriter          bw;
    private BufferedReader          br;

    private HashMap<String, IrcChannel> channels = new HashMap<String, IrcChannel>();

    public IrcHost(String host, Integer port, String nick, String login, String charset, Handler handler) {
        this.HOST = host;
        this.PORT = port;
        this.NICK = nick;
        this.LOGIN = login;
        this.CHARSET = charset;
        this.handler = handler;
        try {
            this.sendMsg("", this.HOST + " connecting...");
            Socket irc = new Socket(this.HOST, this.PORT);
            this.bw = new BufferedWriter(new OutputStreamWriter(irc.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(irc.getInputStream(), this.CHARSET));// とりあえず文字コード決め打ち
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
    }

    @Override
    public void run() {
        try {
            // 1行ずつ処理
            String current = null;
            while ((current = this.br.readLine()) != null) {
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
        this.sendMsg(ch, "<" + this.NICK + "> " + str);
    }
    
    /**
     * 実際にbufferWriterで書き込むメソッド
     * @param cmd
     */
    private void write(String cmd) {
        try {
            this.bw.write(cmd);
            this.bw.flush();
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
        IrcHost.this.handler.sendMessage(msg);
    }
}
