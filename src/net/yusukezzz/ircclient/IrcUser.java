package net.yusukezzz.ircclient;

public class IrcUser {
    private String nick;
    private final String username;
    private final String host;
    private boolean naruto = false;

    public IrcUser(String nick, String username, String host) {
        this.nick = nick;
        this.username = username;
        this.host = host;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getHost() {
        return host;
    }

    public boolean getNaruto() {
        return naruto;
    }

    public void setNaruto(boolean naruto) {
        this.naruto = naruto;
    }
}
