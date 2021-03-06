package com.mc.mctalk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mc.mctalk.chatserver.ChattingClient;
import com.mc.mctalk.vo.UserVO;

public class UserDAO {
	private final String TAG = "UserDAO : ";
//	private String checkSQL = "select id from inbody_member where id = ? ";
	private String loginSQL = "select * from users where user_id = ? and user_pw = ? ";
	private String findPwSQL = "select user_pw from users where user_id =?and user_phone=?";
	private String findIdSQL = "select user_id from users where user_name=? and user_phone=?";
	private String searchAllFriendsSQL = "select ur.rel_user_id, u.user_name, u.user_pf_img_path, u.user_msg "
												+ "from user_relation ur, users u "
												+ "where ur.rel_user_id = u.user_id "
												+ "and ur.user_id = ? "
												+ "order by user_name";
	private String memberJoinSQL =  "insert into users (user_id,user_pw,user_name,user_sex,user_birthday,user_joindate) "
											  + "values(?,?,?,?,now(),now()) ";
	private String memberSearchSQL = "SELECT user_id, user_name, user_pf_img_path, user_msg "
												  + "from users " 
												  + "WHERE user_id NOT IN (SELECT rel_user_id from user_relation WHERE user_id = ?) "
												  + "and user_id != ? "
												  + "and user_name like ? ";
	private String memberAddSQL = "INSERT into user_relation (user_id,rel_user_id,rel_added_date) values(?,?,now()) ";
	private String idDuplicationCheckSQL = "SELECT user_id FROM users WHERE user_id=?";
	
