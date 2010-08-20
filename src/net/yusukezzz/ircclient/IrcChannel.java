package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.HashMap;

public class IrcChannel {
    private String name;
    private ArrayList<HashMap<String, String>> members;
    
    public IrcChannel(String ch) {
        this.setName(ch);
    }
    
    /**
     * チャンネルに所属するユーザーリストを更新
     * @param names
     * @return
     */
    public boolean updateUserList(String names) {
        String nameA[] = names.split(" ");
        for (int i = 0; i < nameA.length; i++) {
            HashMap<String, String> member = null;
            // なるとチェック
            String naruto = nameA[i].indexOf("@") == 1 ? "yes" : "no";
            member.put("naruto", naruto);
            member.put("name", nameA[i]);
            members.add(member);
        }
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
