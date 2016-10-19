package com.mc.mctalk.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.mc.mctalk.chatserver.ChattingClient;
import com.mc.mctalk.chatserver.ChattingController;
import com.mc.mctalk.dao.ChattingRoomDAO;
import com.mc.mctalk.view.uiitem.CustomJScrollPane;
import com.mc.mctalk.view.uiitem.RoundedImageMaker;
import com.mc.mctalk.view.uiitem.SearchPanel;
import com.mc.mctalk.vo.ChattingRoomVO;
import com.mc.mctalk.vo.UserVO;

public class ChattingRoomListPanel extends JPanel {
   //선택된 친구목록 모음 (맵)
   private Map<String, UserVO> selectedFriends = new LinkedHashMap<>();
   private Robot clickRobot ;
   private RoundedImageMaker imageMaker = new RoundedImageMaker();
   private ChattingClient client;
   
   private String loginID;
   private ArrayList<UserVO> alFriendsList;
   private Map<String, ChattingRoomVO> roomVOMap;

   private JList jlFriendsList;
   private CustomJScrollPane scrollPane;
   private DefaultListModel listModel;
   private SearchPanel pSearch;
   private JTextField tfSearch;
   
   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   Date times = null;
   SimpleDateFormat formatter1 = new SimpleDateFormat("aahh:mm", Locale.KOREA); 
   
