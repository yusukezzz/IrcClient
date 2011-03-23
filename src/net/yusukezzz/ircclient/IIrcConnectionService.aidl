package net.yusukezzz.ircclient;

interface IIrcConnectionService {
    int addHost(String setting, String host, int port, String pass, String nick, String login, String real, String charset);
    boolean connectHost(int id);
}