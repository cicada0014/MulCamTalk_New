package com.mc.mctalk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mc.mctalk.chatserver.ChattingClient;
import com.mc.mctalk.vo.ChattingRoomVO;
import com.mc.mctalk.vo.MessageVO;
import com.mc.mctalk.vo.UserVO;
import com.mysql.jdbc.Statement;

public class ChattingRoomDAO {
	private final String TAG = "ChattingRoomDAO : ";
	private String searchLastChatRoomSQL =  "SELECT room_id "
															+ "FROM chat_room_users "
															+ "WHERE room_id in( "
															+ "	select "
															+ "	me.room_id "
															+ "	from( select room_id "
															+ "		from chat_room_users cru "
															+ "		where cru_left_time is null "
															+ "		and user_id = ? "
															+ "		)me, "
															+ "		(select room_id "
															+ "		from chat_room_users cru "
															+ "		where cru_left_time is null "
															+ "		and user_id = ? "
															+ "		)other "
															+ "	where me.room_id = other.room_id ) "
															+ "GROUP BY room_id "
															+ "HAVING count(room_id) = 2 ";
	private String make1onNChattingRoomSQL = "insert into chat_rooms "
																+ "(room_created_time, room_name) "
																+ "values(now(), ?)";
	private String addUserToChattingRoomSQL = "insert into chat_room_users "
																+ "(room_id, user_id, cru_entered_time) "
																+ "values(?, ?, now())";
	private String searchChatRoomUsersSQL = "select user_id "
															 + "from users " 
															 + "where user_id in (select user_id " 
															 + "from chat_room_users "
															 + "where room_id = ?) ";	 
	private String searchChatRoomInfoSQL = "select room_id, room_name from chat_rooms where room_id = ? ";	 			
	private String insertMessageToDBSQL = "INSERT INTO messages (room_id,msg_sent_user_id,"
														+ "msg_content,msg_sent_time) values (? ,? ,? ,?)";
	private String insertDisconnClientSQL = "INSERT INTO disconn_client (msg_id,disconn_client_id) values (?,?)"; 
	private String searchChatRoomListSQL =  "SELECT cr.room_id, cr.room_name, cru.user_cnt, ms.msg_content last_msg_content, ms.msg_sent_time last_msg_sent_time, dc.unread_msg_cnt, path.path "
													 	 + "FROM chat_rooms cr "
														 + "LEFT JOIN (select room_id, count(user_id) user_cnt from chat_room_users where cru_left_time is null "
														 + "				  and room_id in (select room_id from chat_room_users where user_id = ?) group by room_id) cru "
														 + "		ON	cr.room_id = cru.room_id "
														 + "LEFT JOIN (select msg_id, room_id, msg_content, msg_sent_time " 
												 		 + " 				from (select  @ROWNUM := @ROWNUM + 1 AS ROWNUM, ms.* " 
										 		 	     + " 					     from messages ms, (SELECT @ROWNUM := 0) R "
									 		 	     	 + " 						order by msg_sent_time desc) f "
									 		 	     	 + "				group by room_id "
									 		 	     	 + "				order by msg_sent_time desc) ms "
									 		 	     	 + "		ON cr.room_id = ms.room_id "
														 + "LEFT JOIN (select ms.room_id, count(ms.msg_id) unread_msg_cnt from disconn_client dc, messages ms	where dc.msg_id = ms.msg_id and ms.msg_sent_user_id != ? group by ms.room_id) dc "
														 + "		ON cr.room_id = dc.room_id "
														 + "LEFT JOIN (select cru.room_id, (select user_pf_img_path from users where user_id = cru.user_id) path "
														 + "				from chat_room_users cru "
														 + "				where cru_left_time is null "
														 + "				and room_id in (select room_id from chat_room_users where user_id = ?) "
														 + "				and user_id != ? "
														 + "				group by room_id "
														 + "				having count(user_id) = 1) path "
														 + "		ON cr.room_id = path.room_id "
														 + "WHERE cru.user_cnt is not null "
														 + "GROUP BY cr.room_id "
														 + "ORDER BY ms.msg_sent_time desc, cr.room_id desc ";
	private String getChatRoomMessagesSQL = "SELECT ms.room_id, "
															 + "		 ms.msg_id,"
															 + "		 ms.msg_sent_user_id, "
															 + "	 	 (select user_name from users where user_id = ms.msg_sent_user_id) msg_sent_user_name, "
															 + "		 ms.msg_content, "
															 + "		 ms.msg_sent_time  "
															 + "FROM messages ms "
															 + "WHERE room_id = ? " 
															 + "ORDER BY msg_sent_time asc, msg_id asc ";
	private String deleteUnreadMessageSQL = "DELETE FROM disconn_client WHERE disconn_client_id = ? AND msg_id in (select msg_id from messages where room_id = ?) ";
	
	
	//접속하지 않은 유저에게 메시지 전송시 관련 정보 입력
	public void insertDiconnClient(String msg_id, String disconnClient){
		Connection conn = null;
		PreparedStatement stmt = null;
		conn = JDBCUtil.getConnection();
		try {
			stmt = conn.prepareStatement(insertDisconnClientSQL);
			stmt.setString(1, msg_id);
			stmt.setString(2, disconnClient);
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCUtil.close(stmt, conn);
		}
	}
	
