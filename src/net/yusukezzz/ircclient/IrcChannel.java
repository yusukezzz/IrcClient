package net.yusukezzz.ircclient;

import java.util.ArrayList;

public class IrcChannel {
    private String                             name;
    private ArrayList<User> users   = new ArrayList<User>();
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
            String name = nameA[i];
            boolean naruto = name.indexOf("@") == 1 ? true : false;
            User user = new User(name, naruto);
            users.add(user);
        }
        return true;
    }

    /**
     * ユーザーの人数を返す
     * @return users.size()
     */
    public Integer getUsersNum() {
        return users.size();
    }

    /**
     * users を返す
     * @return users
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * ユーザーの名前一覧をListで返す
     *
     * @return names
     */
    public ArrayList<String> getUserNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            names.add(users.get(i).getName());
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
