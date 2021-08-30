
package ä��Ŭ���̾�Ʈ;

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


//ActionListener�� KeyListener�� ��ӹ޴´�. 
public class Client extends JFrame implements ActionListener,KeyListener {
// �ڵ� ������ ctrl+shift+o
   
   //Login GUI ����
   final ImageIcon logo_img = new ImageIcon("src\\icon.png");
   private JFrame Login_GUI = new JFrame();
   private JPanel Login_Pane = new JPanel();
   private JPanel logo_pane = new JPanel(){
      public void paintComponent(Graphics g){//logo_pane�� �̹������
         g.drawImage(logo_img.getImage(),0,0,null);
         setOpaque(false);
         super.paintComponent(g);
      };
   }; 
   private JTextField ip_tf; //ip �ؽ�Ʈ�ʵ�
   private JTextField port_tf; //port �ؽ�Ʈ�ʵ�
   private JTextField id_tf; //id �ؽ�Ʈ�ʵ�
   private JButton login_btn = new JButton("��  ��");
   
   
   //Main GUI ����
   final ImageIcon main_img = new ImageIcon("src\\off.png");
   final ImageIcon chat_img = new ImageIcon("src\\on.png");
   private JPanel contentPane = new JPanel(){//ContentPane�� �̹������
      public void paintComponent(Graphics g){
           g.drawImage(main_img.getImage(), 0, 0, null);
           setOpaque(false);
           super.paintComponent(g);
        };
   };
   private JTextField message_tf;//ä�ù� ��ȭ
   private JButton notesend_btn = new JButton("����������");
   private JButton joinroom_btn = new JButton("ä�ù�����");
   private JButton createroom_btn = new JButton("�游���");
   private JButton send_btn = new JButton("����");
   private JButton exit_btn = new JButton("������");
   private JLabel lbTimelabel = new JLabel(" ?");
   
   private JList User_list = new JList();
   private JList Room_list = new JList();
   
   private JTextArea Chat_area = new JTextArea();//ä�ù� ��ȭȭ��
   
   //�޴���
private JMenuBar bar=new JMenuBar();
private JMenu menu_talk=new JMenu("��ȭ����");
private JMenuItem talkOpen=new JMenuItem("�ҷ�����");
private JMenuItem talkSave=new JMenuItem("����");
private JMenuItem picsOpen=new JMenuItem("���� �ҷ�����");
private JMenuItem itemExit=new JMenuItem("������");

private JMenu menu_pics=new JMenu("���ȭ��");
private JMenuItem bg1=new JMenuItem("����");
private JMenuItem bg2=new JMenuItem("�Ŀ�����Ʈ");
private JMenuItem bg3=new JMenuItem("���־� ��Ʃ���");


//�̹�������
private StyleContext context = new StyleContext();
private StyledDocument document = new DefaultStyledDocument(context);

//�� �޼��� ��Ÿ�� ��ü ����
private Style myMessageStyle = context.getStyle(StyleContext.DEFAULT_STYLE);

// �Ӽ� ��ü ����
private SimpleAttributeSet attributes = new SimpleAttributeSet();
// ���̺�(�̹�����) ��Ÿ�� ��ü ����
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
//����Ʈ �ε��� ��
private JLabel lbNewlabel;
private JLabel lblNewLabel_1;

   //��Ʈ��ũ�� ���� �ڿ� ����

   private Socket socket;
   private String ip="";// �� ��ȣ�� �ڱ��ڽ�
   private int port;
   private String id="";//�г���
   private InputStream is;
   private OutputStream os;
   private DataInputStream dis;
   private DataOutputStream dos;
   
   private BufferedInputStream bis;
   private BufferedOutputStream bos;
   
   //�׿� ������
   Vector user_list = new Vector();//User ���� ����
   Vector room_list = new Vector();//Room ���� ����
   StringTokenizer st;
   
   private String My_Room; // ���� ���� ������ �� �̸�
 
   
   
   Client()//client������
   {
	  super("LAGS_CAHT");
      Login_init();//Login GUI���� �޼ҵ�
      Main_init();//Main GUI���� �޼ҵ�
      start();//�̺�Ʈ������ ���� �żҵ�
     
   }
   private void start()//�̺�Ʈ������ ���� �żҵ�
   {//��ư 6�� �׼� ������
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
      
    //�ؽ�Ʈ�ʵ� 1�� Ű ������
      message_tf.addKeyListener(this);
   }
   
   private void Main_init()//Main GUI���� �޼ҵ�
   {
	   
	   
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 580, 455);
      setResizable(true);
     
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      
      lbNewlabel = new JLabel("�� ü �� �� ��");
      lbNewlabel.setBounds(12, 10, 86, 15);
      contentPane.add(lbNewlabel);
      
