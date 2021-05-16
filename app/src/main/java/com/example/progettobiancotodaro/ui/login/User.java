package com.example.progettobiancotodaro.ui.login;

public class User {
    String email;
    String piva;

    public User(){

    }

    public User(String email, String piva){
        this.email = email;
        this.piva = piva;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPiva(String piva) {
        this.piva = piva;
    }

    public String getEmail() {
        return email;
    }

    public String getPiva() {
        return piva;
    }
}
