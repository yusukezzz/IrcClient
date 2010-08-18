package net.yusukezzz.ircviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;

public class Irc extends Thread {
    private String         HOST;
    private Integer        PORT;
    private Handler        handler;
    private BufferedWriter bw;
    private BufferedReader br;

    public Irc(String host, Integer port, Handler handler) {
        this.HOST = host;
        this.PORT = port;
        this.handler = handler;
        try {
            this.sendMsg("", this.HOST + " connecting...");
            Socket irc = new Socket(this.HOST, this.PORT);
            this.bw = new BufferedWriter(new OutputStreamWriter(
                    irc.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(
                    irc.getInputStream(), "ISO-2022-JP"));// とりあえず文字コード決め打ち
        } catch (UnsupportedEncodingException e) {
            // TODO: handle exception
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
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
                // PING PONG
                Pattern pingRegex = Pattern.compile("^PING (:.+)",
                        Pattern.CASE_INSENSITIVE);
                Matcher ping = pingRegex.matcher(current);
                if (ping.find()) {
                    this.pong(ping.group(1));
                }
                // JOIN の表示
                Pattern joinRegex = Pattern.compile("JOIN :(#.+)",
                        Pattern.CASE_INSENSITIVE);
                Matcher join = joinRegex.matcher(current);
                if (join.find()) {
                    this.sendMsg(join.group(1), "*join " + join.group(1));
                }
                // PRIVMSG のテキストをhandlerへ
                Pattern pmsgRegex = Pattern
                        .compile(":([a-zA-Z0-9_]+?)!.+? PRIVMSG (#.+?) :(.+)");
                Matcher pmsg = pmsgRegex.matcher(current);
                if (pmsg.find()) {
                    String text = "<" + pmsg.group(1) + "> " + pmsg.group(3);
                    this.sendMsg(pmsg.group(2), text);
                } else {
                    this.sendMsg("", current);
                }
            }
        } catch (UnknownHostException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * ping に返信
     * @param String daemon
     */
    public void pong(String daemon) {
        try {
            this.bw.write("PONG " + daemon + "\n");
            this.bw.flush();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * ニックネームを変更する
     * @param nick
     */
    public void nick(String nick) {
        try {
            this.bw.write("NICK " + nick + "\n");
            this.bw.flush();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * ircサーバにユーザー情報を登録する
     * @param user
     * @param hostname
     * @param server
     * @param realname
     */
    public void user(String user, String hostname, String server, String realname) {
        try {
            this.bw.write("USER " + user + " " + hostname + " " + server + " " + realname + "\n");
            this.bw.flush();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * 指定channelに参加する
     * @param ch
     */
    public void join(String ch) {
        try {
            this.bw.write("JOIN " + ch + "\n");
            this.bw.flush();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * 指定channelに発言する
     * @param ch
     * @param msg
     */
    public void privmsg(String ch, String msg) {
        try {
            this.bw.write("PRIVMSG " + ch + " " + msg + "\n");
            this.bw.flush();
            this.sendMsg(ch, msg);
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * 描画threadにテキストを送る
     * @param ch
     * @param text
     */
    private void sendMsg(String ch, String text) {
        Message msg;
        msg = new Message();
//        HashMap<String, String> chat = new HashMap<String, String>();
//        chat.put("ch", ch);
//        chat.put("text", text);
        msg.obj = text;
        msg.what = 0;
        Irc.this.handler.sendMessage(msg);
    }

}
