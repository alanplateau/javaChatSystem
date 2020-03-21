package server;

//用户昵称+密码
public class UserPass {
    String NickName;
    String Password;
    public String getNickName() {
        return NickName;
    }
    public String getPassword() {
            return Password;
    }
    public UserPass(String NickName,String Password){
        this.NickName=NickName;
        this.Password=Password;
    }

}
