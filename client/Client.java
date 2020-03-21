package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import static java.lang.System.exit;

public class Client {
    //1.以下为必须在外部声明的变量

    //1.1 UI组件
    //登录窗口
    JFrame fram1;
    JLabel portUI;
    JLabel ItelAddr;
    JLabel account;
    JLabel password;
    JTextField inputportUI;
    JTextField inputItelAddr;
    JTextField inputAccount;
    JTextField inputpassword;
    JButton login,cancel,exitsystem,registerButton;
    JLabel background;

    //注册窗口
    JFrame fram1r;
    JLabel portUIr;
    JLabel ItelAddrr;
    JLabel accountr;
    JLabel passwordr;
    JLabel NickNamer;
    JTextField inputportUIr;
    JTextField inputItelAddrr;
    JTextField inputNickNamer;
    JTextField inputAccountr;
    JTextField inputpasswordr;
    JButton registerLogin,cancelr,exitsystemr;//注册并登录，取消，退出，注册
    JLabel backgroundr;//设置背景用

    //对话窗口
    JFrame ClientFrame;
    JButton DisconnectServer;
    JButton ConnectServer;//连接
    JButton NickNameButton;
    JButton SendMessageButton;//发送
    JTextField NickNameText;//昵称
    JTextField ServerIPAddressText;//服务器ip
    JTextField ServerPortText;//服务器端口
    JTextField InputContentText;//输入内容
    JList OnlineClientList;//在线列表
    JTextArea ChatContentLabel;//聊天内容

    //1.2 socket相关
    Socket socket;//input和output是通过socket定义的，如果socket关闭了，其他两个也失效
    BufferedReader input;//input为服务器传来的数据
    PrintStream output;//output为向服务器输出的数据

    //1.3  用户昵称
    DefaultListModel<String> OnlineClientNickName;//在线用户昵称列表：向其中插入数据，自动将数据插入到JList中
    String ToTargetName = "ALL";//目标用户昵称：OnlineClientList的监听器对其修改

    //1.4客户端线程
    ClientThread cliendThread;
    String MyNickName;//我的昵称

    //2.构造函数
    public Client() {
        //2.1 调用UI函数显示登录窗口
        CreateFrameLogin();
    }