	//접속 여부에 관계 없이 메시지 DB 입력
	public String insertMessageToDB(MessageVO msg)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		String messageID = null;
		conn = JDBCUtil.getConnection();
		try {
			if(msg.getMessageID()!=null){
				insertDiconnClient(msg.getMessageID(), msg.getSendUserID());
				System.out.println(" 반송!"+msg.getSendUserID()+"에게 보냈으나  채팅방을 안열어 반송되었음. disconn 디비에 저장합니다. ");
				throw new Exception();
			}else{
				stmt = conn.prepareStatement(insertMessageToDBSQL, Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, msg.getRoomVO().getChattingRoomID());
				stmt.setString(2, msg.getSendUserID());
				stmt.setString(3, msg.getMessage());
				stmt.setString(4, msg.getSendTime());
				int cnt = stmt.executeUpdate();
				if(cnt>0){
					rst = stmt.getGeneratedKeys();
					if(rst.next()){
						System.out.println("Message Insert Success");
						messageID = rst.getString(1);
						System.out.println("메세지 ID  : " + messageID);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCUtil.close(stmt, conn);
		}
		return messageID;
	}
	
	//기존에 1:1 채팅방을 만든적이 있는지 검색하기
	public String searchLastChatRoom(String loginID, String friendID){
		System.out.println(TAG + "searchLastChatRoom()");
		String roomID = null; 
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(searchLastChatRoomSQL);
			stmt.setString(1, loginID);
			stmt.setString(2, friendID);
			rst = stmt.executeQuery();
			
			//로그인 정보는 1개만 리턴하므로 while문이 필요없음
			if(rst.next()){
				roomID = rst.getString(1);
			}
			
		}catch(SQLException e){
			System.out.println("searchLastChatRoom e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return roomID;
	}
	
	// 채팅방 만들기
	public String makeChattingRoom(ChattingClient client, LinkedHashMap<String, UserVO> lastSelected, boolean is1on1){
		System.out.println(TAG + "makeChattingRoom()");
		String roomID = null;
		String friendNames = client.getLoginUserVO().getUserName();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(make1onNChattingRoomSQL, Statement.RETURN_GENERATED_KEYS);
			Iterator<Entry<String, UserVO>> entry = lastSelected.entrySet().iterator();
			for(int i =0 ; i<lastSelected.size(); i++){
				friendNames += "/"+ entry.next().getValue().getUserName();
			}
			
			stmt.setString(1, friendNames);// 방이름이 될 것이다. 
			int cnt = stmt.executeUpdate();

			if(cnt > 0){
				rst = stmt.getGeneratedKeys();
				if(rst.next()){
					System.out.println("Chat Room Insert Success");
					roomID = rst.getString(1);
					System.out.println("만들어진 방 ID : " + roomID);
				}
			}		
		}catch(SQLException e){
			System.out.println("make1on1ChattingRoom e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return roomID;
	}
	
	//채팅방에 참여하는 유저 추가하기
	public boolean addUserToChattingRoom(String roomID, String userID){
		System.out.println(TAG + "addUserToChattingRoom()");
		boolean isSuceed = false; 
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(addUserToChattingRoomSQL);
			stmt.setString(1, roomID);
			stmt.setString(2, userID);

			int cnt = stmt.executeUpdate();
			if(cnt > 0){
				System.out.println("Chat User Insert Success");
				isSuceed = true;
			}		
			
		}catch(SQLException e){
			System.out.println("addUserToChattingRoom e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return isSuceed;
	}
	
	//로그인한 유저의 방목록 리스트
	public Map<String, ChattingRoomVO> getAllChatRoomVOMap(String loginID){
		System.out.println(TAG + "getAllChatRoomVOMap()");
		System.out.println("로그인ID : " + loginID);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		Map<String, ChattingRoomVO> roomVOMap = new LinkedHashMap<String, ChattingRoomVO>();
		
		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(searchChatRoomListSQL);
			stmt.setString(1, loginID);
			stmt.setString(2, loginID);
			stmt.setString(3, loginID);
			stmt.setString(4, loginID);

			rst = stmt.executeQuery();
			
			while(rst.next()){
				ChattingRoomVO roomVO = new ChattingRoomVO(); 
				roomVO.setChattingRoomID(rst.getString(1));
				roomVO.setChattingRoomName(rst.getString(2));
				roomVO.setUserCount(rst.getInt(3));
				roomVO.setLastMsgContent(rst.getString(4));
				roomVO.setLasMsgSendTime(rst.getString(5));
				roomVO.setUnReadMsgCount(rst.getInt(6));
				roomVO.setImgPath(rst.getString(7));
				roomVO.setChattingRoomUserIDs(getChattingRoomUserList(rst.getString(1)));
				roomVOMap.put(roomVO.getChattingRoomID(), roomVO);
			}
		}catch(SQLException e){
			System.out.println("getAllChatRoomVOMap e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return roomVOMap;
	}
	
	//채팅 방 한개에 대한 정보 검색하기(채팅창 오픈용)
	public ChattingRoomVO getChatRoomVO(String roomID){
		System.out.println(TAG + "getChatRoomVO()");
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		ChattingRoomVO roomVO = new ChattingRoomVO(); 
		
		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(searchChatRoomInfoSQL);
			stmt.setString(1, roomID);
			rst = stmt.executeQuery();

			if (rst.next()) {
				roomVO.setChattingRoomID(roomID);
				roomVO.setChattingRoomName(rst.getString(2));
				roomVO.setChattingRoomUserIDs(getChattingRoomUserList(roomID));
			}
			
		}catch(SQLException e){
			System.out.println("addUserToChattingRoom e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return roomVO;
	}
	
	//방에 참여한 유저ID 리스트
	public ArrayList<String> getChattingRoomUserList(String roomID){
//		System.out.println(TAG + "getUserListChattingRoom()");
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		ArrayList<String> listChattingUserIDs = new ArrayList<>();

		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(searchChatRoomUsersSQL);
			stmt.setString(1, roomID);
			rst = stmt.executeQuery();
			
			while(rst.next()){
				listChattingUserIDs.add(rst.getString(1));
			}
		}catch(SQLException e){
			System.out.println("getAllChatRoomVOMap e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return listChattingUserIDs;
	}
	
	//채팅 방 한개에 대한 메시지 이력 불러오기
	public ArrayList<MessageVO> getChatRoomMessageArray(String roomID){
		System.out.println(TAG + "getChatRoomMessages()");
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		ArrayList<MessageVO> MessageVOArray = new ArrayList<MessageVO>();

		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(getChatRoomMessagesSQL);
			stmt.setString(1, roomID);
			rst = stmt.executeQuery();

			while (rst.next()) {
				MessageVO messageVO = new MessageVO(); 
				ChattingRoomVO roomVO = new ChattingRoomVO(); 
				roomVO.setChattingRoomID(rst.getString(1));
				messageVO.setRoomVO(roomVO);
				messageVO.setMessageID(rst.getString(2));
				messageVO.setSendUserID(rst.getString(3));
				messageVO.setSendUserName(rst.getString(4));
				messageVO.setMessage(rst.getString(5));
				messageVO.setSendTime(rst.getString(6));
				MessageVOArray.add(messageVO);
			}
			
		}catch(SQLException e){
			System.out.println("addUserToChattingRoom e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return MessageVOArray;
	}

	public void deleteUnReadMsg(String userID, String roomID) {
		System.out.println(TAG + "deleteUnReadMsg()");

		int result = 0;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;

		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(deleteUnreadMessageSQL);
			stmt.setString(1, userID);
			stmt.setString(2, roomID);
			result = stmt.executeUpdate();

			System.out.println("딜리트 결과 : " + result);
			if (result > 0) {
				System.out.println("unread message delete success");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}
	}
}
