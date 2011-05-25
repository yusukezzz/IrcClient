package net.yusukezzz.ircclient;

import java.util.ArrayList;

public class IrcChannel {
    private final ArrayList<IrcUser> users = new ArrayList<IrcUser>();
    private String ch_name;
    private String recieve = "";

    public IrcChannel(String ch) {
        this.setName(ch);
    }

    /**
     * チャンネルに所属するユーザーリストを更新
     * @param names
     * @return bool
     */
    public void updateUsers(String names) {
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
    public ArrayList<IrcUser> getUsers() {
        return users;
    }

    /**
     * ユーザーの名前一覧をListで返す
     * @return names
     */
    public ArrayList<String> getUserNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            names.add(users.get(i).getNick());
        }
        return names;
    }

    /**
     * チャンネルの名前を設定する
     * @param channel name
     */
    public void setName(String ch_name) {
        this.ch_name = ch_name;
    }

    /**
     * チャンネルの名前を返す
     * @return channel name
     */
    public String getName() {
        return ch_name;
    }

    /**
     * 受信テキストに追加する
     * @param line
     */
    public void addRecieve(String line) {
        recieve += line;
    }

    /**
     * 受信テキストを返す
     * @return recieve
     */
    public String getRecieve() {
        return recieve;
    }
}
