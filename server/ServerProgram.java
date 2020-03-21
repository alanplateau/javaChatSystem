package server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ServerProgram {
    JLabel serverport;//服务器端口
    JTextField inputport;//输入端口
    JButton conne, disconne;//建立服务器，中止服务器
    JLabel MemberList;//好友列表
    JList Membersum;
    JTextArea showMessage;//聊天记录
    JLabel block;//分割两个文本框
    JTextArea inputMessage;//输入消息

    ServerSocket server;
    ServerTherad serverTherad1;//服务器进程
    TreeMap<String, ClientTherad> ClientTherads;

    TreeMap<String, UserPass> users;//运用文件里的账户+昵称+密码建立TreeMap
    BufferedReader readAccount;//读取文件

    DefaultListModel<String> MemberOnline;//用来向好友列表动态添加删除元素


    public ServerProgram() {
        CreatForm();//创建窗口

        ClientTherads = new TreeMap<String, ClientTherad>();
        //建立用户TreeMap
        try {
            users = new TreeMap<String, UserPass>();
            readAccount = new BufferedReader(new FileReader
                    ("src/server/userdata.txt"));
            while (readAccount.ready()) {
                String temp[] = readAccount.readLine().split("\\s+");
                UserPass tempUser = new UserPass(temp[1], temp[2]);
                users.put(temp[0], tempUser);
            }
            readAccount.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CreatForm() {
        JFrame fram = new JFrame("服务器界面");
        Container c = fram.getContentPane();
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        fram.setLayout(gl);

        //设置windows界面风格
        try {
            String lfClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            UIManager.setLookAndFeel(lfClassName);
            SwingUtilities.updateComponentTreeUI(fram);
        } catch (Exception e) {e.printStackTrace();}


        serverport = new JLabel("服务器端口");
        inputport = new JTextField(8);
        inputport.setText("8088");
        conne = new JButton("开始");
        disconne = new JButton("中止");
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gl.setConstraints(serverport, gc);
        c.add(serverport);
        gl.setConstraints(inputport, gc);
        c.add(inputport);
        gl.setConstraints(conne, gc);
        c.add(conne);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gl.setConstraints(disconne, gc);
        c.add(disconne);

        MemberList = new JLabel("好友列表");
        MemberOnline=new DefaultListModel<String>();
        Membersum = new JList(MemberOnline);
        JScrollPane jsp=new JScrollPane(Membersum);
        JPanel panetemp = new JPanel();
        panetemp.setOpaque(false);
        GridBagLayout gltemp = new GridBagLayout();
        GridBagConstraints gctemp = new GridBagConstraints();
        panetemp.setLayout(gltemp);
        gctemp.fill = GridBagConstraints.BOTH;
        gctemp.gridwidth = GridBagConstraints.REMAINDER;
        gctemp.gridheight = 1;
        gltemp.setConstraints(MemberList, gctemp);
        panetemp.add(MemberList);
        gctemp.gridheight = 12;
        gctemp.weighty = 1.0;
        gltemp.setConstraints(jsp, gctemp);
        panetemp.add(jsp);

        gc.gridwidth = 1;
        gc.weightx=1.5;
        gc.gridheight = 13;
        gc.weighty = 1.0;
        gl.setConstraints(panetemp, gc);
        c.add(panetemp);

        showMessage = new JTextArea(7,20);
        JScrollPane temproll=new JScrollPane(showMessage);
        temproll.setBorder(new TitledBorder("日志表"));
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridheight = GridBagConstraints.REMAINDER;
        gc.weightx = 3.0;
        gc.weighty =1.0;
        gl.setConstraints(temproll, gc);
        c.add(temproll);


        fram.setSize(500, 600);
        //设置中间显示
        Toolkit tk = fram.getToolkit();
        Dimension dm = tk.getScreenSize();
        fram.setLocation((int) (dm.getWidth() - fram.getWidth()) / 2,
                (int) (dm.getHeight() - fram.getHeight()) / 2);
        fram.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fram.setVisible(true);
        //设置监听器
        addactionlistenr();
    }

    //在日志表上输出日志
    public void writeLog(String infomation) {
        showMessage.setText(showMessage.getText() + "\r\n" + infomation);
    }

    //为各个按钮添加监听器
    private void addactionlistenr() {
        //创建服务器
        conne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Creatserver();
            }
        });
        //关闭服务器
        disconne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverTherad1.Closeserver();
            }
        });
    }

    //新建服务器
    private void Creatserver() {
        int port = Integer.parseInt(inputport.getText());
        try {
            server = new ServerSocket(port);
            serverTherad1 = new ServerTherad();
            writeLog("成功建立服务器");
            MemberOnline.addElement("所有人");
        } catch (IOException e) {
            e.printStackTrace();
            writeLog("建立服务器失败");
        } catch (Exception e) {
            e.printStackTrace();
            writeLog("建立服务器失败");
        }

    }

    //服务器线程
    class ServerTherad implements Runnable {
        boolean isRunning;

        public ServerTherad() {
            isRunning=true;
            new Thread(this).start();
        }

        public void  Closeserver(){
            try {
                isRunning = false;
                server.close();
                writeLog("server:服务器关闭成功");
            }
            catch (IOException e){
                e.printStackTrace();
                writeLog("server:服务器关闭失败");
            }
        }

        public  void run() {
            while (isRunning) {
                if (!server.isClosed()) {
                    try {
                        Socket tempsocket = server.accept();
                        ClientTherad tempclient = new ClientTherad(tempsocket);
                        if(tempclient.getisRunning()!=false) {
                            String tempname = new String(tempclient.getUseraccount());
                            ClientTherads.put(tempname, tempclient);
                        }
                    }
                    catch (IOException e) { e.printStackTrace(); }
                }
            }
        }
    }


    //客户端线程
    class ClientTherad implements Runnable {
        String Useraccount;
        String UserNickName;
        Socket socketTarget;//客户端socket
        BufferedReader inputwords;//客户端输入流
        BufferedWriter outputwords;//客户端输出流
        BufferedWriter appendAccount;//将新追加的账户密码添入文件中


        boolean isRunning;//用来判断是否在运行

        public ClientTherad(Socket target) {
            this.socketTarget = target;
            isRunning=false;
            initial();
            if(isRunning==true)
            new Thread(this).start();
        }

        public synchronized void initial() {
            try {
                inputwords = new BufferedReader(new InputStreamReader(socketTarget.getInputStream()));
                outputwords = new BufferedWriter(new OutputStreamWriter(socketTarget.getOutputStream()));
                String inputstr = inputwords.readLine();
                PartitionString resultMessage = new PartitionString(inputstr, "\\|");
                String MessageType = resultMessage.nextpart();
                switch (MessageType) {
                    //登录（消息格式为Login|用户名|密码）
                    case "Login": {
                        String account1 = resultMessage.nextpart();
                        String password1 = resultMessage.nextpart();
                        //账号密码正确且没有重复登录
                        if (users.get(account1) != null &&
                                users.get(account1).getPassword().equals(password1)
                        &&ClientTherads.get(account1)==null) {
                            Useraccount=account1;
                            UserNickName=users.get(Useraccount).getNickName();
                            sendMessage("LoginAnswer|true|"+UserNickName);
                            isRunning = true;
                            writeLog("client:"+UserNickName+"登录");
                            Broadcast("Login|"+UserNickName);
                            MemberOnline.addElement(UserNickName);
                            //将以上线用户信息发给新登录用户
                            Iterator<String> it=ClientTherads.keySet().iterator();
                            while(it.hasNext()){
                                sendMessage("Login|"+users.get(it.next()).getNickName());
                            }
                        }
                        else {
                            if(users.get(account1) != null)
                                writeLog(users.get(account1).getPassword());
                            sendMessage("LoginAnswer|false");
                            socketTarget.close();
                            writeLog("client:"+account1 + "登录失败");
                        }
                        break;
                    }
                    //注册并登录(消息格式为Register|用户名|昵称|密码）
                    case "Register": {
                        writeLog("server:有用户注册");
                        String account2 = resultMessage.nextpart();
                        String NickName2 = resultMessage.nextpart();
                        String password2 = resultMessage.nextpart();
                        //未被注册过
                        if (users.get(account2) == null) {
                            UserPass userpass2 = new UserPass(NickName2, password2);
                            users.put(account2, userpass2);
                            AddUserToFile(account2,NickName2,password2);
                            sendMessage("RegisterAnswer|true|"+NickName2);
                            writeLog(NickName2+ "登录成功");
                            Broadcast("Login|"+NickName2);
                            Useraccount=account2;
                            UserNickName=NickName2;
                            isRunning = true;
                            MemberOnline.addElement(UserNickName);
                            //将以上线用户信息发给新登录用户
                            Iterator<String> it=ClientTherads.keySet().iterator();
                            while(it.hasNext()){
                                sendMessage("Login|"+users.get(it.next()).getNickName());
                            }

                        } else {
                            sendMessage("RegisterAnswer|false|"+NickName2);
                            isRunning = false;
                            socketTarget.close();
                            writeLog(NickName2 + "注册失败");
                        }
                        break;
                    }
                    default: {
                        writeLog(MessageType+"\r\n"+"Server:首次接受消息不为Login或Register"+inputstr);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public synchronized void run() {
            while(isRunning) {
                if (!socketTarget.isClosed()) {
                    try {
                        String inputstr2 = inputwords.readLine();
                        PartitionString splitinput2 = new PartitionString(inputstr2, "\\|");
                        String MessageType2 = splitinput2.nextpart();
                        switch (MessageType2) {
                            //消息（格式为Message|To|from|消息内容
                            case "Message": {
                                //广播，转发给所有人
                                String accept = splitinput2.nextpart();
                                if (accept.equals("ALL")) {
                                    Broadcast(inputstr2);
                                    writeLog("已经将来自" + splitinput2.nextpart() + "的消息转发给所有人");
                                } else {
                                    if (JudgeNick(accept)!=null) {
                                        ClientTherads.get(JudgeNick(accept)).sendMessage(inputstr2);
                                        writeLog("已经将来自" + accept +
                                                "的消息转发给" + splitinput2.nextpart());
                                    } else
                                        writeLog("不存在" + accept + "用户，或其已下线");
                                }
                                break;
                            }
                            //登出格式为Logout|账户
                            case "Logout": {
                                isRunning = false;
                                socketTarget.close();
                                writeLog(UserNickName + "登出");
                                ClientTherads.remove(Useraccount);
                                RemoveElement(UserNickName);
                                Broadcast(inputstr2);
                                break;
                            }
                            default: {
                                writeLog("server:接受消息格式错误");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


        }

        public boolean getisRunning() {
            return isRunning;
        }

        //向客户端发送消息
        public void sendMessage(String target) {
            try {
                outputwords.write(target);
                outputwords.newLine();
                outputwords.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //广播
        public void Broadcast(String Message){
            Iterator<String> it=ClientTherads.keySet().iterator();
            while(it.hasNext()){
                ClientTherads.get(it.next()).sendMessage(Message);
            }
        }
        //查找是否有NickName对应的账号，有则返回账号，无则返回NULL
        public String JudgeNick(String Nickname3){
            Iterator<String> it=ClientTherads.keySet().iterator();
            while(it.hasNext()){
                String account4=new String(it.next());
                if(ClientTherads.get(account4).UserNickName.equals(Nickname3)){
                    return account4;
                }
            }
            return null;
        }

        public String getUseraccount() {
            return Useraccount;
        }

        //将注册用户添加入文件
        public void AddUserToFile(String account4,String NickName4,String Password4){
            try {
                appendAccount = new BufferedWriter(new FileWriter("src/server/userdata.txt",true));
                appendAccount.newLine();
                appendAccount.write(account4+"  "+NickName4+"  "+Password4);
                appendAccount.flush();
                appendAccount.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }
    //将退出用户从好友列表里清除
    public void RemoveElement(String target){
        int index=MemberOnline.indexOf(target);
        MemberOnline.remove(index);
    }

    public static void main(String argc[]){
        ServerProgram test=new ServerProgram();
    }
}


