package net.yusukezzz.ircclient;

interface IIrcConnection {
    boolean addHost(String hostname, String nick, String user, String real,
                String charset);
}