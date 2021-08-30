
package 채팅클라이언트;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.Style;

import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;


//ActionListener와 KeyListener를 상속받는다. 
public class Client extends JFrame implements ActionListener,KeyListener {
// 자동 재정의 ctrl+shift+o
   
   //Login GUI 변수
   final ImageIcon logo_img = new ImageIcon("src\\icon.png");
   private JFrame Login_GUI = new JFrame();
   private JPanel Login_Pane = new JPanel();
   private JPanel logo_pane = new JPanel(){
      public void paintComponent(Graphics g){//logo_pane의 이미지배경
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
   final ImageIcon main_img = new ImageIcon("src\\off.png");
   final ImageIcon chat_img = new ImageIcon("src\\on.png");
   private JPanel contentPane = new JPanel(){//ContentPane의 이미지배경
      public void paintComponent(Graphics g){
           g.drawImage(main_img.getImage(), 0, 0, null);
           setOpaque(false);
           super.paintComponent(g);
        };
   };
   private JTextField message_tf;//채팅방 대화
   private JButton notesend_btn = new JButton("쪽지보내기");
   private JButton joinroom_btn = new JButton("채팅방참여");
   private JButton createroom_btn = new JButton("방만들기");
   private JButton send_btn = new JButton("전송");
   private JButton exit_btn = new JButton("나가기");
   private JLabel lbTimelabel = new JLabel(" ?");
   
   private JList User_list = new JList();
   private JList Room_list = new JList();
   
   private JTextArea Chat_area = new JTextArea();//채팅방 대화화면
   
   //메뉴바
private JMenuBar bar=new JMenuBar();
private JMenu menu_talk=new JMenu("대화내용");
private JMenuItem talkOpen=new JMenuItem("불러오기");
private JMenuItem talkSave=new JMenuItem("저장");
private JMenuItem picsOpen=new JMenuItem("사진 불러오기");
private JMenuItem itemExit=new JMenuItem("끝내기");

private JMenu menu_pics=new JMenu("배경화면");
private JMenuItem bg1=new JMenuItem("엑셀");
private JMenuItem bg2=new JMenuItem("파워포인트");
private JMenuItem bg3=new JMenuItem("비주얼 스튜디오");


//이미지파일
private StyleContext context = new StyleContext();
private StyledDocument document = new DefaultStyledDocument(context);

//내 메세지 스타일 객체 설정
private Style myMessageStyle = context.getStyle(StyleContext.DEFAULT_STYLE);

// 속성 객체 설정
private SimpleAttributeSet attributes = new SimpleAttributeSet();
// 레이블(이미지용) 스타일 객체 설정
private Style labelStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
private Icon icon;
private JLabel label;
private JFrame frame = new JFrame();
private Container content = frame.getContentPane();
private JTextPane textPane = new JTextPane(document);
private ImageIcon image;
private JScrollPane scrollPane;
private File f;
private String dir1;
private String file1;
//리스트 인덱수 수
private JLabel lbNewlabel;
private JLabel lblNewLabel_1;

   //네트워크를 위한 자원 변수

   private Socket socket;
   private String ip="";// 이 번호는 자기자신
   private int port;
   private String id="";//닉네임
   private InputStream is;
   private OutputStream os;
   private DataInputStream dis;
   private DataOutputStream dos;
   
   private BufferedInputStream bis;
   private BufferedOutputStream bos;
   
   //그외 변수들
   Vector user_list = new Vector();//User 원소 관리
   Vector room_list = new Vector();//Room 원소 관리
   StringTokenizer st;
   
   private String My_Room; // 내가 현재 접속한 방 이름
 
   
   
   Client()//client생성자
   {
	  super("LAGS_CAHT");
      Login_init();//Login GUI설정 메소드
      Main_init();//Main GUI설정 메소드
      start();//이벤트리스너 설정 매소드
     
   }
   private void start()//이벤트리스너 설정 매소드
   {//버튼 6개 액션 리스너
      login_btn.addActionListener(this);
      notesend_btn.addActionListener(this);
      joinroom_btn.addActionListener(this);
      createroom_btn.addActionListener(this);
      send_btn.addActionListener(this);
      exit_btn.addActionListener(this);
      
      talkOpen.addActionListener(this);
      talkSave.addActionListener(this);
      itemExit.addActionListener(this);
      picsOpen.addActionListener(this);
      
      bg1.addActionListener(this);
      bg2.addActionListener(this);
      bg3.addActionListener(this);
    //  picsSave.addActionListener(this);
      
    //텍스트필드 1개 키 리스너
      message_tf.addKeyListener(this);
   }
   
   private void Main_init()//Main GUI설정 메소드
   {
	   
	   
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 580, 455);
      setResizable(true);
     
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      
      lbNewlabel = new JLabel("전 체 접 속 자");
      lbNewlabel.setBounds(12, 10, 86, 15);
      contentPane.add(lbNewlabel);
      
      JScrollPane scrollPane_2 = new JScrollPane();
      scrollPane_2.setBounds(12, 32, 109, 117);
      contentPane.add(scrollPane_2);
      
      scrollPane_2.setViewportView(User_list);
      
      
      notesend_btn.setBounds(12, 159, 109, 23);
      contentPane.add(notesend_btn);
      
      lblNewLabel_1 = new JLabel("채팅방목록");
      lblNewLabel_1.setBounds(12, 192, 97, 15);
      contentPane.add(lblNewLabel_1);
      
      JScrollPane scrollPane_1 = new JScrollPane();
      scrollPane_1.setBounds(12, 213, 109, 135);
      contentPane.add(scrollPane_1);
      
      scrollPane_1.setViewportView(Room_list);
    
   
      joinroom_btn.setBounds(12, 386, 109, 23);
      contentPane.add(joinroom_btn);
      
      createroom_btn.setBounds(12, 358, 109, 23);
      contentPane.add(createroom_btn);
      
      Date date=new Date();
      lbTimelabel.setBounds(300, 10, 286, 15);
      contentPane.add(lbTimelabel);
      lbTimelabel.setText("접속시간 : "+date.toString());
      
      StyleConstants.setAlignment(myMessageStyle, StyleConstants.ALIGN_LEFT);
      textPane.setEditable(false);
      textPane.setBackground(null);
      textPane.setOpaque(false);
      scrollPane = new JScrollPane(textPane){
    	   public void paintComponent(Graphics g){//paintComponent의 이미지배경
    		  g.drawImage(chat_img.getImage(), 0, 0, null);
    		  setOpaque(false);
    		  super.paintComponent(g);
    	   }  
    	};
      
      scrollPane.setBounds(133, 29, 418, 347);         
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      contentPane.add(scrollPane);
      
      //Chat_area.setBackground(null);
      //Chat_area.setOpaque(false);
      
      scrollPane.setBackground(null);   
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      scrollPane.setViewportView(textPane);
      //scrollPane.setViewportView(Chat_area);
      //Chat_area.setEditable(false);
      
      
      message_tf = new JTextField();
      message_tf.setBounds(133, 387, 279, 21);
      contentPane.add(message_tf);
      message_tf.setColumns(10);
      message_tf.setEnabled(false);
      
      send_btn.setBounds(414, 386, 63, 23);
      contentPane.add(send_btn);
      send_btn.setEnabled(false);
      
      exit_btn.setBounds(479, 386, 80, 23);
      contentPane.add(exit_btn);
      exit_btn.setEnabled(false);
      
      //메뉴바
      this.setJMenuBar(bar);
      bar.add(menu_talk);
       menu_talk.add(talkOpen);
       menu_talk.add(talkSave);
       menu_talk.add(picsOpen);
       menu_talk.add(itemExit);
       bar.add(menu_pics);
       menu_pics.add(bg1);
       menu_pics.add(bg2);
       menu_pics.add(bg3);
      // menu_pics.add(picsSave);
      this.setVisible(false);
   }
   
   
   private void Login_init()//Login GUI설정 메소드
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
      
      
      login_btn.setBounds(27, 294, 176, 23);
      Login_Pane.add(login_btn);
      
      Login_GUI.setVisible(true); 
   }
   
