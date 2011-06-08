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
import java.util.Iterator;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

/**
 * バックグラウンドで IrcServer と通信を行うクラス
 * @author yusuke
 */
public class IrcConnectionService extends Service {

    private static HashMap<String, IrcConnection> conns = null;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        IrcConnectionService getService() {
            return IrcConnectionService.this;
        }
    }

    /**
     * IrcConnrctionを追加
     * @param IrcHost
     * @return boolean
     * @throws RemoteException
     */
    public IrcConnection addHost(IrcHost host) {
        IrcConnection conn = new IrcConnection(host);
        conns.put(host.getSettingName(), conn);
        return conn;
    }

    /**
     * 指定されたIrcConnectionを停止後、削除
     * @param setting
     * @return boolean
     */
    public boolean removeHost(String setting) {
        IrcConnection conn = getConnection(setting);
        if (conn == null) { return false; }
        conn.close();
        conns.remove(setting);
        return true;
    }

    /**
     * 指定されたIrcConnectionを返す
     * @param index
     * @return IrcConnection
     */
    public IrcConnection getConnection(String setting) {
        return conns.get(setting);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        conns = new HashMap<String, IrcConnection>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onDestroy() {
        if (conns != null) {
            // 接続しているhostがあったら切断
            Iterator<?> ite = conns.entrySet().iterator();
            while (ite.hasNext()) {
                Map.Entry obj = (Map.Entry) ite.next();
                IrcConnection conn = (IrcConnection) obj.getValue();
                if (conn.isConnected()) {
                    conn.close();
                }
            }
            conns = null;
        }
        super.onDestroy();
        Toast.makeText(this, "IrcConnection has been terminated.", Toast.LENGTH_SHORT).show();
    }

    class IrcConnection extends Thread {
        private final IrcHost host;
        // ch指定のないテキストを格納
        private String receive = "";
        // 最後に表示されていたchannel
        private IrcChannel last_channel = null;
        // thread 稼働フラグ
        private boolean running = false;
        private Socket socket = null;
        private BufferedWriter bw;
        private BufferedReader br;
        private final HashMap<String, IrcChannel> channels = new HashMap<String, IrcChannel>();

        public IrcConnection(IrcHost host) {
            this.host = host;
        }

        /**
         * ホストに接続する
         */
        public void connect() {
            try {
                this.addMsg("", host.getHostName() + " connecting...");
                socket = new Socket(host.getHostName(), Integer.parseInt(host.getPort()));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    host.getCharset()));
            } catch (UnsupportedEncodingException e) {
                Util.d(e.getStackTrace());
                close();
            } catch (UnknownHostException e) {
                Util.d(e.getStackTrace());
                close();
            } catch (IOException e) {
                Util.d(e.getStackTrace());
                close();
            }
            running = true;
            this.start();
            if (host.getPassword().equals("") == false) {
                this.pass(host.getPassword());
            }
            this.changeNick(host.getNick());
            this.user();
        }

        /**
         * socket等を閉じる
         */
        private void disconnect() {
            close();
        }

        /**
         * ホストから切断する
         */
        public synchronized void close() {
            try {
                if (isInterrupted() == false) {
                    interrupt();
                }
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
                // thread 停止待ち
                this.join();
            } catch (InterruptedException e) {
                Util.d(e.getStackTrace());
            } catch (IOException e) {
                Util.d(e.getStackTrace());
            }
            br = null;
            bw = null;
            socket = null;
        }

        /**
         * 受信したメッセージを処理
         */
        @Override
        public void run() {
            try {
                String current = null;
                while (!isInterrupted()) {
                    current = br.readLine();
                    if (current != null) {
                        dispatch(current);
                    } else {
                        close();
                    }
                }
            } catch (IOException e) {
                Util.d(e.getStackTrace());
                close();
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
         * IRCサーバーからのレスポンスをIRCイベントに変換する
         * @param msg
         */
        private void dispatch(String msg) {
            Util.d(msg);
            IrcMessage m = IrcMessage.parse(msg);
//            Util.d("command:" + m.getCommand());
//            Util.d("middle:" + m.getMiddle());
//            Util.d("nick:" + m.getNick());
//            Util.d("trail:" + m.getTrailing());
            if (m == null) {
                return;
            }
            String command = m.getCommand();
            if (command.equalsIgnoreCase("PRIVMSG")) {
                IrcUser user = m.getUser();
                String html = "<div class='privmsg'>" + Util.getTime() + " (" + user.getNick() + ") " + m.getTrailing() + "</div>";
                addMsg(m.getMiddle(), html);
            } else if (command.equalsIgnoreCase("PING")) {
                String ping = m.getTrailing();
                pong(ping);
                if (running == false) {
                    running = true;
                }
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
            this.write("USER " + host.getLogin() + " " + hostname + " " + host.getHostName() + " :"
                + host.getReal());
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
            this.close();
        }

        /**
         * 指定channelに発言する
         * @param ch
         * @param str
         */
        public void privmsg(String ch, String str) {
            this.write("PRIVMSG " + ch + " :" + str);
            this.addMsg(ch, "<" + host.getNick() + "> " + str);
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
        private void addMsg(String ch, String text) {
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
         * ユーザー一覧を更新する
         * @param ch_name
         * @param users
         */
        private void updateUsers(String ch_name, String users) {
            IrcChannel ch = getChannel(ch_name);
            if (ch == null) {
                ch = this.join(ch_name);
            }
            if (ch != null) {
                ch.updateUsers(users);
            }
        }

        /**
         * 最後に表示されたchannelを返す
         * @return IrcChannel
         */
        public IrcChannel getLastChannel() {
            return last_channel;
        }

        /**
         * 受信したテキストを返す
         * @return　String
         */
        public String getRecieve() {
            return receive;
        }

        /**
         * joinしているchannelリストを返す
         * @return ArrayList<IrcChannel>
         */
        @SuppressWarnings("rawtypes")
        public ArrayList<IrcChannel> getChannels() {
            ArrayList<IrcChannel> latestChannels = new ArrayList<IrcChannel>();
            if (channels.isEmpty()) {
                return latestChannels;
            }
            Iterator<?> ite = channels.entrySet().iterator();
            while (ite.hasNext()) {
                Map.Entry obj = (Map.Entry) ite.next();
                IrcChannel ch = (IrcChannel) obj.getValue();
                latestChannels.add(ch);
            }
            return latestChannels;
        }
    }
}
