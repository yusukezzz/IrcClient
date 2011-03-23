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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

public class IrcConnectionService extends Service {

    private static ArrayList<IrcConnection> conns = null;
    private IIrcConnectionService.Stub binder = new IIrcConnectionService.Stub() {
        /**
         * IrcConnectionを生成、IDを返す
         */
        public int addHost(String setting, String host, int port, String pass, String nick, String login, String real,
                String charset) throws RemoteException {
            IrcConnection con = new IrcConnection(setting, host, false, port, pass, nick, login, real, charset);
            conns.add(con);
            int id = conns.indexOf(con);
            return id;
        }
        /**
         * IrcConnectionの接続を開始する
         * @param id
         * @return boolean
         * @throws RemoteException
         */
        public boolean connectHost(int id) throws RemoteException {
            IrcConnection conn = null;
            try {
                conn = conns.get(id);
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
            conn.connect();
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IrcConnectionService.class.getName().equals(intent.getAction())) {
            return binder;
        }
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(this, "IrcConnection has been started.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "IrcConnection has been terminated.", Toast.LENGTH_SHORT).show();
        if (conns != null) {
            // 接続しているhostがあったら切断
            for (IrcConnection conn : conns) {
                if (conn.isConnected()) {
                    conn.close();
                }
            }
            conns = null;
        }
        super.onDestroy();
    }

    class IrcConnection extends Thread {
        private String SETTING_NAME;
        private String HOST;
        private boolean USE_SSL;
        private int PORT;
        private String PASS;
        private String NICK;
        private String LOGIN;
        private String REAL;
        private String CHARSET;
        // ch指定のないテキストを格納
        private String receive = "";
        // 最後に表示されていたchannel
        private IrcChannel last_channel = null;
        // thread 稼働フラグ
        private boolean running = false;
        private Socket socket = null;
        private BufferedWriter bw;
        private BufferedReader br;
        private HashMap<String, IrcChannel> channels = new HashMap<String, IrcChannel>();
        
        public IrcConnection(String setting, String host, boolean use_ssl, int port, String pass, String nick, String login,
                String real, String charset) {
            SETTING_NAME = setting;
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
         * 指定したチャンネルのオブジェクトを返す
         * @param name
         * @return IrcChannel
         */
        public IrcChannel getChannel(String name) {
            return channels.get(name);
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

        private void updateUsers(String ch_name, String users) {
            IrcChannel ch = getChannel(ch_name);
            ch.updateUsers(users);
        }
    }
}
