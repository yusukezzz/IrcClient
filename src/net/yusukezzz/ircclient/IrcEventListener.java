package net.yusukezzz.ircclient;

import java.util.EventListener;

public interface IrcEventListener extends EventListener {
    public void onConnected();
    public void onRegistered();
    public void onDisconnected();
    public void onError(String err);
    public void onQuit();
    public void onJoin();
    public void onPart();
    public void onMode();
    public void onTopic();
    public void onNames();
    public void onList();
    public void onInvite();
    public void onKick();
    public void onPrivmsg();
    public void onNotice();
    public void onPing();
    public void onReply();
    public void onUnknown();
}
