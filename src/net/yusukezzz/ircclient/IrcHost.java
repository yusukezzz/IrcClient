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

public class IrcHost {
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
