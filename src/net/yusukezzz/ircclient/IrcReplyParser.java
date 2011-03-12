package net.yusukezzz.ircclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IrcHostの受信したメッセージを正規表現で分解しIrcReplyオブジェクトに変換するクラス
 * @author yusuke
 */
public class IrcReplyParser {
    // 識別対象リプライ
    private String          reply         = "";
    // 識別結果の配列
    private String[]        results       = {};

    // IRCリプライの内部ID
    public static final int RID_UNKNOWN   = 0;
    public static final int RID_SYSMSG    = 1;
    public static final int RID_PING      = 2;
    public static final int RID_JOIN      = 3;
    public static final int RID_PRIVMSG   = 4;
    public static final int RID_NAMES     = 5;
    public static final int RID_MOTD      = 6;
    public static final int RID_END_MOTD  = 7;
    // IRCリプライの正規表現
    static final String     PTRN_SYSMSG   = " \\* :(.+)";
    static final String     PTRN_PING     = "^PING (:.+)";
    static final String     PTRN_JOIN     = "JOIN :(#.+)";
    static final String     PTRN_PRIVMSG  = ":([a-zA-Z0-9_]+?)!.+? PRIVMSG (#.+?) :(.+)";
    static final String     PTRN_NAMES    = "353.+(#.+) :(.+)";
    static final String     PTRN_MOTD     = "372 .+ :-(.+)";
    static final String     PTRN_END_MOTD = "376 .+ :End";

    public IrcReplyParser(String msg) {
        this.reply = msg;
    }

    /**
     * 正規表現にマッチしたグループの配列を返す
     * @return String[] results
     */
    public String[] get() {
        return this.results;
    }

    /**
     * 正規表現チェックを行ない、IrcReplyオブジェクトを生成する
     * @return Integer RID
     */
    public IrcReply parse() {
        IrcReply rep = null;
        if (this.match(PTRN_SYSMSG)) {
            rep = new IrcReply(RID_SYSMSG, "", this.results[1]);
        }
        if (this.match(PTRN_PING, Pattern.CASE_INSENSITIVE)) {
            rep = new IrcReply(RID_PING, this.results[1], "");
        }
        if (this.match(PTRN_JOIN, Pattern.CASE_INSENSITIVE)) {
            rep = new IrcReply(RID_JOIN, this.results[1], "");
        }
        if (this.match(PTRN_PRIVMSG)) {
            rep = new IrcReply(RID_PRIVMSG, this.results[2], this.results[3], this.results[3]);
        }
        if (this.match(PTRN_NAMES)) {
            rep = new IrcReply(RID_NAMES, this.results[1], this.results[2]);
        }
        if (this.match(PTRN_MOTD)) {
            rep = new IrcReply(RID_MOTD, "", this.results[1]);
        }
        if (this.match(PTRN_END_MOTD)) {
        }
        // 不明
        if (rep == null) {
            rep = new IrcReply(RID_UNKNOWN, "", "");
        }
        return rep;
    }

    /**
     * 実際に正規表現チェックを行うメソッド
     * @param String pattern
     * @return boolean
     */
    public boolean match(String pattern) {
        return this.match(pattern, 0);
    }

    public boolean match(String pattern, int option) {
        // 正規表現でIRCサーバからの返信をチェック
        Pattern regex = (option == 0) ? Pattern.compile(pattern) : Pattern.compile(pattern, option);
        Matcher matcher = regex.matcher(this.reply);
        // マッチしたら
        if (matcher.find()) {
            // 配列を初期化
            int groups = matcher.groupCount() + 1;
            this.results = new String[groups];
            // ヒットしたグループを詰める
            for (int i = 0; i < groups; i++) {
                this.results[i] = matcher.group(i);
            }
            return true;
        }
        return false;
    }
}
