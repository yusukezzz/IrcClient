package net.yusukezzz.ircclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IrcHostの受信したメッセージを正規表現で分解しIrcReplyオブジェクトに変換するクラス
 * @author yusuke
 */
public class IrcReplyParser {
    // IRCリプライの内部IDメッセージ
    public static final int RID_UNKNOWN = -1;
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
        patterns = new String[]
            {
                " \\* :(.+)", // RID_SYSMSG
                "^PING (:.+)", // RID_PING
                ":([a-zA-Z0-9_]+?)!.+? JOIN :(#.+)", // RID_JOIN
                ":([a-zA-Z0-9_]+?)!.+? PRIVMSG (#.+?) :(.+)", // RID_PRIVMSG
                "353.+(#.+) :(.+)", // RID_NAMES
                "372 .+ :-(.+)", // RID_MOTD
                "376 .+ :End", // RID_END_MOTD
            };
    }

    /**
     * 正規表現チェックを行ない、IrcReplyオブジェクトを生成する
     * @param String msg
     * @return IrcReply reply
     */
    public static IrcReply parse(String msg) {
        IrcReply reply = null;
        String results[] = match(msg);
        int id = RID_UNKNOWN;
        try {
            id = Integer.parseInt(results[0]);
            switch (id) {
                case RID_SYSMSG:
                    reply = new IrcReply(id, "", results[2]);
                    break;
                case RID_PING:
                    reply = new IrcReply(id, results[2], "");
                    break;
                case RID_JOIN:
                    reply = new IrcReply(id, results[2], results[1], results[3]);
                    break;
                case RID_PRIVMSG:
                    reply = new IrcReply(id, results[3], results[4], results[2]);
                    break;
                case RID_NAMES:
                    reply = new IrcReply(id, results[2], results[3]);
                    break;
                case RID_MOTD:
                    reply = new IrcReply(id, "", results[2]);
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
     * @return String[] 0 = replyId, 1~ matches
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
