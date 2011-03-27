package net.yusukezzz.ircclient;

import java.util.HashMap;

import net.yusukezzz.ircclient.IrcConnectionService.IrcConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ホストの設定と接続　IrcConnection　を保持するクラス
 * @author yusuke
 *
 */
public class IrcHost {
    private IrcConnection connection = null;
    private String SETTING_NAME;
    private String HOST;
    private boolean USE_SSL;
    private int PORT;
    private String PASS;
    private String NICK;
    private String LOGIN;
    private String REAL;
    private String CHARSET;
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
    
    public void setConnection(IrcConnection conn) {
        connection = conn;
    }
    
    public IrcConnection connection() {
        return connection;
    }

    public String getSettingName() {
        return SETTING_NAME;
    }

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
