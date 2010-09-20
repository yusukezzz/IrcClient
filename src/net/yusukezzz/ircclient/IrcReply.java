package net.yusukezzz.ircclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcReply {
    // 識別対象リプライ
    private String          reply          = "";
    // 識別結果の配列
    private String[]        results        = {};

    // IRCリプライの内部ID
    public static final int RID_UNKNOWN    = 300;
    public static final int RID_SYSMSG     = 1;
    public static final int RID_PING       = 2;
    public static final int RID_JOIN       = 3;
    public static final int RID_PRIVMSG    = 4;
    public static final int RID_NAMES      = 5;
    // IRCリプライの正規表現
    static final String     PTRN_SYSMSG    = " \\* :(.+)";
    static final String     PTRN_PING      = "^PING (:.+)";
    static final String     PTRN_JOIN      = "JOIN :(#.+)";
    static final String     PTRN_PRIVMSG   = ":([a-zA-Z0-9_]+?)!.+? PRIVMSG (#.+?) :(.+)";
    static final String     PTRN_NAMES     = "353.+(#.+) :(.+)";
    static final String     PTRN_END_NAMES = "366.+(#.+) :End of NAMES list";

    public IrcReply(String msg) {
        this.reply = msg;
    }

    /**
     * 正規表現にマッチしたグループの配列を返す
     *
     * @return String[] results
     */
    public String[] get() {
        return this.results;
    }

    /**
     * 正規表現チェックを行ない、リプライ内容に応じたIDを返す
     *
     * @return Integer RID
     */
    public int parse() {
        if (this.match(PTRN_SYSMSG)) {
            return RID_SYSMSG;
        }
        if (this.match(PTRN_PING, Pattern.CASE_INSENSITIVE)) {
            return RID_PING;
        }
        if (this.match(PTRN_JOIN, Pattern.CASE_INSENSITIVE)) {
            return RID_JOIN;
        }
        if (this.match(PTRN_PRIVMSG)) {
            return RID_PRIVMSG;
        }
        if (this.match(PTRN_NAMES)) {
            return RID_NAMES;
        }
        // 未登録
        return RID_UNKNOWN;
    }

    /**
     * 実際に正規表現チェックを行うメソッド
     *
     * @param String
     *            pattern
     * @return boolean true 成功時 false 失敗時
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
