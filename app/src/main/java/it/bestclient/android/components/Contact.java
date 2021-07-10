package it.bestclient.android.components;

public class Contact implements Comparable<Contact>{
    private String name;
    private String phone;
    //Bitmap image = null;


    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    public Contact() {
        this.name = "";
        this.phone = "";
    }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int compareTo(Contact o) {
        if(this.getName().compareTo(o.getName()) == 0)
            return this.getPhone().compareTo(o.getPhone());

        return this.getName().compareTo(o.getName());
    }
}