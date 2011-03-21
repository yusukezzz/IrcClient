package net.yusukezzz.ircclient;

public class IrcReply {

    private int    id;
    private String ch_name;
    private String body;
    private String from;

    public IrcReply(int reply_id, String channel, String text) {
        id = reply_id;
        ch_name = channel;
        body = text;
        from = "";
    }

    public IrcReply(int id, String ch_name, String body, String from) {
        this.id = id;
        this.ch_name = ch_name;
        this.body = body;
        this.from = from;
    }

    public int getId() {
        return id;
    }

    public String getChannel() {
        return ch_name;
    }

    public String getBody() {
        return body;
    }

    public String getFrom() {
        return from;
    }
}