      JScrollPane scrollPane_2 = new JScrollPane();
      scrollPane_2.setBounds(12, 32, 109, 117);
      contentPane.add(scrollPane_2);
      
      scrollPane_2.setViewportView(User_list);
      
      
      notesend_btn.setBounds(12, 159, 109, 23);
      contentPane.add(notesend_btn);
      
      lblNewLabel_1 = new JLabel("ä�ù���");
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
      lbTimelabel.setText("���ӽð� : "+date.toString());
      
      StyleConstants.setAlignment(myMessageStyle, StyleConstants.ALIGN_LEFT);
      textPane.setEditable(false);
      textPane.setBackground(null);
      textPane.setOpaque(false);
      scrollPane = new JScrollPane(textPane){
    	   public void paintComponent(Graphics g){//paintComponent�� �̹������
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
      
      //�޴���
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
   
   
   private void Login_init()//Login GUI���� �޼ҵ�
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
         socket = new Socket(ip,port);//���ϻ����Ͽ� ip�� port�� ����
         
         if(socket != null)//���������� ������ ����Ǿ������
         {
            Connection();//���� ���� �� Main GUI�� �Ѿ�� �޼ҵ�
         }
      } catch (UnknownHostException e) {//ȣ��Ʈ�� �Һи��� ���
         
    	  JOptionPane.showMessageDialog(null,"���� ����","�˸�",JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {//IO������ ���
         JOptionPane.showMessageDialog(null,"���� ����","�˸�",JOptionPane.INFORMATION_MESSAGE);
      }
      
   }
   
   private void Connection() // �������� �޼ҵ� ����κ�
   {
      try{//Stream ����
      
      is = socket.getInputStream();
      dis = new DataInputStream(is);
      
      os = socket.getOutputStream();
      dos = new DataOutputStream(os);
      }
      catch(IOException e)
      {
         JOptionPane.showMessageDialog(null,"���� ����","�˸�",JOptionPane.INFORMATION_MESSAGE);
      } // Stream ���� ��
      
      
      this.setVisible(true); // Main GUIǥ��
      this.Login_GUI.setVisible(false);//Login GUI�����
      

      // ó�� ���ӽÿ� ID ����
      send_message(id);
      
      // User_list ���� ����� �߰�
      user_list.add(id);
      User_list.setListData(user_list);
      lbNewlabel.setText("��ü������("+(User_list.getLastVisibleIndex()+1)+")");
      lblNewLabel_1.setText("ä�ù���("+(Room_list.getLastVisibleIndex()+1)+")");
      
      Thread th = new Thread(new Runnable() {//������ ����
    		
      	@Override
  		public void run() {//������ ����
  		 
      		while(true) //���α׷� ���������� ��� ����
          {
             
      		
             try {
                String msg = dis.readUTF(); // �޼������� 
            
                System.out.println("�����κ��� ���ŵ� �޼��� : "+msg);
                
                inmessage(msg);//�޽��� ó��
                lbNewlabel.setText("��ü������("+(User_list.getLastVisibleIndex()+1)+")");
                lblNewLabel_1.setText("ä�ù���("+(Room_list.getLastVisibleIndex()+1)+")");
 			
 			} catch (IOException e) {//IO������ ��� stream�� ���� ����
			
                try{
                os.close();
                is.close();
                dos.close();
                dis.close();
                socket.close();
                JOptionPane.showMessageDialog(null,"������ ���� ������","�˸�",JOptionPane.INFORMATION_MESSAGE);
                }
                catch(IOException e1){
                   
                }
                break;
                
             } 
         
               
          }
      
      
       }
      });
      th.start();//������ ����
    
      
   
   }
   
   private void inmessage(String str) //�����κ��� ������ ��� �޼���
   {
	  
      StringTokenizer st = new StringTokenizer(str, "/");
      //str�޽����� �Ľ��Ͽ� ��ū(/) �и�
      
	   String protocol = st.nextToken();//str���� ù��°/ ������ ���ڿ�
	   String Message = st.nextToken();//str���� �ι�°/ ������ ���ڿ�
	   
	   System.out.println("�������� :" +protocol);
	   System.out.println("���� :"+Message);
	 
	   if(protocol.equals("NewUser")) // ���ο� ������
      {
         user_list.add(Message);
         User_list.setListData(user_list);
         // AWT List add();
      }
      
      else if(protocol.equals("OldUser"))//���� ������
	   {
		   user_list.add(Message);	  
		   User_list.setListData(user_list);
	   }
	   else if(protocol.equals("Note"))//�������� ��
	   {
		   String note = st.nextToken();//str���� ����°/ ������ ���ڿ�

         System.out.println(Message+"����ڷκ��� �� ����"+note);
         
         JOptionPane.showMessageDialog
         (null,note,Message+"������ ���� ����",JOptionPane.CLOSED_OPTION);
      }

      else if(protocol.equals("CreateRoom")) // ���� ���������
      {
         My_Room = Message;
         message_tf.setEnabled(true);
         send_btn.setEnabled(true);
         joinroom_btn.setEnabled(false);
         createroom_btn.setEnabled(false);
         exit_btn.setEnabled(true);
         
      }
      else if(protocol.equals("CreateRoomFail")) // �游��� �������� ���
      {
         JOptionPane.showMessageDialog(null,"���� �̸��� ���� ���� �մϴ�","�˸�",JOptionPane.ERROR_MESSAGE);
      }
      else if(protocol.equals("New_Room")) // ���ο� ���� ���������
      {
         room_list.add(Message);
         Room_list.setListData(room_list);   
      }
      else if(protocol.equals("Chatting"))//ä�ù濡�� ��ȭ �ְ����� ��
	   {
    	  String msg = st.nextToken();//str���� ����°/ ������ ���ڿ�
    	  Date date=new Date();
		   textPane.setText(textPane.getText()+"\n["+date+"] "+Message+" : "+msg+"\n");
		   System.out.println(textPane.getText());
		   //Chat_area.append("["+date+"] "+Message+" : "+msg+"\n");
		   //System.out.println(Chat_area.getText());
		   scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	   }
	   else if(protocol.equals("OldRoom"))//������ �ִ� ��
	   {
		   room_list.add(Message);
		   Room_list.setListData(room_list);
	   }
	   else if(protocol.equals("JoinRoom"))//�濡 �� ��
      {
         My_Room = Message;
         message_tf.setEnabled(true);
         send_btn.setEnabled(true);
         joinroom_btn.setEnabled(false);
         createroom_btn.setEnabled(false);
         exit_btn.setEnabled(true);
         
         JOptionPane.showMessageDialog(null,"ä�ù濡 �����߽��ϴ�","�˸�",JOptionPane.INFORMATION_MESSAGE);
      }
      else if(protocol.equals("User_out"))//�����ڰ� ���� ��
	   {
		   user_list.remove(Message);
		   User_list.setListData(user_list);
	   }
	   else if(protocol.equals("Chat_area_Clear"))//ä�ù� ��ȭ Clear
	   {
		   //Chat_area.removeAll();
		   textPane.removeAll();
	   }
	   else if(protocol.equals("Exiting"))//ä�ù� ���� ��
	   {

         
         message_tf.setEnabled(false);
         send_btn.setEnabled(false);
         joinroom_btn.setEnabled(true);
         createroom_btn.setEnabled(true);
         exit_btn.setEnabled(false);
         
      }
      
   }
   
   private void send_message(String str)//�������� �޼����� ������ �޼ҵ�
   { 
      try {
         dos.writeUTF(str);
      } catch (IOException e) {
         
         e.printStackTrace();
      }
        
   }
   
   
   public static void main(String[] args) {//����
	   
	      new Client();//client��ü ����

   }

   @Override
   public void actionPerformed(ActionEvent e) {//�׼��̺�Ʈ ����
	      // TODO Auto-generated method stub
	      
	      if(e.getSource()==login_btn)//login_btn������ ��
	      {
	         System.out.println("�α��ι�ư");
	         
	         if(ip_tf.getText().length()==0)//ip�� �Է����� �ʾ��� ��
	         {
	        	 ip_tf.setText("IP�� �Է����ּ���");
	        	 ip_tf.requestFocus();
	         }
	         else if(port_tf.getText().length()==0)//port�� �Է����� �ʾ��� ��
	         {
	        	 port_tf.setText("Port��ȣ�� �Է����ּ���");
	        	 port_tf.requestFocus();
	         }
	         else if(id_tf.getText().length()==0)//id�� �Է����� �ʾ��� ��
	         {
	        	 id_tf.setText("ID�� �Է����ּ���");
	        	 id_tf.requestFocus();
	         }
	         else//���� ���� ��
	         {
	        	 ip = ip_tf.getText().trim(); //trim�� ������� �����ϰ� �Է��� �Ȱɷ� �����ϰ� �ϴ°� , ip�� �޴°�
	         
	        	 port = Integer.parseInt(port_tf.getText().trim());//int������ ����ȯ
	         
	        	 id = id_tf.getText().trim(); //id�޾ƿ��� �κ�
	         
	        	 Network();//������ ���������� �����ϱ� ���� �޼ҵ�
	         }
	      }
	      else if(e.getSource()==notesend_btn)//notesend_btn�� ������ ��
      {
         System.out.println("���� ������ ��ư Ŭ��");
         String user = (String)User_list.getSelectedValue();
         String note = JOptionPane.showInputDialog("�����޼���");
         
         if(note!=null)
         {
            send_message("Note/"+user+"/"+note);
            // Note/User2/���� User1�̾�
            
         }
         System.out.println("�޴� ��� : "+user+"| ���� ���� :"+note);
         
      }
      else if(e.getSource()==joinroom_btn)//joinroom_btn�� ������ ��
      {
    	 String JoinRoom = (String)Room_list.getSelectedValue();
    	 
    	 send_message("JoinRoom/"+JoinRoom); 
    	  
         System.out.println("��������ưŬ��");
      }
      else if(e.getSource()==createroom_btn)//createroom_btn�� ������ ��
      {
    	 String roomname = JOptionPane.showInputDialog("�� �̸�");
    	 if(!(roomname == null))
    	 {
    		 send_message("CreateRoom/"+roomname);
    	 }
    	
         System.out.println("�游����ưŬ��");
      }
      else if(e.getSource()==send_btn)//send_btn�� ������ ��
      {
    	  if(message_tf.getText() == null) // �ؽ�Ʈ�Է� ���ϰ� �����ϸ� ���ߴ����� �ذ�
  		{
           String msg = message_tf.getText();
           msg = " ";
           send_message("Chatting/"+My_Room+"/"+msg);
           message_tf.setText(" ");
           message_tf.requestFocus();
        }
        else if(!(message_tf.getText() == null))//�ؽ�Ʈ �Է��Ͽ� ����
  		{
  			send_message("Chatting/"+My_Room+"/"+message_tf.getText());
  			message_tf.setText(" ");
  	   	 	message_tf.requestFocus();
  		}
    	 
    	 // Chatting + ���̸� + ����
    	System.out.println("���۹�ư");
    	 
      }
      else if(e.getSource()==exit_btn)//exit_btn�� ������ ��
      {
         System.out.println("������ ��ư Ŭ��");
         send_message("Exiting/"+My_Room);
        
      }
      else if(e.getSource()==itemExit){
         System.out.println("����!!");
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
         System.out.println("�ҷ�����!!"); 
         FileDialog fd=new FileDialog(this, "��ȭ �ҷ�����", FileDialog.LOAD);
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
         System.out.println("����!!");
         FileDialog fd=new FileDialog(this, "��ȭ ����", FileDialog.SAVE);
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
              textPane.setText(textPane.getText()+"��ȭ�� ����Ǿ����ϴ�.\n");
              System.out.println("��ȭ������ ����Ǿ����ϴ�.\n");
          }catch(Exception e1){  }
      } 
      else if(e.getSource()==picsOpen){
          System.out.println("�ҷ�����!!"); 
          FileDialog fd=new FileDialog(this, "��ȭ �ҷ�����", FileDialog.LOAD);
           fd.show();
           dir1=fd.getDirectory();
           file1=fd.getFile();
           
           if(dir1==null||file1==null) return;
        // �޼��� ������ ����
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
    	  //����
      }else if(e.getSource()==bg2){
    	  //�Ŀ�����Ʈ
      }else if(e.getSource()==bg3){
    	  //���־�Ʃ���
      }
	      
   }
     
@Override
public void keyPressed(KeyEvent e) { // ������ ��
   // TODO Auto-generated method stub
   
}
@Override
public void keyReleased(KeyEvent e) { // �����ٶ��� ��
   
   if(e.getKeyCode()==10)//�Է°��� ������ ��
	{
		if(message_tf.getText() == null)//�޽��� �Է¾��ϰ� �����ϸ� ���ߴ� ���� �ذ�
		{
         String msg = message_tf.getText();
         msg = " ";
         send_message("Chatting/"+My_Room+"/"+msg);
         message_tf.setText(" ");
         message_tf.requestFocus();
      }
      else if(!(message_tf.getText() == null))//�޽��� �Է��ϰ� ����
      {
         send_message("Chatting/"+My_Room+"/"+message_tf.getText());
         message_tf.setText(" "); // �޼����� ������ ���� �޼��� ����â�� ����.
         message_tf.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��
      }
   }
   // TODO Auto-generated method stub
   
}
@Override
public void keyTyped(KeyEvent e) { // Ÿ�������� ��
   // TODO Auto-generated method stub
   
}

}