   public ChattingRoomListPanel(ChattingClient client) {
      this.client = client;
      this.loginID = client.getLoginUserVO().getUserID();
      initPanel();
   }
   
   
   public void initPanel(){
      this.setLayout(new BorderLayout());
      //친구 찾기 패널 생성 및 해당 서치 키워드 액션 리스너 연결
      pSearch = new SearchPanel();
      tfSearch = pSearch.getTfSearch();
      tfSearch.addKeyListener(new FriendSearchKeyListener());
      tfSearch.setPreferredSize(new Dimension(325, 15));

      // JList에 데이터 담기
      jlFriendsList = new JList(new DefaultListModel());
      listModel = (DefaultListModel) jlFriendsList.getModel();
      
      //DB접속 후 친구 목록 가져와 Custom JList Model에 프로필 사진 path, 이름 엘리먼트 추가하기.
      ChattingRoomDAO dao = new ChattingRoomDAO();
      roomVOMap = new LinkedHashMap<String, ChattingRoomVO>();
      roomVOMap = dao.getAllChatRoomVOMap(loginID);
      addElementToJList();
      
      // JList 모양 변경
      jlFriendsList.setCellRenderer(new FriendsListCellRenderer());
      jlFriendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jlFriendsList.addMouseListener(new FriendSelectionListener());
//      scrollPane = new CustomJScrollPane(jlFriendsList,scrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,scrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane = new CustomJScrollPane(jlFriendsList);
      scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(230, 230, 230)));

      this.add(pSearch, "North");
      this.add(scrollPane, "Center");
   }
   
   
   //JList를 기존에 가져온 LinkedHashMap(순서보장) 데이터로 초기화
   public void addElementToJList(){
      Set<Map.Entry<String, ChattingRoomVO>> entrySet = roomVOMap.entrySet();
      Iterator<Map.Entry<String, ChattingRoomVO>> entryIterator = entrySet.iterator();
      while (entryIterator.hasNext()) {
         Map.Entry<String, ChattingRoomVO> entry = entryIterator.next();
         String key = entry.getKey();
         ChattingRoomVO vo = entry.getValue();
         listModel.addElement(vo);
      }
   }
   
   //선택된 친구에 대한 더블클릭 리스너
   class FriendSelectionListener implements MouseListener {
      @Override
      public void mouseClicked(MouseEvent e) {
         //getClickCount가 2 이상이면 더블클릭으로 판단함 && 선택된 인덱스가 -1이면 제대로된 선택이 아님
         if(e.getClickCount() >= 2 && jlFriendsList.getSelectedIndex() != -1){
            //선택된 친구ID와 로그인 ID를 매개변수로 컨트롤러 호출
			ChattingRoomVO vo = (ChattingRoomVO)jlFriendsList.getSelectedValue();
			new ChattingController(client).openChattingRoom(vo.getChattingRoomID());
         }
      }
      public void mouseReleased(MouseEvent arg0) {}
      public void mousePressed(MouseEvent arg0) {}
      public void mouseExited(MouseEvent arg0) {}
      public void mouseEntered(MouseEvent arg0) {}
   }
   
   //TextField 검색 키보드 리스너(입력될때마다 리스너 실행)
   class FriendSearchKeyListener implements KeyListener {
      @Override
      public void keyPressed(KeyEvent e) {
         searchFriendsMap();
      }
      @Override
      public void keyReleased(KeyEvent e) {
         searchFriendsMap();
      }
      @Override
      public void keyTyped(KeyEvent e) {   
         searchFriendsMap();
      }
   }
   
   //Map에 있는 개체 중 검색 값을 가진 JList엘리먼트를 찾아 보여주기 
   public void searchFriendsMap(){
      String inputSearchText = tfSearch.getText().trim();
      //System.out.println("입력된 값 : " + inputSearchText);
      //입력된 값이 없을 경우 전체 리스트 항목 삭제 후 다시 로딩
      if(inputSearchText.length()==0){
         listModel.removeAllElements();
         addElementToJList();
      }else{
      //있을 경우 전체 삭제 후 입력된 값을 받아 객채 안에 해당 값을 가진 엘리먼트를 추가. 없다면 삭제.
      //DB를 다시 붙는 개념이 아니고, 최초 1번만 붙어서 받아온 데이터를 담고 있는 map에 대한 컨트롤임.
      //메뉴 이동시 & 친구 추가시 친구 리스트에 대한 refresh 필요할 듯.
         listModel.removeAllElements();
         for (Map.Entry<String, ChattingRoomVO> entry : roomVOMap.entrySet()) {
            ChattingRoomVO vo = entry.getValue();
            if (vo.getChattingRoomName().contains(inputSearchText)) {
               listModel.addElement(vo);
            } else {
               listModel.removeElement(vo);
            }
         }//for
      }//if
   }
   
   //JList 모양 변경
   class FriendsListCellRenderer extends JPanel implements ListCellRenderer<ChattingRoomVO> {
      private JLabel lbImgIcon = new JLabel();
      private JLabel lbName = new JLabel();
      private JLabel lbStatMsg = new JLabel();
      private JPanel panelText;
      private JLabel groupIcon = new JLabel();
      private BufferedImage img = null;
      private BufferedImage unreadImg = null;
      private JLabel unreadmsg = new JLabel();
      private JLabel lastMsgTime = new JLabel();
      private JPanel brank = new JPanel();
      private JPanel brank1 = new JPanel();
      private JPanel brank2 = new JPanel();
      
      private JPanel uppanel = new JPanel();
      private JPanel downpanel = new JPanel();
      
      

      
      
      
//      times = formatter.parse(msgSendTime);
      
      public FriendsListCellRenderer() {
         Border border = this.getBorder();
         Border margin = new EmptyBorder(5, 15, 5, 10);
         this.setLayout(new BorderLayout(10, 10)); //간격 조정이 되버림(확인필요)
         this.setBorder(new CompoundBorder(border, margin));
         this.setPreferredSize(new Dimension(378, 95));
         
         lbName.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
         lbStatMsg.setFont(new Font("Malgun Gothic", Font.PLAIN, 10));
         lbStatMsg.setBorder(new EmptyBorder(0, 10, 0, 10));
         
         
         lastMsgTime.setFont(new Font("Monospaced",Font.PLAIN, 12));
         lastMsgTime.setForeground(Color.lightGray);
         lbName.setPreferredSize(new Dimension(150, 500));
         lbStatMsg.setPreferredSize(new Dimension(220, 0));
//         lastMsgTime.setPreferredSize(new Dimension(0, 0));
         unreadmsg.setPreferredSize(new Dimension(30, 0));
         
         
         
         
         panelText = new JPanel(new GridLayout(2,2));
         panelText.setBorder(new EmptyBorder(15, 10, 15, 0));
//         panelText.setPreferredSize(new Dimension(50, 100));
         
         uppanel.setLayout(new BorderLayout());
         downpanel.setLayout(new BorderLayout());
         
         brank.setLayout(new BorderLayout());
         brank1.setLayout(new BorderLayout());
         
         
         
         panelText.add(uppanel);
         panelText.add(downpanel);
         
         
         uppanel.add(lbName,BorderLayout.LINE_START);
         uppanel.add(groupIcon,BorderLayout.CENTER);
         uppanel.add(brank,BorderLayout.EAST);
         brank.add(lastMsgTime,BorderLayout.EAST);
//         panelText.add(lastMsgTime);
         downpanel.add(lbStatMsg,BorderLayout.LINE_START);
//         downpanel.add(brank2);
         downpanel.add(brank1,BorderLayout.EAST);
         brank1.add(unreadmsg,BorderLayout.EAST);
//         lbCloseBtn.setPreferredSize(new Dimension(48, 45));
         
         add(lbImgIcon, BorderLayout.WEST);
         add(panelText, BorderLayout.CENTER);
         
         
         
         
         
         
      }
      

      
      @Override
      public Component getListCellRendererComponent(JList<? extends ChattingRoomVO> list, ChattingRoomVO value, int index,
            boolean isSelected, boolean cellHasFocus) {
         //받아온 JList의 값을 UserVO 객체에 담기
         ChattingRoomVO vo = (ChattingRoomVO) value;
         
         
         
         
      
         
         //리턴할 객체에 둥근 프로필 이미지, 이름과, 상태 메세지 세팅
         String imgPath = null; 
         if(vo.getUserCount() > 2){
            imgPath = "images/icon_chat_group.png"; 
         }else if(vo.getUserCount() == 2 && vo.getImgPath() != null){
            imgPath = vo.getImgPath();
         }
         ImageIcon profileImage = imageMaker.getRoundedImage(imgPath, 70, 70);
         lbImgIcon.setIcon(profileImage);
         
         
         
         
         
         img = null;
         if (vo.getUserCount() > 2 ) {
            try {
               img = ImageIO.read(new File("images/GrupeMemberImg.png"));
            } catch (IOException e) {
               e.printStackTrace();
            }

            ImageIcon icon = new ImageIcon(img);
            Image image = icon.getImage();
            Image newImage = image.getScaledInstance(35, 20, Image.SCALE_SMOOTH);

            icon = new ImageIcon(newImage);

            groupIcon.setIcon(icon);
            groupIcon.setText(""+vo.getUserCount());
            groupIcon.setIconTextGap(-15);
         }else{
            groupIcon.setIcon(null);
            groupIcon.setText("");
            
         }
         unreadImg = null;
         if(vo.getUnReadMsgCount()>=1){
            
            
            try {
               unreadImg =  ImageIO.read(new File("images/Unreadmsg.png"));
            } catch (IOException e) {
               e.printStackTrace();
            }
            ImageIcon unreadIcon = new ImageIcon(unreadImg);
            Image unreadImage = unreadIcon.getImage();
            Image newUnreadImage = unreadImage.getScaledInstance(17, 17, Image.SCALE_SMOOTH);
            unreadIcon = new ImageIcon(newUnreadImage);
            
            unreadmsg.setIcon(unreadIcon);
            unreadmsg.setForeground(Color.white);
            unreadmsg.setText(vo.getUnReadMsgCount()+"");
            unreadmsg.setIconTextGap(-12);
            
         }else{
            unreadmsg.setIcon(null);
            unreadmsg.setText("");
         }
         
      
         
         
         
         if(vo.getChattingRoomName().length()>15){
            lbName.setText(vo.getChattingRoomName().substring(0,14)+"...");
            
         }else{
            lbName.setText(vo.getChattingRoomName());
         }
         
         
         if(vo.getLastMsgContent() != null ){
            try {
               times = formatter.parse(vo.getLasMsgSendTime());
            } catch (ParseException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            
         
            String formattedTime = formatter1.format(times);
            
            lastMsgTime.setText(formattedTime);
            System.out.println(vo.getLastMsgContent());
            if(vo.getLastMsgContent().length()>40){
               
               lbStatMsg.setText(vo.getLastMsgContent().substring(0, 39));
            }else{
               lbStatMsg.setText(vo.getLastMsgContent());
            }
            
         }else{
            lbStatMsg.setText("");
         }
         
         //투명도 설정
         lbImgIcon.setOpaque(true);
          lbName.setOpaque(true);
          lbStatMsg.setOpaque(true);
         panelText.setOpaque(true);
         
          // 선택됐을때 색상 변경
          if (isSelected) {
             lbImgIcon.setBackground(list.getSelectionBackground());
              lbName.setBackground(list.getSelectionBackground());
              lbStatMsg.setBackground(list.getSelectionBackground());
              panelText.setBackground(list.getSelectionBackground());
              brank.setBackground(list.getSelectionBackground());
              brank1.setBackground(list.getSelectionBackground());
              brank2.setBackground(list.getSelectionBackground());
              uppanel.setBackground(list.getSelectionBackground());
              downpanel.setBackground(list.getSelectionBackground());
              
              setBackground(list.getSelectionBackground());
          } else { 
             lbImgIcon.setBackground(list.getBackground());
             lbName.setBackground(list.getBackground());
             lbStatMsg.setBackground(list.getBackground());
              panelText.setBackground(list.getBackground());
              brank.setBackground(list.getBackground());
              brank1.setBackground(list.getBackground());
              brank2.setBackground(list.getBackground());
              uppanel.setBackground(list.getBackground());
              downpanel.setBackground(list.getBackground());
              setBackground(list.getBackground());
          }
          
         return this;
      }
   }

   public SearchPanel getpSearch() {
      return pSearch;
   }

   public void setpSearch(SearchPanel pSearch) {
      this.pSearch = pSearch;
   }

   public JTextField getTfSearch() {
      return tfSearch;
   }

   public void setTfSearch(JTextField tfSearch) {
      this.tfSearch = tfSearch;
   }

   public JList getJlFriendsList() {
      return jlFriendsList;
   }

   public void setJlFriendsList(JList jlFriendsList) {
      this.jlFriendsList = jlFriendsList;
   }

   public Map<String, UserVO> getSelectedFriends() {
      return selectedFriends;
   }

   public void setSelectedFriends(Map<String, UserVO> selectedFriends) {
      this.selectedFriends = selectedFriends;
   }
}