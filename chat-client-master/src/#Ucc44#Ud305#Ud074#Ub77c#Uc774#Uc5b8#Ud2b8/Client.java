
package 채팅클라이언트;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

/*
 * modified Author : Jeon Jong-Chan
 * Date : 2016-11-27 ~
 * 문제점 & 수정
 * 1. 채팅방 나가기가 안된다.
 * 2. 서버종료해도 서버와는 통신을 한다.
 * 3. 암호화 구현이 안되있다.
 */
public class Client extends JFrame implements ActionListener,KeyListener {
// 자동 재정의 ctrl+shift+o
   
   //Login GUI 변수
  
   final ImageIcon logo_img = new ImageIcon("C:\\chat\\login_logo.jpg");
   private JFrame Login_GUI = new JFrame();
   private JPanel Login_Pane = new JPanel();
   private JPanel logo_pane = new JPanel(){
      public void paintComponent(Graphics g){
         g.drawImage(logo_img.getImage(),0,0,null);
         setOpaque(false);
         super.paintComponent(g);
      };
   }; 
   private JTextField ip_tf; //ip 텍스트필드
   private JTextField port_tf; //port 텍스트필드
   private JTextField id_tf; //id 텍스트필드
   private JButton login_btn = new JButton("입  장");
   
   
   //Main GUI 변수
   ImageIcon main_img = new ImageIcon("main_sheet.jpg"); // 채팅창 배경 불러오기
   final ImageIcon chat_img = new ImageIcon("mainchat_sheet.jpg"); // 채팅 영역 배경 불러오기
   private JPanel contentPane = new JPanel(){
      public void paintComponent(Graphics g){
           g.drawImage(main_img.getImage(), 0, 0, null);
           setOpaque(false);
           super.paintComponent(g);
        };
   };
   private JTextField message_tf;
   private JButton notesend_btn = new JButton("쪽지보내기");
   private JButton joinroom_btn = new JButton("채팅방참여");
   private JButton createroom_btn = new JButton("방만들기");
   private JButton send_btn = new JButton("전송");
   private JButton exit_btn = new JButton("나가기");
   //======================= 배경화면 버튼 생성 ===============
   /*Author : 최재현*/
   private JButton backgnd1_btn = new JButton("3");
   private JButton backgnd2_btn = new JButton("2");
   private JButton backgnd3_btn = new JButton("1");
   private JButton return_btn = new JButton("");
   //=======================================================
   private JList User_list = new JList();
   private JList Room_list = new JList();
   
   private JTextArea Chat_area = new JTextArea();
   JScrollPane scrollPane = new JScrollPane(Chat_area){
   public void paintComponent(Graphics g){
     g.drawImage(chat_img.getImage(), 0, 0, null);
     setOpaque(false);
     super.paintComponent(g);
   }  
};
   
   //네트워크를 위한 자원 변수

   private Socket socket;
   private String ip="";// 이 번호는 자기자신
   private int port;
   private String id="";
   private InputStream is;
   private OutputStream os;
   private DataInputStream dis;
   private DataOutputStream dos;
   // 11-28 JC 추가 : xor 랜덤 수를 저장
   // 11-28 JC 추가 : 암호화된 문자 저장
   private String xor; 
   private String encryption;
   private String decryption;
   //그외 변수들
   Vector user_list = new Vector();
   Vector room_list = new Vector();
   StringTokenizer st;
   
   private String My_Room; // 내가 현재 접속한 방 이름
 
   
   
   Client()
   {
     super("LAGS_CAHT");
      Login_init();
      Main_init();
      start();
     
   }
   private void start()
   {
      login_btn.addActionListener(this);
      notesend_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      notesend_btn.setBackground(Color.WHITE);
      notesend_btn.addActionListener(this);
      joinroom_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      joinroom_btn.setBackground(Color.WHITE);
      joinroom_btn.addActionListener(this);
      createroom_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      createroom_btn.setBackground(Color.WHITE);
      createroom_btn.addActionListener(this);
      send_btn.setBackground(Color.WHITE);
      send_btn.addActionListener(this);
      exit_btn.setBackground(Color.WHITE);
      exit_btn.addActionListener(this);
      backgnd1_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      backgnd1_btn.setBackground(Color.WHITE);
      //================ 배경 이미지 버튼 눌림 확인=================
      /*Author : 최재현*/
      backgnd1_btn.addActionListener(this);
      backgnd2_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      backgnd2_btn.setBackground(Color.WHITE);
      backgnd2_btn.addActionListener(this);
      backgnd3_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      backgnd3_btn.setBackground(Color.WHITE);
      backgnd3_btn.addActionListener(this);
      
      return_btn.setFont(new Font("HY돋움", Font.PLAIN, 10));
      return_btn.setBackground(Color.WHITE);
      return_btn.addActionListener(this);
      //=======================================================
    
      message_tf.addKeyListener(this);
   }
   