	// 회원가입
	public boolean joinMember(UserVO memberinfoVO) {
		System.out.println(TAG + "joinMember()");
		boolean insertSucess = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(memberJoinSQL);

			stmt.setString(1, memberinfoVO.getUserID());
			stmt.setString(2, memberinfoVO.getUserPassword());
			stmt.setString(3, memberinfoVO.getUserName());
			stmt.setString(4, memberinfoVO.getUserSex() + "");

			int cnt = stmt.executeUpdate();
			if (cnt > 0) {
				System.out.println("Insert Success");
				insertSucess = true;
			}

		} catch (SQLException e) {
			System.out.println("Join e : " + e);
			insertSucess = false;
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}
		return insertSucess;

	}
	
	// 로그인
	public ChattingClient loginMember(String id, String pw) {
		System.out.println(TAG + "loginMember()");

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		ChattingClient client = null;
		UserVO vo = new UserVO();
		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(loginSQL);
			stmt.setString(1, id);
			stmt.setString(2, pw);
			rst = stmt.executeQuery();

			// 로그인 정보는 1개만 리턴하므로 while문이 필요없음
			if (rst.next()) {
				vo.setUserID(rst.getString(1));
				vo.setUserPassword(rst.getString(2));
				vo.setUserName(rst.getString(3));
				vo.setUserSex(rst.getInt(4));
				vo.setUserBirth(rst.getString(5));
				vo.setUserMail(rst.getString(6));
				vo.setUserPhone(rst.getString(7));
				vo.setUserAddress(rst.getString(8));
				vo.setUserJoinDate(rst.getString(9));
				vo.setUserImgPath(rst.getString(10));
			}

			if (vo.getUserID() != null) {
				System.out.println("Login User Info : " + vo.toString());
				client = new ChattingClient(vo);
			}

		} catch (SQLException e) {
			System.out.println("login e : " + e);
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}
		return client;
	}
	
	// 비밀번호찾기
	public String findPw(String id, String phoneNum) {
		System.out.println("findPW()");
		String pw_result = "";
		Connection conn = null;

		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(findPwSQL);
			stmt.setString(1, id);
			stmt.setString(2, phoneNum);
			rst = stmt.executeQuery();

			if (rst.next()) {
				pw_result = rst.getString(1);
				System.out.println(pw_result);
			}

		} catch (SQLException e) {
			System.out.println("find PW e : " + e);
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}

		return pw_result;
	}

	// 아이디 찾기
	public String findId(String name, String phoneNum) {
		System.out.println("findId()");
		String id_result = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(findIdSQL);
			stmt.setString(1, name);
			stmt.setString(2, phoneNum);
			rst = stmt.executeQuery();
			
			System.out.println("결과 " + rst);
			if (rst.next()) {
				id_result = rst.getString(1);
			}

		} catch (SQLException e) {
			System.out.println("find PW e : " + e);
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}

		return id_result;
	}
	
	//친구 추가시 회원 검색
	public Map<String, UserVO> SearchMember(String id, String searchName) {
		System.out.println(TAG + "SearchMember()");
		String id_result = null;
		Map<String, UserVO> searchMap = new LinkedHashMap<String, UserVO>();

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		UserVO vo = null;

		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(memberSearchSQL); // SQL 미리 컴파일,인수값 공간
															// 사전 확보
			stmt.setString(1, id);
			stmt.setString(2, id);
			stmt.setString(3, "%" + searchName + "%"); // 쿼리 URL 중 ?를 다른 변수로 치환
			rst = stmt.executeQuery(); // 쿼리 Execute

			while (rst.next()) { // 결과 집합에서 다음 레코드로 이동
				vo = new UserVO();
				vo.setUserID(rst.getString("user_id"));
				vo.setUserName(rst.getString("user_name"));
				vo.setUserImgPath(rst.getString("user_pf_img_path"));
				if(rst.getString("user_msg") != null)
				{
					vo.setUserMsg(rst.getString("user_msg"));
				}
				searchMap.put(vo.getUserID(), vo);
			}
		} catch (SQLException e) {
			System.out.println("login e : " + e);
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}
		return searchMap;
	}
	
	//친구추가
	public int AddFriend(String loginId, String addId) {
		System.out.println(TAG + "AddFriend()");
		String id_result = null;
		Map<String, UserVO> addMap = new LinkedHashMap<String, UserVO>();

		Connection conn = null;
		PreparedStatement stmt = null;
		UserVO vo = null;
		int rst = 0;

		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(memberAddSQL); // SQL 미리 컴파일,인수값 공간
															// 사전 확보
			stmt.setString(1, loginId); //등록할 ID
			stmt.setString(2, addId); //추가할 친구 ID(FriendsAddFrame의 listModel에서 rel_user_id만 따서 매개변수로 사용
//			stmt.setString(3, vo.getUserJoinDate());
			rst = stmt.executeUpdate(); // 쿼리 Execute

			System.out.println(rst);
		} catch (SQLException e) {
			System.out.println("login e : " + e);
		} finally {
			JDBCUtil.close(stmt, conn);
		}
		return rst;
	} 
		
	// 아이디중복 확인!!! 
	public boolean idDuplicationCheckDao(String id) {
		boolean check = true;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;

		try {
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(idDuplicationCheckSQL);
			stmt.setString(1, id);// SQL 미리 컴파일,인수값 공간
			rst = stmt.executeQuery(); // 쿼리 Execute
			while (rst.next()) { // 결과 집합에서 다음 레코드로 이동
				if(rst.getString(1).equals(id)){
					check = false;
					//중복되는 것이있다면 false값 리턴 
				}
			}
		} catch (SQLException e) {
			System.out.println("login e : " + e);
		} finally {
			JDBCUtil.close(rst, stmt, conn);
		}
		return check;
	}
	
	//친구 목록 불러오기
	public Map<String, UserVO> getAllFriendsMap(String id){
		System.out.println(TAG + "getAllFriendsMap()");
		Map<String, UserVO> friendsMap = new LinkedHashMap<String, UserVO>();
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		UserVO vo = null;
		int friendIndex = 0;

		try{
			conn = JDBCUtil.getConnection();
			stmt = conn.prepareStatement(searchAllFriendsSQL);
			stmt.setString(1, id);
			rst = stmt.executeQuery();
			
			while(rst.next()){
				vo = new UserVO();
				vo.setUserID(rst.getString("rel_user_id"));
				vo.setUserName(rst.getString("user_name"));
				vo.setUserImgPath(rst.getString("user_pf_img_path"));
				vo.setUserMsg(rst.getString("user_msg"));
				vo.setSelectedIndex(friendIndex);
				friendIndex++;
				friendsMap.put(vo.getUserID(), vo);
			}
//			System.out.println("searchFriend() : " + friendsList.size());
		}catch(SQLException e){
			System.out.println("getAllFriendsMap e : " + e);
		}finally {
			JDBCUtil.close(rst,stmt, conn);
		}
		return friendsMap;
	}

}
