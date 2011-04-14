package net.yusukezzz.ircclient.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yusukezzz.ircclient.IrcReply;
import net.yusukezzz.ircclient.IrcReplyParser;
import junit.framework.TestCase;

public class TestIrcReplyParser extends TestCase {

    public void testParse() {
        // IrcReplyクラスのオブジェクトが返る
        assertTrue(IrcReplyParser.parse("") instanceof IrcReply);
    }

    public void testMatchUnknown() {
        String res[] = IrcReplyParser.match("hoge");
        assertTrue(Integer.parseInt(res[0]) == IrcReplyParser.RID_UNKNOWN);
    }

    public void testMatchPing() {
        String ping = "PING :yusukezzz";
        String res[] = IrcReplyParser.match(ping);
        assertTrue(Integer.parseInt(res[0]) == IrcReplyParser.RID_PING);
        assertTrue(res[1].equals(ping));
        assertTrue(res[2].equals(":yusukezzz"));
    }

    public void testMatchJoin() {
        String join = ":yusukezzz!yusukezzz.net JOIN :#test";
        String res[] = IrcReplyParser.match(join);
        assertTrue(Integer.parseInt(res[0]) == IrcReplyParser.RID_JOIN);
        assertTrue(res[1].equals(join));
        assertTrue(res[2].equals("yusukezzz"));
        assertTrue(res[3].equals("#test"));
    }

    public void testMatchPrivmsg() {
        String privmsg = ":yusukezzz!yusukezzz.net PRIVMSG #hoge :メッセージ　ほげ";
        String res[] = IrcReplyParser.match(privmsg);
        assertTrue(Integer.parseInt(res[0]) == IrcReplyParser.RID_PRIVMSG);
        assertTrue(res[1].equals(privmsg));
        assertTrue(res[2].equals("yusukezzz"));
        assertTrue(res[3].equals("#hoge"));
        assertTrue(res[4].equals("メッセージ　ほげ"));
    }

    public void testMatchNames() {
        String names = ":tiarra 353 yusukezzz @ #hoge :@yusukezzz androzzz hogeuser";
        String res[] = IrcReplyParser.match(names);
        assertTrue(Integer.parseInt(res[0]) == IrcReplyParser.RID_NAMES);
        assertTrue(res[1].equals("353 yusukezzz @ #hoge :@yusukezzz androzzz hogeuser"));
        assertTrue(res[2].equals("#hoge"));
        assertTrue(res[3].equals("@yusukezzz androzzz hogeuser"));
    }

    /**
     * New universal pattern
     */
    private final static String MES_REGEXP = "^(:([\\w!@~\\.]+?)\\x20)?(\\w+|\\d{3})((\\x20([^\\x00\\x20\\n:]+))*)?(\\x20:([^\\x00\\n]+))?";
    private final static String CHANNEL = "#yusukezzz";
    private final static String NAMES = "androzzz @yusukezzz";
    private final static String PREFIX = "yusukezzz!~yusukezzz@yusukezzz.net";
    private final static String MES_JOIN = ":" + PREFIX + " JOIN :" + CHANNEL;
    private final static String MES_NAMES = ":" + PREFIX + " 353 androzzz @ #yusukezzz :" + NAMES;
    private final static String MES_PRIVMSG = ":" + PREFIX + " PRIVMSG " + CHANNEL
        + " :my name is yusukezzz";
    private final static String MES_MODE = ":" + PREFIX + " MODE " + CHANNEL + " +o androzzz";
    private final static String MES_PING = "PING :" + PREFIX;
    public void testRegexpJoin() {
        Matcher matcher = getMatcher(MES_JOIN);
        // search
        assertTrue(matcher.find());
        // prefix
        assertTrue(matcher.group(2).equals(PREFIX));
        // command
        assertTrue(matcher.group(3).equals("JOIN"));
        // middle
        assertTrue(matcher.group(4).trim().equals(""));
        // traits
        assertTrue(matcher.group(8).equals(CHANNEL));
    }

    public void testRegexpNames() {
        Matcher matcher = getMatcher(MES_NAMES);
        // search
        assertTrue(matcher.find());
        // prefix
        assertTrue(matcher.group(2).equals(PREFIX));
        // command
        assertTrue(matcher.group(3).equals("353"));
        // middle
        assertTrue(matcher.group(4).trim().equals("androzzz @ #yusukezzz"));
        // traits
        assertTrue(matcher.group(8).equals(NAMES));
    }

    public void testRegexpPrivmsg() {
        Matcher matcher = getMatcher(MES_PRIVMSG);
        // search
        assertTrue(matcher.find());
        // prefix
        assertTrue(matcher.group(2).equals(PREFIX));
        // command
        assertTrue(matcher.group(3).equals("PRIVMSG"));
        // middle
        assertTrue(matcher.group(4).trim().equals(CHANNEL));
        // traits
        assertTrue(matcher.group(8).equals("my name is yusukezzz"));
    }
    
    public void testRegexpMode() {
        Matcher matcher = getMatcher(MES_MODE);
        // search
        assertTrue(matcher.find());
        // prefix
        assertTrue(matcher.group(2).equals(PREFIX));
        // command
        assertTrue(matcher.group(3).equals("MODE"));
        // middle
        assertTrue(matcher.group(4).trim().equals(CHANNEL + " +o androzzz"));
        // traits
        assertTrue(matcher.group(8) == null);
    }
    
    public void testRegexpPing() {
        Matcher matcher = getMatcher(MES_PING);
        // search
        assertTrue(matcher.find());
        // prefix
        assertTrue(matcher.group(2) == null);
        // command
        assertTrue(matcher.group(3).equals("PING"));
        // middle
        assertTrue(matcher.group(4).trim().equals(""));
        // traits
        assertTrue(matcher.group(8).equals(PREFIX));
    }

    private Matcher getMatcher(String input) {
        return Pattern.compile(MES_REGEXP).matcher(input);
    }

}
