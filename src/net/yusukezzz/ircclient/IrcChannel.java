package net.yusukezzz.ircclient;

import java.util.ArrayList;
import java.util.HashMap;

public class IrcChannel {
    private String                             name;
    private ArrayList<HashMap<String, String>> users   = new ArrayList<HashMap<String, String>>();
    private String                             recieve = "";

    public IrcChannel(String ch) {
        this.setName(ch);
    }

    /**
     * チャンネルに所属するユーザーリストを更新
     * 
     * @param names
     * @return bool
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

    /**
     * ユーザーの人数を返す
     * @return users.size
     */
    public Integer getUsersNum() {
        return users.size();
    }

    /**
     * ユーザーの名前一覧をListで返す
     * 
     * @return names
     */
    public ArrayList<String> getUserNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            names.add(users.get(i).get("name"));
        }
        return names;
    }

    /**
     * チャンネルの名前を設定する
     * 
     * @param channel name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * チャンネルの名前を返す
     * 
     * @return channel name
     */
    public String getName() {
        return name;
    }

    /**
     * 受信テキストに追加する
     * 
     * @param line
     */
    public void addRecieve(String line) {
        recieve += line;
    }

    /**
     * 受信テキストを返す
     * 
     * @return recieve
     */
    public String getRecieve() {
        return recieve;
    }
}