   private void Network()
   {
   
      try {
         socket = new Socket(ip,port);//소켓생성하여 ip와 port로 연결
         
         if(socket != null)//정상적으로 소켓이 연결되었을경우
         {
            Connection();//정상 연결 시 Main GUI로 넘어가는 메소드
         }
      } catch (UnknownHostException e) {//호스트가 불분명한 경우
         
    	  JOptionPane.showMessageDialog(null,"연결 실패","알림",JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {//IO예외의 경우
         JOptionPane.showMessageDialog(null,"연결 실패","알림",JOptionPane.INFORMATION_MESSAGE);
      }
      
   }
   
   private void Connection() // 실제적인 메소드 연결부분
   {
      try{//Stream 설정
      
      is = socket.getInputStream();
      dis = new DataInputStream(is);
      
      os = socket.getOutputStream();
      dos = new DataOutputStream(os);
      }
      catch(IOException e)
      {
         JOptionPane.showMessageDialog(null,"연결 실패","알림",JOptionPane.INFORMATION_MESSAGE);
      } // Stream 설정 끝
      
      
      this.setVisible(true); // Main GUI표시
      this.Login_GUI.setVisible(false);//Login GUI사라짐
      

      // 처음 접속시에 ID 전송
      send_message(id);
      
      // User_list 에서 사용자 추가
      user_list.add(id);
      User_list.setListData(user_list);
      lbNewlabel.setText("전체접속자("+(User_list.getLastVisibleIndex()+1)+")");
      lblNewLabel_1.setText("채팅방목록("+(Room_list.getLastVisibleIndex()+1)+")");
      
      Thread th = new Thread(new Runnable() {//쓰레드 생성
    		
      	@Override
  		public void run() {//쓰레드 실행
  		 
      		while(true) //프로그램 종료전까지 계속 실행
          {
             
      		
             try {
                String msg = dis.readUTF(); // 메세지수신 
            
                System.out.println("서버로부터 수신된 메세지 : "+msg);
                
                inmessage(msg);//메시지 처리
                lbNewlabel.setText("전체접속자("+(User_list.getLastVisibleIndex()+1)+")");
                lblNewLabel_1.setText("채팅방목록("+(Room_list.getLastVisibleIndex()+1)+")");
 			
 			} catch (IOException e) {//IO예외인 경우 stream과 소켓 닫음
			
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
      th.start();//쓰레드 실행
    
      
   
   }
   
   private void inmessage(String str) //서버로부터 들어오는 모든 메세지
   {
	  
      StringTokenizer st = new StringTokenizer(str, "/");
      //str메시지를 파싱하여 토큰(/) 분리
      
	   String protocol = st.nextToken();//str에서 첫번째/ 이후의 문자열
	   String Message = st.nextToken();//str에서 두번째/ 이후의 문자열
	   
	   System.out.println("프로토콜 :" +protocol);
	   System.out.println("내용 :"+Message);
	 
	   if(protocol.equals("NewUser")) // 새로운 접속자
      {
         user_list.add(Message);
         User_list.setListData(user_list);
         // AWT List add();
      }
      
      else if(protocol.equals("OldUser"))//기존 접속자
	   {
		   user_list.add(Message);	  
		   User_list.setListData(user_list);
	   }
	   else if(protocol.equals("Note"))//쪽지받을 때
	   {
		   String note = st.nextToken();//str에서 세번째/ 이후의 문자열

         System.out.println(Message+"사용자로부터 온 쪽지"+note);
         
         JOptionPane.showMessageDialog
         (null,note,Message+"님으로 부터 쪽지",JOptionPane.CLOSED_OPTION);
      }

      else if(protocol.equals("CreateRoom")) // 방을 만들었을때
      {
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
      else if(protocol.equals("Chatting"))//채팅방에서 대화 주고받을 때
	   {
    	  String msg = st.nextToken();//str에서 세번째/ 이후의 문자열
    	  Date date=new Date();
		   textPane.setText(textPane.getText()+"\n["+date+"] "+Message+" : "+msg+"\n");
		   System.out.println(textPane.getText());
		   //Chat_area.append("["+date+"] "+Message+" : "+msg+"\n");
		   //System.out.println(Chat_area.getText());
		   scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	   }
	   else if(protocol.equals("OldRoom"))//기존에 있던 방
	   {
		   room_list.add(Message);
		   Room_list.setListData(room_list);
	   }
	   else if(protocol.equals("JoinRoom"))//방에 들어갈 때
      {
         My_Room = Message;
         message_tf.setEnabled(true);
         send_btn.setEnabled(true);
         joinroom_btn.setEnabled(false);
         createroom_btn.setEnabled(false);
         exit_btn.setEnabled(true);
         
         JOptionPane.showMessageDialog(null,"채팅방에 입장했습니다","알림",JOptionPane.INFORMATION_MESSAGE);
      }
      else if(protocol.equals("User_out"))//접속자가 나갈 때
	   {
		   user_list.remove(Message);
		   User_list.setListData(user_list);
	   }
	   else if(protocol.equals("Chat_area_Clear"))//채팅방 대화 Clear
	   {
		   //Chat_area.removeAll();
		   textPane.removeAll();
	   }
	   else if(protocol.equals("Exiting"))//채팅방 나갈 때
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
   
   
   public static void main(String[] args) {//메인
	   
	      new Client();//client객체 생성

   }

   @Override
   public void actionPerformed(ActionEvent e) {//액션이벤트 수행
	      // TODO Auto-generated method stub
	      
	      if(e.getSource()==login_btn)//login_btn눌렀을 때
	      {
	         System.out.println("로그인버튼");
	         
	         if(ip_tf.getText().length()==0)//ip를 입력하지 않았을 때
	         {
	        	 ip_tf.setText("IP를 입력해주세요");
	        	 ip_tf.requestFocus();
	         }
	         else if(port_tf.getText().length()==0)//port를 입력하지 않았을 때
	         {
	        	 port_tf.setText("Port번호를 입력해주세요");
	        	 port_tf.requestFocus();
	         }
	         else if(id_tf.getText().length()==0)//id를 입력하지 않았을 때
	         {
	        	 id_tf.setText("ID를 입력해주세요");
	        	 id_tf.requestFocus();
	         }
	         else//소켓 연결 전
	         {
	        	 ip = ip_tf.getText().trim(); //trim은 빈공간을 제외하고 입력이 된걸로 가능하게 하는것 , ip를 받는곳
	         
	        	 port = Integer.parseInt(port_tf.getText().trim());//int형으로 형변환
	         
	        	 id = id_tf.getText().trim(); //id받아오는 부분
	         
	        	 Network();//소켓을 정상적으로 연결하기 위한 메소드
	         }
	      }
	      else if(e.getSource()==notesend_btn)//notesend_btn을 눌렀을 때
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
      else if(e.getSource()==joinroom_btn)//joinroom_btn을 눌렀을 때
      {
    	 String JoinRoom = (String)Room_list.getSelectedValue();
    	 
    	 send_message("JoinRoom/"+JoinRoom); 
    	  
         System.out.println("방참여버튼클릭");
      }
      else if(e.getSource()==createroom_btn)//createroom_btn을 눌렀을 때
      {
    	 String roomname = JOptionPane.showInputDialog("방 이름");
    	 if(!(roomname == null))
    	 {
    		 send_message("CreateRoom/"+roomname);
    	 }
    	
         System.out.println("방만들기버튼클릭");
      }
      else if(e.getSource()==send_btn)//send_btn을 눌렀을 때
      {
    	  if(message_tf.getText() == null) // 텍스트입력 안하고 전송하면 멈추는현상 해결
  		{
           String msg = message_tf.getText();
           msg = " ";
           send_message("Chatting/"+My_Room+"/"+msg);
           message_tf.setText(" ");
           message_tf.requestFocus();
        }
        else if(!(message_tf.getText() == null))//텍스트 입력하여 전송
  		{
  			send_message("Chatting/"+My_Room+"/"+message_tf.getText());
  			message_tf.setText(" ");
  	   	 	message_tf.requestFocus();
  		}
    	 
    	 // Chatting + 방이름 + 내용
    	System.out.println("전송버튼");
    	 
      }
      else if(e.getSource()==exit_btn)//exit_btn을 눌렀을 때
      {
         System.out.println("나가기 버튼 클릭");
         send_message("Exiting/"+My_Room);
        
      }
      else if(e.getSource()==itemExit){
         System.out.println("종료!!");
         try {
            os.close();
            is.close();
            dos.close();
            dis.close();
            socket.close();
             System.exit(0);
         } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
      }
      else if(e.getSource()==talkOpen){
         System.out.println("불러오기!!"); 
         FileDialog fd=new FileDialog(this, "대화 불러오기", FileDialog.LOAD);
          fd.show();
          String dir=fd.getDirectory();
          String file=fd.getFile();
          if(dir==null||file==null) return;
          try{
              FileReader fr=new FileReader(dir+file);
              BufferedReader br=new BufferedReader(fr);
              while(true){
                  String data="";
                  data=br.readLine();
                  if(data==null)break;
                  //Chat_area.append(data+"\n");
                  textPane.setText("\n"+textPane.getText()+data+"\n");
                  scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
           	   
              }
          }catch(Exception e1){  }
        
      }
      else if(e.getSource()==talkSave){
         System.out.println("저장!!");
         FileDialog fd=new FileDialog(this, "대화 저장", FileDialog.SAVE);
          fd.show();
          String dir=fd.getDirectory();
          String file=fd.getFile();
          if(dir==null||file==null) return;
          f=new File(dir+file);
          try{
        	  
             PrintWriter pw=new PrintWriter(f);
              //pw.println(Chat_area.getText());
             pw.println(textPane.getText());
              pw.close();
              textPane.setText(textPane.getText()+"대화가 저장되었습니다.\n");
              System.out.println("대화내용이 저장되었습니다.\n");
          }catch(Exception e1){  }
      } 
      else if(e.getSource()==picsOpen){
          System.out.println("불러오기!!"); 
          FileDialog fd=new FileDialog(this, "대화 불러오기", FileDialog.LOAD);
           fd.show();
           dir1=fd.getDirectory();
           file1=fd.getFile();
           
           if(dir1==null||file1==null) return;
        // 메세지 콘텐츠 삽입
    	   try {
    		   Date date=new Date();
    	        textPane.setText(textPane.getText()+"\n["+date+"]\n");
    	    	icon = new ImageIcon(dir1+file1);
    	        label = new JLabel(icon);
    	        StyleConstants.setComponent(labelStyle, label);
    	        document.insertString(document.getLength(), "\n", labelStyle);
    	        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    	   } catch (BadLocationException badLocationException) {
    	      System.err.println("Oops");
    	   }
       }
      else if(e.getSource()==bg1){
    	  //엑셀
      }else if(e.getSource()==bg2){
    	  //파워포인트
      }else if(e.getSource()==bg3){
    	  //비주얼스튜디오
      }
	      
   }
     
@Override
public void keyPressed(KeyEvent e) { // 눌렀을 때
   // TODO Auto-generated method stub
   
}
@Override
public void keyReleased(KeyEvent e) { // 눌렀다땠을 때
   
   if(e.getKeyCode()==10)//입력값이 엔터일 때
	{
		if(message_tf.getText() == null)//메시지 입력안하고 전송하면 멈추는 현상 해결
		{
         String msg = message_tf.getText();
         msg = " ";
         send_message("Chatting/"+My_Room+"/"+msg);
         message_tf.setText(" ");
         message_tf.requestFocus();
      }
      else if(!(message_tf.getText() == null))//메시지 입력하고 전송
      {
         send_message("Chatting/"+My_Room+"/"+message_tf.getText());
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