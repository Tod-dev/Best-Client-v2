package com.example.progettobiancotodaro.ui.login;

public class User {
    String email;
    String password;
    String piva;

    public User(){

    }

    public User(String email, String password, String piva){
        this.email = email;
        this.password = password;
        this.piva = piva;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPiva(String piva) {
        this.piva = piva;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPiva() {
        return piva;
    }
}