   private void Main_init()
   {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 1460, 794);
      setResizable(false);
      contentPane.setBackground(Color.WHITE);
     
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      
      JLabel lbNewlabel = new JLabel("전 체 접 속 자");
      lbNewlabel.setFont(new Font("HY돋움", Font.PLAIN, 12));
      lbNewlabel.setBounds(124, 37, 86, 15);
      contentPane.add(lbNewlabel);
      
      
      notesend_btn.setBounds(113, 124, 109, 23);
      contentPane.add(notesend_btn);
      
      JLabel lblNewLabel_1 = new JLabel("채팅방목록");
      lblNewLabel_1.setFont(new Font("HY돋움", Font.PLAIN, 12));
      lblNewLabel_1.setBounds(388, 37, 97, 15);
      contentPane.add(lblNewLabel_1);
    
   
      joinroom_btn.setBounds(431, 124, 109, 23);
      contentPane.add(joinroom_btn);
      
      createroom_btn.setBounds(313, 124, 109, 23);
      contentPane.add(createroom_btn);

      scrollPane.setBounds(54, 157, 1390, 573);         
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      contentPane.add(scrollPane);
      
      scrollPane.setBackground(Color.WHITE);   
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      
      
      message_tf = new JTextField();
      message_tf.setBounds(64, 740, 1233, 21);
      contentPane.add(message_tf);
      message_tf.setColumns(10);
      message_tf.setEnabled(false);
      
      send_btn.setBounds(1299, 739, 63, 23);
      contentPane.add(send_btn);
      send_btn.setEnabled(false);
      
      exit_btn.setBounds(1364, 739, 80, 23);
      contentPane.add(exit_btn);
      exit_btn.setEnabled(false);
      
      
    //================= 배경 이미지 변경 버튼 위치설정, 버튼추가==========
      /*Author : 최재현*/

      backgnd1_btn.setBounds(1362, 124, 54, 23); 
      contentPane.add(backgnd1_btn);        
      
      backgnd2_btn.setBounds(1297, 124, 54, 23);
      contentPane.add(backgnd2_btn);
       
      backgnd3_btn.setBounds(1231, 124, 54, 23);
      contentPane.add(backgnd3_btn);
      
      return_btn.setBounds(1203, 124, 21, 23);
      contentPane.add(return_btn);
      Chat_area.setFont(new Font("Monospaced", Font.PLAIN, 16));
      
      Chat_area.setBounds(54, 157, 1390, 573);
      contentPane.add(Chat_area);
      Chat_area.setDisabledTextColor(new Color(72, 61, 139));
      
      Chat_area.setBackground(Color.WHITE);
      Chat_area.setOpaque(false);
      Chat_area.setEditable(false);
      User_list.setFont(new Font("HY돋움", Font.PLAIN, 12));
      User_list.setBounds(124, 62, 86, 52);
      contentPane.add(User_list);
      Room_list.setFont(new Font("HY돋움", Font.PLAIN, 12));
      Room_list.setBounds(365, 56, 109, 52);
      contentPane.add(Room_list);
     
