package net.yusukezzz.ircclient;

interface IIrcConnectionService {
    boolean addHost(String setting, String host, int port, String pass, String nick, String login, String real, String charset);
    boolean connectHost(String setting);
}