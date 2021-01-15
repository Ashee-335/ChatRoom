package cn.edu.hcnu.client;


import cn.edu.hcnu.util.MD5;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * 登录线程
 */
public class LoginThread extends Thread {
    private JFrame loginf;

    private JTextField t;

    public void run() {
        /*
         * 设置登录界面
         */
        loginf = new JFrame();
        loginf.setResizable(false);
        loginf.setLocation(300, 200);
        loginf.setSize(400, 150);
        loginf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginf.setTitle("聊天室" + " - 登录");

        t = new JTextField("Version " + "1.1.0" + "        By wfy");
        t.setHorizontalAlignment(JTextField.CENTER);
        t.setEditable(false);
        loginf.getContentPane().add(t, BorderLayout.SOUTH);

        JPanel loginp = new JPanel(new GridLayout(3, 2));
        loginf.getContentPane().add(loginp);

        JTextField t1 = new JTextField("登录名:");
        t1.setHorizontalAlignment(JTextField.CENTER);
        t1.setEditable(false);
        loginp.add(t1);

        final JTextField loginname = new JTextField("wfy");
        loginname.setHorizontalAlignment(JTextField.CENTER);
        loginp.add(loginname);

        JTextField t2 = new JTextField("密码:");
        t2.setHorizontalAlignment(JTextField.CENTER);
        t2.setEditable(false);
        loginp.add(t2);

        final JTextField loginPassword = new JTextField("wfy1234");
        loginPassword.setHorizontalAlignment(JTextField.CENTER);
        loginp.add(loginPassword);
        /*
         * 监听退出按钮(匿名内部类)
         */
        JButton b1 = new JButton("退  出");
        loginp.add(b1);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        final JButton b2 = new JButton("登  录");
        loginp.add(b2);

        loginf.setVisible(true);

        /**
         * 监听器,监听"登录"Button的点击和TextField的回车
         */
        class ButtonListener implements ActionListener {
            private Socket s;

            public void actionPerformed(ActionEvent e) {
                String username = loginname.getText();//我们自己输入的
                String password = loginPassword.getText();//我们自己输入的
                PreparedStatement pstmt=null;
                String sql="";
                try {
                    /**
                 * 实现登录
                     *1.往数据库里插入数据
                     *2. 根据用户去数据库把加密后的密码拿到
                     *如：SELECT password FROM users WHERE username='wfy';
                     *3.
                 */
                    String url = "jdbc:oracle:thin:@localhost:1521:orcl";
                    String username_db = "opts";//username_dbs是数据库用户名
                    String password_db = "opts1234";//password_db是数据库的登录密码
                    Connection conn = DriverManager.getConnection(url, username_db, password_db);
                    sql = "SELECT password FROM users WHERE username=?";//条件查询
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1,username);
                    ResultSet rs = pstmt.executeQuery();//结果集
                    //如果这个结果集能够往下走，就证明根据用户名找到对应的密码
                    if (rs.next()) {
                        String encodePassword = rs.getString("PASSWORD");
                        //把登录界面输入的密码（password）和数据库里加密后的密码（encodePassword）进行比对（调用MD5类的checkpassword方法）
                        if (MD5.checkpassword(password, encodePassword)) {
                            /*获取本机IP
                                    开启一个端口8888
                            隐藏登录界面
                                    显示聊天窗口
                             */
                            InetAddress addr = InetAddress.getLocalHost();
                            System.out.println("本机IP地址: "+addr.getHostAddress());
                            sql="UPDATE users SET ip=?,port=8888 WHERE username=?";
                            pstmt=conn.prepareStatement(sql);
                            pstmt.setString(1,addr.getHostAddress());
                            pstmt.setString(2,username);
                            pstmt.executeUpdate();
                            loginf.setVisible(false);
                            ChatThreadWindow chatThreadWindow=new ChatThreadWindow();
                            System.out.println("登录成功");
                        } else {
                            System.out.println("登录失败");
                        }
                    }
                } catch (SQLException ee) {
                    ee.printStackTrace();
                } catch (NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
            }
        }
        ButtonListener bl = new ButtonListener();
        b2.addActionListener(bl);
        loginname.addActionListener(bl);
        loginPassword.addActionListener(bl);
    }
}