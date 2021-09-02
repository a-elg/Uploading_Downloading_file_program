import java.io.Serializable;

public class User implements Serializable{
    String Name,Password;
    public User(String name,String pswd){
        this.Name=name;
        this.Password=pswd;
    }
    public String getName() {
        return Name;
    }
    public String getPassword() {
        return Password;
    }
}