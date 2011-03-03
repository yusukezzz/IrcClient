package net.yusukezzz.ircclient;

public class User {
    private String name;
    private boolean naruto = false;

    public User(String name, boolean naruto) {
        this.name = name;
        this.naruto = naruto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getNaruto() {
        return naruto;
    }

    public void setNaruto(boolean naruto) {
        this.naruto = naruto;
    }

}
