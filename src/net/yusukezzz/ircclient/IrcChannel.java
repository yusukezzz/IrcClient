package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.HashMap;

public class IrcChannel {
    private String name;
    private ArrayList<HashMap<String, String>> users = new ArrayList<HashMap<String, String>>();

    public IrcChannel(String ch) {
        this.setName(ch);
    }

    /**
     * チャンネルに所属するユーザーリストを更新
     * @param names
     * @return
     */
    public boolean updateUserList(String names) {
        // userリストを初期化
        users.clear();
        String nameA[] = names.split(" ");
        for (int i = 0; i < nameA.length; i++) {
            // なるとチェック
            HashMap<String, String> user = new HashMap<String, String>();
            String u = nameA[i];
            String naruto = u.indexOf("@") == 1 ? "yes" : "no";
            user.put("naruto", naruto);
            user.put("name", u);
            users.add(user);
        }
        return true;
    }

    public Integer getUsersNum() {
        return users.size();
    }

    public ArrayList<String> getUserNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i=0; i<users.size(); i++) {
            names.add(users.get(i).get("name"));
        }
        return names;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
