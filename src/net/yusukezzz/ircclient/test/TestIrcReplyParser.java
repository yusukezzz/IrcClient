package net.yusukezzz.ircclient.test;

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

}