    //3.登录，连接服务器
    //3.1直接登录
    public void ConnectServer() {
        //3.1.1 获取基本信息
        String ServerIPAddress = inputItelAddr.getText().trim();
        int ServerPort = Integer.parseInt(inputportUI.getText().trim());
        String Useraccount = inputAccount.getText();
        String Userpassword=inputpassword.getText();

        try {
            //3.1.2 socket相关
            socket = new Socket(ServerIPAddress, ServerPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintStream(socket.getOutputStream());
            //尝试进行登录
            output.println("Login|"+Useraccount+"|"+Userpassword);
            String answer=input.readLine();
            PartitionString AnalyzeAnswer=new PartitionString(answer,"\\|");
            if(AnalyzeAnswer.nextpart().equals("LoginAnswer")){
                if(AnalyzeAnswer.nextpart().equals("true")){
                    CreateFrame();
                    cliendThread = new ClientThread();
                    MyNickName=AnalyzeAnswer.nextpart();
                    //创造用于保存聊天记录的文件夹
                    File diarecords=new File("out\\production\\"+MyNickName);
                    if(!diarecords.exists())
                        diarecords.mkdir();
                    fram1.dispose();
                }
                else{
                    JOptionPane.showMessageDialog(null,"用户名或密码错误");
                }
            }
            else{
                JOptionPane.showMessageDialog(null,"服务器传入格式不正确");
            }

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null,"Client：主机地址异常" + e.getMessage());
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Client：连接服务器异常" + e.getMessage());
            return;
        }
    }
    //3.2进行注册后登录
    public void ConnectServerRegister(){
        //3.2.1 获取基本信息
        String ServerIPAddress = inputItelAddrr.getText().trim();
        int ServerPort = Integer.parseInt(inputportUIr.getText().trim());
        String Useraccount =inputAccountr.getText();
        String UserNickName=inputNickNamer.getText();
        String Userpassword=inputpasswordr.getText();
        try{
            socket = new Socket(ServerIPAddress, ServerPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintStream(socket.getOutputStream());
            //尝试进行注册
            output.println("Register|"+Useraccount+"|"+UserNickName+"|"+Userpassword);
            String answer=input.readLine();
            PartitionString AnalyzeAnswer=new PartitionString(answer,"\\|");
            if(AnalyzeAnswer.nextpart().equals("RegisterAnswer")){
                if(AnalyzeAnswer.nextpart().equals("true")){
                    CreateFrame();
                    cliendThread = new ClientThread();
                    MyNickName=UserNickName;
                    //创造用于保存聊天记录的文件夹
                    File diarecords=new File("out\\production\\"+MyNickName);
                    if(!diarecords.exists())
                        diarecords.mkdir();
                    fram1.dispose();
                }
                else{
                    JOptionPane.showMessageDialog(null,"用户名或密码错误");
                }
            }
            else{
                JOptionPane.showMessageDialog(null,"服务器传入格式不正确");
            }
        }
        catch (UnknownHostException e) {
           JOptionPane.showMessageDialog(null,"Client：主机地址异常" + e.getMessage());
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Client：连接服务器异常" + e.getMessage());
            return;
        }
    }

    //4. UI相关
    //4.1登录界面
    public void CreateFrameLogin() {
        fram1=new JFrame("登录界面");
        //设置大小及中心显示
        fram1.setSize(400,400);
        CenterWindow(fram1);
        Container c=fram1.getContentPane();
        //设置网格布局
        c.setLayout(new GridLayout(6,1));
        //设置windows界面风格
        try{
            String lfClassName="com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            UIManager.setLookAndFeel(lfClassName);
            SwingUtilities.updateComponentTreeUI(fram1);
        }
        catch (Exception e){}
        //设置窗体的角标
        try {
            Image iconwindow = ImageIO.read(getClass().getResource("/client/javalogin.jpg"));
            fram1.setIconImage(iconwindow);
        }
        catch (IOException e){
            System.out.println("image setting wrong");
        }
        //设置背景图片
        JPanel temp=(JPanel)c;
        temp.setOpaque(false);
        ImageIcon img=new ImageIcon(getClass().getResource("/client/Javaloginscheme.jpg"));
        background=new JLabel(img);
        fram1.getLayeredPane().add(background,new Integer(Integer.MIN_VALUE));
        background.setBounds(0,0,img.getIconWidth(),img.getIconHeight());


        JPanel pane0=new JPanel();
        pane0.setOpaque(false);
        GridBagLayout gl=new GridBagLayout();
        pane0.setLayout(gl);
        GridBagConstraints gc=new GridBagConstraints();
        gc.anchor=GridBagConstraints.CENTER;
        JLabel titlecenter=new JLabel("登录窗");
        titlecenter.setFont(new Font("宋体",Font.BOLD,28));
        gl.setConstraints(titlecenter,gc);
        pane0.add(titlecenter);
        c.add(pane0);

        JPanel pane01=new JPanel();
        pane01.setBackground(Color.white);
        ItelAddr=new JLabel("服务器地址");
        inputItelAddr=new JTextField(16);
        inputItelAddr.setText("127.0.0.1");
        pane01.add(ItelAddr);
        pane01.add(inputItelAddr);
        c.add(pane01);

        JPanel pane02=new JPanel();
        pane02.setBackground(Color.white);
        portUI=new JLabel("端口   ");
        inputportUI=new JTextField(13);
        inputportUI.setText("8088");
        pane02.add(portUI);
        pane02.add(inputportUI);
        c.add(pane02);


        JPanel pane1=new JPanel();
        pane1.setBackground(Color.white);
        account=new JLabel("用户名");
        account.setSize(50,30);
        inputAccount=new JTextField(13);
        inputAccount.setText("201706062003");
        pane1.add(account);
        pane1.add(inputAccount);
        c.add(pane1);

        JPanel pane2=new JPanel();
        pane2.setBackground(Color.white);
        password=new JLabel("密码  ");
        inputpassword=new JTextField(13);
        inputpassword.setText("062003");
        pane2.add(password);
        pane2.add(inputpassword);
        c.add(pane2);

        JPanel pane3=new JPanel();
        pane3.setBackground(Color.white);
        login=new JButton("登录");
        cancel=new JButton("取消");
        exitsystem=new JButton("退出");
        registerButton=new JButton("注册");
        pane3.add(login);
        pane3.add(cancel);
        pane3.add(exitsystem);
        pane3.add(registerButton);
        c.add(pane3);



        fram1.setVisible(true);
        fram1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AddActionListenerLogin();
    }

    //4.2注册界面
    public void CreatFormRegister(){
        fram1r=new JFrame("注册界面");
        //设置大小及中心显示
        fram1r.setSize(400,500);
        CenterWindow(fram1r);
        Container c=fram1r.getContentPane();
        //设置网格布局
        c.setLayout(new GridLayout(7,1));
        //设置windows界面风格
        try{
            String lfClassName="com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            UIManager.setLookAndFeel(lfClassName);
            SwingUtilities.updateComponentTreeUI(fram1r);
        }
        catch (Exception e){}
        //设置窗体的角标
        try {
            Image iconwindow = ImageIO.read(getClass().getResource("/client/javalogin.jpg"));
            fram1r.setIconImage(iconwindow);
        }
        catch (IOException e){
            System.out.println("image setting wrong");
        }
        //设置背景图片
        JPanel temp=(JPanel)c;
        temp.setOpaque(false);
        ImageIcon img=new ImageIcon(getClass().getResource("/client/Javaloginscheme.jpg"));
        backgroundr=new JLabel(img);
        fram1r.getLayeredPane().add(background,new Integer(Integer.MIN_VALUE));
        backgroundr.setBounds(0,0,img.getIconWidth(),img.getIconHeight());


        JPanel pane0=new JPanel();
        pane0.setOpaque(false);
        GridBagLayout gl=new GridBagLayout();
        pane0.setLayout(gl);
        GridBagConstraints gc=new GridBagConstraints();
        gc.anchor=GridBagConstraints.CENTER;
        JLabel titlecenter=new JLabel("注册窗");
        titlecenter.setFont(new Font("宋体",Font.BOLD,28));
        gl.setConstraints(titlecenter,gc);
        pane0.add(titlecenter);
        c.add(pane0);

        JPanel pane01=new JPanel();
        pane01.setBackground(Color.white);
        ItelAddrr=new JLabel("服务器地址");
        inputItelAddrr=new JTextField(16);
        inputItelAddrr.setText("127.0.0.1");
        pane01.add(ItelAddrr);
        pane01.add(inputItelAddrr);
        c.add(pane01);

        JPanel pane02=new JPanel();
        pane02.setBackground(Color.white);
        portUIr=new JLabel("端口   ");
        inputportUIr=new JTextField(13);
        inputportUIr.setText("8088");
        pane02.add(portUIr);
        pane02.add(inputportUIr);
        c.add(pane02);

        JPanel pane1=new JPanel();
        pane1.setBackground(Color.white);
        accountr=new JLabel("用户名");
        accountr.setSize(50,30);
        inputAccountr=new JTextField(13);
        pane1.add(accountr);
        pane1.add(inputAccountr);
        c.add(pane1);

        JPanel pane12=new JPanel();
        pane12.setBackground(Color.white);
        NickNamer=new JLabel("昵称  ");
        inputNickNamer=new JTextField(13);
        pane12.add(NickNamer);
        pane12.add(inputNickNamer);
        c.add(pane12);


        JPanel pane2=new JPanel();
        pane2.setBackground(Color.white);
        passwordr=new JLabel("密码  ");
        inputpasswordr=new JTextField(13);
        pane2.add(passwordr);
        pane2.add(inputpasswordr);
        c.add(pane2);

        JPanel pane3=new JPanel();
        pane3.setBackground(Color.white);
        registerLogin=new JButton("注册并登录");
        cancelr=new JButton("取消");
        exitsystemr=new JButton("退出");
        pane3.add(registerLogin);
        pane3.add(cancelr);
        pane3.add(exitsystemr);
        c.add(pane3);



        fram1r.setVisible(true);
        fram1r.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AddActionListenerRegister();


    }

    //4.3对话窗口
    public void CreateFrame() {

        ClientFrame = new JFrame("客户端");
        ClientFrame.setSize(800, 600);
        ClientFrame.setLocationRelativeTo(null);//设置在屏幕中央显示
        ClientFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//设置关闭按钮do nothig,用监听器代替
        Container c=ClientFrame.getContentPane();



        try{
            String lfClassName="com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            UIManager.setLookAndFeel(lfClassName);
            SwingUtilities.updateComponentTreeUI(ClientFrame);
        }
        catch (Exception e){e.printStackTrace();}

        //4.1.2 客户端信息
        JPanel ClientIdPanel = new JPanel();
        ClientIdPanel.setLayout(new FlowLayout(FlowLayout.LEFT));//4.2.1 设置客户端id栏的布局为流式布局
        ClientIdPanel.setSize(800, 100);
        ClientIdPanel.setOpaque(false);
        DisconnectServer = new JButton("断开");
        NickNameButton = new JButton("显示昵称");
        NickNameText = new JTextField(10);
        ClientIdPanel.add(NickNameButton);
        ClientIdPanel.add(NickNameText);
        ClientIdPanel.add(DisconnectServer);

        //4.1.2.6 设置标题
        ClientIdPanel.setBorder(new TitledBorder("用户信息栏"));

        //4.1.3 好友列表
        JPanel FriendListPanel = new JPanel();
        FriendListPanel.setPreferredSize(new Dimension(200, 400));
        FriendListPanel.setBorder(new TitledBorder("好友列表"));
        //4.1.3.1 好友列表内容
        OnlineClientNickName = new DefaultListModel<String>();
        OnlineClientList = new JList(OnlineClientNickName);
        OnlineClientNickName.addElement("所有人");
        FriendListPanel.add(OnlineClientList);

        //4.1.4 聊天内容面板
        JPanel ChatContentPanel = new JPanel();
        ChatContentPanel.setPreferredSize(new Dimension(490, 400));
        ChatContentPanel.setBorder(new TitledBorder("聊天内容"));

        //4.1.4.1 声明聊天内容文本框
        ChatContentLabel = new JTextArea();
        ChatContentLabel.setLineWrap(true);
        ChatContentLabel.setFont(new Font("宋体",Font.BOLD,17));
        JScrollPane ContentRoll=new JScrollPane(ChatContentLabel);
        ContentRoll.setPreferredSize(new Dimension(490,400));
        ChatContentPanel.add(ContentRoll);

        //4.1.5 输入内容面板
        JPanel InputContentPanel = new JPanel();
        InputContentPanel.setPreferredSize(new Dimension(600, 100));
        //4.1.5.1 聊天输入框
        InputContentText = new JTextField();
        InputContentText.setPreferredSize(new Dimension(600, 60));
        //4.1.5.2 发送按钮
        SendMessageButton = new JButton("发送");
        InputContentPanel.add(InputContentText);
        InputContentPanel.add(SendMessageButton);
        InputContentPanel.setBorder(new TitledBorder("输入内容"));

        //4.1.6 客户端整体布局
        ClientFrame.add(ClientIdPanel, BorderLayout.NORTH);
        ClientFrame.add(FriendListPanel, BorderLayout.WEST);
        ClientFrame.add(ChatContentPanel, BorderLayout.CENTER);
        ClientFrame.add(InputContentPanel, BorderLayout.SOUTH);


        //4.1.7设置可见
        ClientFrame.setVisible(true);    //设置可见必须在所有内容都add进Frame之后

        //4.1.8添加监听器
        AddActionListenerDialog();
    }
    //辅助函数中心显示
    public void CenterWindow(JFrame framtarget){
        Toolkit tk=framtarget.getToolkit();
        Dimension dm=tk.getScreenSize();
        framtarget.setLocation((int)(dm.getWidth()-framtarget.getWidth())/2,(int)(dm.getHeight()-framtarget.getHeight())/2);
    }

    //5.三个界面的监听器
    //5.1 登录界面添加事件监听
    private void AddActionListenerLogin() {
        //5.1.1登录按钮
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectServer();
            }
        });
        //5.1.2取消按钮
        cancel.addActionListener(new ActionListener() {
            @Override
            //将输入内容变为空
            public void actionPerformed(ActionEvent e) {
                inputAccount.setText("");
                inputpassword.setText("");
            }
        });
        //5.1.3退出按钮
        exitsystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //5.1.4注册按钮，跳转至注册界面
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fram1.dispose();
                CreatFormRegister();
            }
        });

    }

    //5.2注册界面的监听器
    private void AddActionListenerRegister(){
        //5.2.1注册并登录按钮
        registerLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectServerRegister();
            }
        });
        //5.2.2取消按钮
        cancelr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputAccountr.setText("");
                inputNickNamer.setText("");
                inputpasswordr.setText("");
            }
        });
        //5.2.3退出按钮
        exitsystemr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    //5.3对话窗口监听器
    private void AddActionListenerDialog(){
        //5.3.1点击发送
        SendMessageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = InputContentText.getText().trim();
                SendMessage("Message|" + ToTargetName + "|" + MyNickName + "|" + message);
                Log(MyNickName+":"+message);
                InputContentText.setText("");
                try {
                    saveword(ToTargetName, MyNickName+":"+message);
                }
                catch (Exception f){
                    f.printStackTrace();
                }
            }
        });

        //5.3.2 检验目标发送者是谁,并在屏幕上输出聊天记录
        OnlineClientList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int index = OnlineClientList.getSelectedIndex();
                if (index < 0) {
                    Error("Client：检测到目标发送者下标为负数");
                    return;
                }
                if (index == 0) {
                    ToTargetName = "ALL";
                    readwords("ALL");
                } else {
                    String ToClientNickName = (String) OnlineClientNickName.getElementAt(index);
                    ToTargetName = ToClientNickName;
                    readwords(ToTargetName);
                }
            }
        });
        //5.3.3退出并断开连接
        DisconnectServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendMessage("Logout|"+MyNickName);
                System.exit(0);
            }
        });
        //5.3.4设置关闭按钮，保证点击关闭按钮也能正常退出
        ClientFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SendMessage("Logout|"+MyNickName);
                System.exit(0);
            }
        });
        //5.2.5显示昵称
        NickNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NickNameText.setText(MyNickName);
            }
        });
    }

    //6.辅助函数
    //6.1 输出错误
    private void Error(String message) {
        ChatContentLabel.append(message+"\r\n");
    }

    //6.2 输出上线下线内容
    private void Log(String message) {
        ChatContentLabel.append(message+"\r\n");
    }

    //6.3 输出私聊内容
    private void Message(String message) {
        ChatContentLabel.append(message+"\r\n");
    }

    //6.4 输出广播内容
    private void MessageTotal(String message) {
        ChatContentLabel.append(message+"\r\n");
    }

    //6.5将聊天记录保存至本地
    public void saveword(String address,String word) throws Exception
    {
        File file=new File("out\\production\\"+MyNickName+"\\"+address+".txt");
        if(!file.exists())
        {file.createNewFile();}
        FileWriter fw = new FileWriter(file,true);
        BufferedWriter bw = new BufferedWriter(fw);
        try
        {bw.write(word+"\r\n");}
        finally
        {
            bw.close();
            fw.close();
        }

    }

    //6.6从文件里读取聊天记录到对话框
    public void readwords(String target){
        File targetfile=new File("out\\production\\"+MyNickName+"\\"+target+".txt");
        ChatContentLabel.setText("");
        if(targetfile.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(targetfile));
                ChatContentLabel.setText("");
                while(br.ready()){
                    String dia=br.readLine();
                    ChatContentLabel.append(dia+"\r\n");
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //6.7发送消息
    public void SendMessage(String message) {
        output.println(message);
        output.flush();
    }

    //6.8移除已经下线的用户
    public void RemoveElement(String target){
        int index=OnlineClientNickName.indexOf(target);
        OnlineClientNickName.remove(index);
    }



    //7. 客户端线程 内部类
    public class ClientThread implements Runnable {
        //与服务器建立连接时，新建客户端线程，否则无法接收信息
        //与服务器断开连接时，向服务器告知，杀掉客户端进程
        //客户端调用readline时会产生死锁，故需要新建一个线程
        boolean isRuning = true;

        //7.1 构造函数
        public ClientThread() {
            //5.1.1 开始本线程
            new Thread(this).start();
        }

        //7.2 run函数会在线程开始时自动调用
        public void run() {
            while (isRuning) {//循环用于重复接收消息，客户端断开连接之前不停止
                String message;
                try {
                    //7.2.1 在服务器传来的消息中读取下一行
                    message = input.readLine();
                    PartitionString tokens = new PartitionString(message, "\\|");//对原有消息进行分割
                    String MessageType = tokens.nextpart();

                    //7.2.2根据人为定义的传输协议对消息进行显示
                    switch (MessageType) {
                        case "Login": {//其他用户上线
                            String LoginClientNickName = tokens.nextpart();
                            Log("上线通知：用户" + LoginClientNickName + "已上线");
                            OnlineClientNickName.addElement(LoginClientNickName);
                            break;
                        }

                        case "Message": {//聊天消息
                            String ToClientNickName = tokens.nextpart();
                            String FromClientNickName = tokens.nextpart();
                            String content = tokens.nextpart();
                            if ("ALL".equals(ToClientNickName)) {
                                //如果不为自己发来的消息
                                if(!FromClientNickName.equals(MyNickName)) {
                                    MessageTotal("来自" + FromClientNickName + "对全体的消息：" + content);
                                    try {
                                        saveword("All", FromClientNickName + ":" + content);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Message("来自" + FromClientNickName + "对您的私聊消息：" + content);
                                try{
                                    saveword(FromClientNickName,FromClientNickName+":"+content);
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }

                        case "Logout": {//其他用户下线的消息
                            String exituser=tokens.nextpart();
                            Log("用户"+exituser+"已下线");
                            //将下线用户从好友列表中移除
                            RemoveElement(exituser);
                            break;
                        }
                        default: {
                            Error("客户端接收消息格式错误");
                            break;
                        }
                    }
                    System.out.println("客户端接收到" + message);
                } catch (IOException e) {
                    Error("Client：客户端接收消息失败" + e.getMessage());
                }
            }
        }
    }



    public static void main(String args[]) {
        Client client = new Client();
    }
}