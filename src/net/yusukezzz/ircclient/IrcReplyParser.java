package net.yusukezzz.ircclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IrcHostの受信したメッセージを正規表現で分解しIrcReplyオブジェクトに変換するクラス
 * @author yusuke
 */
public class IrcReplyParser {
    // IRCリプライの内部ID
    public static final int RID_UNKNOWN = 255;
    public static final int RID_SYSMSG = 0;
    public static final int RID_PING = 1;
    public static final int RID_JOIN = 2;
    public static final int RID_PRIVMSG = 3;
    public static final int RID_NAMES = 4;
    public static final int RID_MOTD = 5;
    public static final int RID_END_MOTD = 6;

    // IRCリプライの正規表現
    static String[] patterns;
    static {
        patterns = new String[] {
                " \\* :(.+)", "^PING (:.+)", "JOIN :(#.+)", ":([a-zA-Z0-9_]+?)!.+? PRIVMSG (#.+?) :(.+)",
                "353.+(#.+) :(.+)", "372 .+ :-(.+)", "376 .+ :End",
        };
    }

    public IrcReplyParser() {
    }

    /**
     * 正規表現チェックを行ない、IrcReplyオブジェクトを生成する
     * @param String msg
     * @return IrcReply reply
     */
    public static IrcReply parse(String msg) {
        IrcReply reply = null;
        String results[] = match(msg);
        int replyId = RID_UNKNOWN;
        try {
            replyId = Integer.parseInt(results[0]);
            switch (replyId) {
                case RID_SYSMSG:
                    reply = new IrcReply(RID_SYSMSG, "", results[2]);
                    break;
                case RID_PING:
                    reply = new IrcReply(RID_PING, results[2], "");
                    break;
                case RID_JOIN:
                    reply = new IrcReply(RID_JOIN, results[2], "");
                    break;
                case RID_PRIVMSG:
                    reply = new IrcReply(RID_PRIVMSG, results[3], results[4], results[2]);
                    break;
                case RID_NAMES:
                    reply = new IrcReply(RID_NAMES, results[2], results[3]);
                    break;
                case RID_MOTD:
                    reply = new IrcReply(RID_MOTD, "", results[2]);
                    break;
                case RID_END_MOTD:
                    break;
                default:
                    reply = null;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            reply = null;
        }
        // 不明
        if (reply == null) {
            reply = new IrcReply(RID_UNKNOWN, "", "");
        }
        return reply;
    }

    /**
     * 実際に正規表現チェックを行うメソッド matchしたグループを配列で返す
     * @param String pattern
     * @return String[]
     */
    public static String[] match(String msg) {
        int replyId = 0;
        String[] results = null;
        // 正規表現の数だけチェック
        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(msg);
            // マッチしたら
            if (matcher.find()) {
                // 配列を初期化 replyId の分で+1
                int groups = matcher.groupCount() + 2;
                results = new String[groups];
                results[0] = replyId + "";
                // ヒットしたグループを詰める
                for (int i = 1; i < groups; i++) {
                    results[i] = matcher.group(i - 1);
                }
                break;
            }
            replyId++;
        }
        if (results == null) {
            results = new String[1];
            results[0] = RID_UNKNOWN + "";
        }
        return results;
    }
}
