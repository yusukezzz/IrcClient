package net.yusukezzz.ircclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcMessage {
    private final static String MES_REGEXP = "^(:([\\w!@~\\.]+?)\\x20)?(\\w+|\\d{3})((\\x20([^\\x00\\x20\\n:]+))*)?(\\x20:([^\\x00\\n]+))?";
    private final String prefix;
    private final String command;
    private final String middle;
    private final String trailing;

    private IrcMessage(String prefix, String command, String middle, String trailing) {
        this.prefix = prefix;
        this.command = command;
        this.middle = middle;
        this.trailing = trailing;
    }

    /**
     * 正規表現チェックを行ない、IrcMessageオブジェクトを生成する
     * @param String msg
     * @return IrcMessage
     */
    public static IrcMessage parse(String msg) {
        IrcMessage reply = null;
        Matcher matcher = Pattern.compile(MES_REGEXP).matcher(msg);
        if (matcher.find()) {
            try {
                reply = new IrcMessage(matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(8));
            } catch (IllegalStateException e) {
                reply = null;
            }
        }
        return reply;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getMiddle() {
        return middle;
    }
    
    public String getTrailing() {
        return trailing;
    }
    
    public String getNick() {
        int i = prefix.indexOf("!");
        if (i != -1 || (i = prefix.indexOf("@")) != -1) {
            return prefix.substring(0, i);
        } else {
            return (prefix.length() > 0 ? prefix : "");
        }
    }
    
    public String getUsername() {
        int i = prefix.indexOf('!') + 1;
        if (i != 0) {
            int j = prefix.indexOf('@', i); 
            return prefix.substring(i, (j != -1) ? j : prefix.length()); 
        }
        return null;
    }
    
    public String getHost() {
        int i = prefix.indexOf('@') + 1;
        if (i != 0)
            return prefix.substring(i, prefix.length()); 
        return null;
    }
    
    public IrcUser getUser() {
        return new IrcUser(getNick(), getUsername(), getHost());
    }
}
