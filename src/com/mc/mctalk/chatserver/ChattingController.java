package com.mc.mctalk.chatserver;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.JPanel;

import com.mc.mctalk.dao.ChattingRoomDAO;
import com.mc.mctalk.view.ChattingFrame;
import com.mc.mctalk.vo.ChattingRoomVO;
import com.mc.mctalk.vo.UserVO;

/** 채팅 프레임 호출
 * 1.DB 적용해서 선택된 친구리스트의 친구 ID 가져와서 ID를 가져간 채팅 방 호출 필요
 * 2.방 불러오면서 방 객체, 방 멤버 객체 생성 및 값 설정 필요
 * 2.지난 대화 내역이 있다면 불러와지는 것도 구현??
 */

public class ChattingController {
	String TAG = "ChattingController : ";
	String loginID, friendID;
	UserVO friendVO;
	ChattingRoomDAO dao = new ChattingRoomDAO();
	ChattingClient client;
	LinkedHashMap<String, UserVO> selectedFriends;

	//채팅창 오픈용 생성자
	public ChattingController(ChattingClient client){
		this.client = client;
	}
	
	//1:1 채팅방 개설용 생성자
	public ChattingController(ChattingClient client, UserVO friendVO) {
		this.client = client;
		this.loginID = client.getLoginUserVO().getUserID();
		this.friendVO = friendVO;
		this.friendID = friendVO.getUserID();
		this.selectedFriends = new LinkedHashMap<String, UserVO>();
		this.selectedFriends.put(friendID, friendVO);
		hasChattingRoom();
	}

	//1:N 채팅방 개설용 생성자
	public ChattingController(ChattingClient client, LinkedHashMap<String, UserVO> selectedFriends) {
		this.client = client;
		this.loginID = client.getLoginUserVO().getUserID();
		this.selectedFriends = selectedFriends;
		String roomID = make1onNChattingRoom(false);
		openChattingRoom(roomID);
	}

	//1:1 채팅방 개설 여부 검사
	public void hasChattingRoom(){
		String roomID = dao.searchLastChatRoom(loginID, friendID);
		if(roomID!=null){
			//지난 대화 내역 불러오는 메소드 추가 필요
			openChattingRoom(roomID);
		}else{
			roomID = make1onNChattingRoom(true);
			openChattingRoom(roomID);
		}
	}

	//채팅방 만들기
	public String make1onNChattingRoom(boolean is1on1){
		String roomID = dao.makeChattingRoom(client, selectedFriends, is1on1);
		if(roomID!=null){
			System.out.println(TAG + "make1onNChattingRoom()");
			dao.addUserToChattingRoom(roomID, loginID);
			Iterator<Entry<String, UserVO>> entry = selectedFriends.entrySet().iterator();
			for(int i=0; i<selectedFriends.size();i++){
				dao.addUserToChattingRoom(roomID, entry.next().getValue().getUserID());
			}
		}
		return roomID;
	}
	
	public void openChattingRoom(String roomID) {
		System.out.println(TAG + "openChattingRoom()");
		ChattingFrame openedChattingGUI = client.getHtChattingGUI(roomID);
		
		// 채팅방 오픈 여부에 따른 분기 처리
		if (openedChattingGUI == null) {
			ChattingRoomVO roomVO = dao.getChatRoomVO(roomID);
			ChattingFrame cf = new ChattingFrame(client, roomVO);
			cf.loadMessageHistory();
			dao.deleteUnReadMsg(client.getLoginUserVO().getUserID(),roomID);
//			client.getHtMainFrame(client.getLoginUserVO().getUserID()).changePanel("chattingList");
		} else {
			openedChattingGUI.requestFocus();
			openedChattingGUI.setState(java.awt.Frame.NORMAL);
		}
	}
}