      //==========================================================
      
      
      this.setVisible(false);
   }
   
   
   private void Login_init()
   {
    
      
      Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Login_GUI.setBounds(100, 100, 226, 361);
      Login_GUI.setResizable(false);
      
      Login_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));
      Login_GUI.setContentPane(Login_Pane);
      Login_Pane.setLayout(null);
      
      logo_pane.setBounds(20, 10, 186, 108);
      Login_Pane.add(logo_pane);
      
      JLabel lblNewLabel = new JLabel("Server IP");
      lblNewLabel.setBounds(27, 150, 57, 15);
      Login_Pane.add(lblNewLabel);
      
      JLabel lblNewLabel_1 = new JLabel("Server Port");
      lblNewLabel_1.setBounds(27, 193, 73, 15);
      Login_Pane.add(lblNewLabel_1);
      
      JLabel lblNewLabel_2 = new JLabel("User ID");
      lblNewLabel_2.setBounds(27, 237, 57, 15);
      Login_Pane.add(lblNewLabel_2);
      
      ip_tf = new JTextField();
      ip_tf.setBounds(105, 147, 100, 21);
      Login_Pane.add(ip_tf);
      ip_tf.setColumns(10);
      
      port_tf = new JTextField();
      port_tf.setBounds(105, 190, 100, 21);
      Login_Pane.add(port_tf);
      port_tf.setColumns(10);
      
      id_tf = new JTextField();
      id_tf.setBounds(105, 237, 100, 21);
      Login_Pane.add(id_tf);
      id_tf.setColumns(10);
      
      
      login_btn.setBounds(27, 294, 176, 23); // '입장'버튼 위치 설정
      Login_Pane.add(login_btn);
      
      Login_GUI.setVisible(true); 
   }
   
   
   private void Network()
   {
   
      try {
         socket = new Socket(ip,port);
         
         if(socket != null)//정상적으로 소켓이 연겨되었을경우
         {
            Connection();
         }
      } catch (UnknownHostException e) {
         
         JOptionPane.showMessageDialog(null,"연결 실패","알림",JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {
         JOptionPane.showMessageDialog(null,"연결 실패","알림",JOptionPane.INFORMATION_MESSAGE);
      }
      
   }
   
   private void Connection() // 실제적인 메소드 연결부분
   {
      try{
      
      is = socket.getInputStream();
      dis = new DataInputStream(is);
      
      os = socket.getOutputStream();
      dos = new DataOutputStream(os);
      }
      catch(IOException e)
      {
         JOptionPane.showMessageDialog(null,"연결 실패","알림",JOptionPane.INFORMATION_MESSAGE);
      } // Stream 설정 끝
      
      
      this.setVisible(true); // main UI표시
      this.Login_GUI.setVisible(false);
      

      // 처음 접속시에 ID 전송
      send_message(id);
      
      // User_list 에서 사용자 추가
      user_list.add(id);
      User_list.setListData(user_list);
      
      Thread th = new Thread(new Runnable() {
   
       @Override
      public void run() {
       
          while(true) 
          {
         
             try {
                String msg = dis.readUTF(); // 메세지수신 
            
                System.out.println("서버로부터 수신된 메세지 : "+msg);
            
                inmessage(msg);
            
             } catch (IOException e) {
         
                try{
                os.close();
                is.close();
                dos.close();
                dis.close();
                socket.close();
                JOptionPane.showMessageDialog(null,"서버와 접속 끊어짐","알림",JOptionPane.INFORMATION_MESSAGE);
                }
                catch(IOException e1){
                   
                }
                break;
                
             } 
         
               
          }
      
      
       }
      });
      th.start();
    
      
   
   }
   
   private void inmessage(String str) //서버로부터 들어오는 모든 메세지
   {
      StringTokenizer st = new StringTokenizer(str, "/");
    
   
      String protocol = st.nextToken();
      String Message = st.nextToken();
      System.out.println(" debug 1 ");
      
      System.out.println("프로토콜 :" +protocol);
      System.out.println("내용 :"+Message);
    
      if(protocol.equals("NewUser")) // 새로운 접속자
      {
         user_list.add(Message);
         User_list.setListData(user_list);
         // AWT List add();
      }
      
      else if(protocol.equals("OldUser"))
      {
         user_list.add(Message);     
         User_list.setListData(user_list);
      }
      else if(protocol.equals("Note"))
      {
         String note = st.nextToken();

         System.out.println(Message+"사용자로부터 온 쪽지"+note);
         
         JOptionPane.showMessageDialog
         (null,note,Message+"님으로 부터 쪽지",JOptionPane.CLOSED_OPTION);
      }

      else if(protocol.equals("CreateRoom")) // 방을 만들었을때
      {
         // 11-28 JC 추가 : xor 암호화를 위한 키 값을 받아온다.
         try {
            
            xor = dis.readUTF();
            System.out.println(xor);
         }
         catch (IOException e) 
         {
            // TODO 자동 생성된 catch 블록
            e.printStackTrace();
         }
         My_Room = Message;
         message_tf.setEnabled(true);
         send_btn.setEnabled(true);
         joinroom_btn.setEnabled(false);
         createroom_btn.setEnabled(false);
         exit_btn.setEnabled(true);
         
      }
      else if(protocol.equals("CreateRoomFail")) // 방만들기 실패했을 경우
      {
         JOptionPane.showMessageDialog(null,"같은 이름의 방이 존재 합니다","알림",JOptionPane.ERROR_MESSAGE);
      }
      else if(protocol.equals("New_Room")) // 새로운 방을 만들었을때
      {
         room_list.add(Message);
         Room_list.setListData(room_list);   
      }
      else if(protocol.equals("Chatting"))
      {
         System.out.println(" debug 2 ");
         String msg = st.nextToken();
         System.out.println(" debug 4 :"+ msg);
         decryption = new String(xor(msg.getBytes()));   
         System.out.println("debug decryption message : " + decryption);
         Chat_area.append(Message+" : "+decryption+"\n");
         System.out.println(" debug 3 ");
         scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
      }
      else if(protocol.equals("Notice"))
      {
         System.out.println(" debug 2 ");
         String msg = st.nextToken();
         System.out.println(" debug 4 :"+ msg);
         Chat_area.append(Message+" : "+msg+"\n");
         System.out.println(" debug 3 ");
         scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
      }
      else if(protocol.equals("OldRoom"))
      {
         room_list.add(Message);
         Room_list.setListData(room_list);
      }
      else if(protocol.equals("JoinRoom"))
      {
         
         try 
         {
           //방에 참가시 해당 방에 키값을 받아온다.
            xor = dis.readUTF();
         } 
         catch (IOException e)
         {
            // TODO 자동 생성된 catch 블록
            e.printStackTrace();
         }
         My_Room = Message;
         message_tf.setEnabled(true);
         send_btn.setEnabled(true);
         joinroom_btn.setEnabled(false);
         createroom_btn.setEnabled(false);
         exit_btn.setEnabled(true);
         
         JOptionPane.showMessageDialog(null,"채팅방에 입장했습니다","알림",JOptionPane.INFORMATION_MESSAGE);
      }
      else if(protocol.equals("User_out"))
      {
         user_list.remove(Message);
         User_list.setListData(user_list);
      }
      else if(protocol.equals("Chat_area_Clear"))
      {
         Chat_area.removeAll();
      }
      else if(protocol.equals("Exiting"))
      {

         
         message_tf.setEnabled(false);
         send_btn.setEnabled(false);
         joinroom_btn.setEnabled(true);
         createroom_btn.setEnabled(true);
         exit_btn.setEnabled(false);
         
      }
      
   }
   
   private void send_message(String str)//서버에게 메세지를 보내는 메소드
   { 
      try {
         dos.writeUTF(str);
      } catch (IOException e) {
         
         e.printStackTrace();
      }
        
   }
   
   /* 11-28 JC 추가
    * 바이트로 전환된 문자열과 서버에게 전달받은 키값을 바이트로 바꾼 값과 xor 연산
    */
   public byte[] xor( byte[] data)
   {
      byte []key = xor.getBytes();
      
      final int orgLength = data.length;  
      final int keyLength = key.length; 
      //XOR 연산
      final byte[] converted = new byte[orgLength];  
      for ( int i = 0 , j = 0 ; i < orgLength ; i++ , j++ )  
      {  
       converted[ i ] = ( byte ) ( data[ i ] ^ key[ j ] );  
       j = (j < keyLength - 1 ? j : 0); //j의 값이 pwd문자열의 길이보다 커질경우 0부터 시작 아닐경우는 j의 값을 갖는다.
      }
     
      return converted ; //byte배열인 code를 String으로 변환하여 반환한다.
      }
   
   public static void main(String[] args) {
   
      new Client();

   }

   @Override
   public void actionPerformed(ActionEvent e) {
      // TODO Auto-generated method stub
      
      if(e.getSource()==login_btn)
      {
         System.out.println("로그인버튼");
         
         if(ip_tf.getText().length()==0)
         {
            ip_tf.setText("IP를 입력해주세요");
            ip_tf.requestFocus();
         }
         else if(port_tf.getText().length()==0)
         {
            port_tf.setText("Port번호를 입력해주세요");
            port_tf.requestFocus();
         }
         else if(id_tf.getText().length()==0)
         {
            id_tf.setText("ID를 입력해주세요");
            id_tf.requestFocus();
         }
     
         else
         {
            ip = ip_tf.getText().trim(); //trim은 빈공간을 제외하고 입력이 된걸로 가능하게 하는것 , ip를 받는곳
         
            port = Integer.parseInt(port_tf.getText().trim());//int형으로 형변환
         
            id = id_tf.getText().trim(); //id받아오는 부분
         
            Network();
         }
      }
      // ====================== 배경 이미지 변경 버튼 클릭 시 ===========================
      /*Author : 최재현*/

      else if(e.getSource()==backgnd3_btn)  
      {
         main_img=new ImageIcon("3.jpg");
         contentPane.repaint();
      }
      else if(e.getSource()==backgnd2_btn)
      {
         main_img=new ImageIcon("2.jpg");
         contentPane.repaint();
      }
      else if(e.getSource()==backgnd1_btn)
      {
         main_img=new ImageIcon("1.jpg");
         contentPane.repaint();
      }
      else if(e.getSource()==return_btn)
      {
         main_img=new ImageIcon("main_sheet.jpg");
         contentPane.repaint();
      }
      //================================================================
      else if(e.getSource()==notesend_btn)
      {
         System.out.println("쪽지 보내기 버튼 클릭");
         String user = (String)User_list.getSelectedValue();
         String note = JOptionPane.showInputDialog("보낼메세지");
         
         if(note!=null)
         {
            send_message("Note/"+user+"/"+note);
            // Note/User2/나는 User1이야
            
         }
         System.out.println("받는 사람 : "+user+"| 보낼 내용 :"+note);
         
      }
      else if(e.getSource()==joinroom_btn)
      {
        String JoinRoom = (String)Room_list.getSelectedValue();
        
        send_message("JoinRoom/"+JoinRoom); 
         
         System.out.println("방참여버튼클릭");
      }
      else if(e.getSource()==createroom_btn)
      {
        String roomname = JOptionPane.showInputDialog("방 이름");
        if(!(roomname == null))
        {
           send_message("CreateRoom/"+roomname);
        }
       
         System.out.println("방만들기버튼클릭");
      }
      else if(e.getSource()==send_btn)
      {
         if(message_tf.getText() == null) // 텍스트입력 안하고 전송하면 멈추는현상 해결
        {
           String msg = message_tf.getText();
           msg = " ";
           send_message("Chatting/"+My_Room+"/"+msg);
           message_tf.setText(" ");
           message_tf.requestFocus();
        }
        else if(!(message_tf.getText() == null))
        {
           /* 11-28 JC 추가
            *입력받은 메세지를 바이트로 전환하여 xor 함수에 전달 후 반환값은 다시 string 값으로 전환.
            *trim 함수를 통해 앞뒤공백을 제거해 오류가 나는 것을 막는다.
            */
           System.out.println("debug input message : "+ message_tf.getText().trim());
           encryption = new String(xor(message_tf.getText().trim().getBytes()));
           System.out.println("debug encryption message : " + encryption);
           send_message("Chatting/"+My_Room+"/"+encryption);
           message_tf.setText(" ");
           message_tf.requestFocus();
        }
        
        // Chatting + 방이름 + 내용
       

         System.out.println("전송버튼");
        
      }
      else if(e.getSource()==exit_btn)
      {
         System.out.println("나가기 버튼 클릭");
         send_message("Exiting/"+My_Room);
        
      }
   }
@Override
public void keyPressed(KeyEvent e) { // 눌렀을 때
   // TODO Auto-generated method stub
   
}
@Override
public void keyReleased(KeyEvent e) { // 눌렀다땠을 때
   
   if(e.getKeyCode()==10)
   {
      if(message_tf.getText() == null)
      {
         String msg = message_tf.getText();
         msg = " ";
         send_message("Chatting/"+My_Room+"/"+msg);
         message_tf.setText(" ");
         message_tf.requestFocus();
      }
      else if(!(message_tf.getText() == null))
      {
           encryption = new String(xor(message_tf.getText().trim().getBytes()));
           System.out.println("debug encryption message : " + encryption);
           send_message("Chatting/"+My_Room+"/"+encryption);
         //send_message("Chatting/"+My_Room+"/"+message_tf.getText());
         message_tf.setText(" "); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
             message_tf.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
      }
   }
   // TODO Auto-generated method stub
   
}
@Override
public void keyTyped(KeyEvent e) { // 타이핑했을 때
   // TODO Auto-generated method stub
   
}

}