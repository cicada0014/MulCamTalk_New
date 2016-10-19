package com.mc.mctalk.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.prompt.PromptSupport;

import com.mc.mctalk.chatserver.ChattingClient;
import com.mc.mctalk.dao.UserDAO;
import com.mc.mctalk.view.uiitem.CustomTitlebar;
import com.mc.mctalk.view.uiitem.IdFindDialog;
import com.mc.mctalk.view.uiitem.LogoManager;
import com.mc.mctalk.view.uiitem.PwFindDialog;
import com.mc.mctalk.vo.UserVO;

public class LoginFrame extends JFrame {

	private JLabel labelLogo = new JLabel();
	private JLabel labelID = new JLabel();
	private JTextField loginID = new JTextField(10);
	private JLabel labelPW = new JLabel();
	private JPasswordField loginPW = new JPasswordField(10);
	private JButton loginBtn = new JButton("로그인");
	private JButton joinBtn = new JButton("아직 회원이 아니신가요?");
	private JButton findPWBtn = new JButton("비밀번호를 잊어버리셨나요?");
	private JButton findIDBtn = new JButton("계정을 잊어버리셨나요?");
	private JPanel allPanel = new JPanel();
	private Font font = new Font("맑은 고딕", Font.PLAIN, 10);
	private LineBorder btnBorder = new LineBorder(Color.WHITE);
	private LoginEmpty loginEmp;
	private LoginFailed lf;

	public LoginFrame() {
		setLayout(null);
		new LogoManager().setLogoFrame(this);

		// 창 화면 중간에 띄우기
		Dimension frameSize = this.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - frameSize.width - 300) / 2,
				(screenSize.height - frameSize.height - 600) / 2);

		this.setUndecorated(true);
		CustomTitlebar ct = new CustomTitlebar(this, null,true);
		ct.setBounds(0, 0, 380, 36);
		add(ct);

		String imgMPath = "images/logo_big.png";
		ImageIcon originMIcon = new ImageIcon(imgMPath);
		Image originMImg = originMIcon.getImage();
		Image changeMImg = originMImg.getScaledInstance(130, 130, Image.SCALE_SMOOTH);
		ImageIcon mIcon = new ImageIcon(changeMImg);
		labelLogo.setIcon(mIcon);
		labelLogo.setBounds(120, 70, 130, 130);
		add(labelLogo);

		String imgIdPath = "images/login_id.png";
		ImageIcon originIdIcon = new ImageIcon(imgIdPath);
		Image originIdImg = originIdIcon.getImage();
		Image changeIdImg = originIdImg.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
		ImageIcon IdIcon = new ImageIcon(changeIdImg);
		labelID.setIcon(IdIcon);
		labelID.setOpaque(true);
		labelID.setBackground(Color.WHITE);
		labelID.setBounds(70, 260, 30, 30);

		String imgPwPath = "images/login_pw.png";
		ImageIcon originPwIcon = new ImageIcon(imgPwPath);
		Image originPwImg = originPwIcon.getImage();
		Image changePwImg = originPwImg.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
		ImageIcon PwIcon = new ImageIcon(changePwImg);
		labelPW.setIcon(PwIcon);
		labelPW.setOpaque(true);
		labelPW.setBackground(Color.WHITE);
		labelPW.setBounds(70, 290, 30, 30);
		add(labelID);
		add(labelPW);

		loginID.setBorder(BorderFactory.createEmptyBorder());
		loginID.setBounds(100, 260, 200, 30);
		PromptSupport.setPrompt("계정", loginID);
		add(loginID);
		loginPW.setBorder(BorderFactory.createEmptyBorder());
		loginPW.setBounds(100, 290, 200, 30);
		PromptSupport.setPrompt("비밀번호", loginPW);
		add(loginPW);

		add(loginBtn);
		loginBtn.setBounds(70, 350, 230, 35);
		loginBtn.addActionListener(new ButtonActionListener());
		loginBtn.setBackground(new Color(104, 192, 152));
		loginBtn.setForeground(Color.WHITE);
		loginBtn.setFont(font);
		loginBtn.setBorder(BorderFactory.createEmptyBorder());

		joinBtn.setBounds(90, 420, 200, 30);
		joinBtn.setContentAreaFilled(false);
		joinBtn.setFont(font);
		joinBtn.setForeground(Color.WHITE);
		joinBtn.setBorder(btnBorder);
		joinBtn.addActionListener(new JoinListener());
		add(joinBtn);
		findIDBtn.setBounds(90, 460, 200, 30);
		findIDBtn.setContentAreaFilled(false);
		findIDBtn.addActionListener(new IdFindListener());
		findIDBtn.setFont(font);
		findIDBtn.setForeground(Color.WHITE);
		findIDBtn.setBorder(btnBorder);
		add(findIDBtn);
		findPWBtn.setBounds(90, 500, 200, 30);
		findPWBtn.setContentAreaFilled(false);
		findPWBtn.addActionListener(new PwFindListener());
		findPWBtn.setFont(font);
		findPWBtn.setForeground(Color.WHITE);
		findPWBtn.setBorder(btnBorder);
		add(findPWBtn);

		Color backColor = new Color(82, 134, 198);
		allPanel.setBackground(backColor);
		allPanel.setBounds(0, 36, 380, 564);
		add(allPanel);

		setSize(380, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

	}

	public class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == loginBtn) { // 로그인 버튼이 눌리면,
				UserDAO dao = new UserDAO();
				// DB 로그인
				String id = loginID.getText();
				String pw = loginPW.getText();
				ChattingClient client = dao.loginMember(id, pw);
				UserVO vo = client.getLoginUserVO();
				if (id == null || pw == null) {
					loginEmp = new LoginEmpty(e.getActionCommand() + "입력이 안 되었습니다");
				} else {
					if (vo.getUserID() != null) {
						System.out.println(vo.getUserID());
						MainFrame mainFrame = new MainFrame(client);
						dispose(); // 제대로 종료되게 변경 필요
					} else {
						lf = new LoginFailed(e.getActionCommand() + "ID와 PW재확인");
					}
				}
			}
		}
	}

	class JoinListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			MembershipFrame msf = new MembershipFrame();

		}

	}

	class PwFindListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == findPWBtn)
				;
			PwFindDialog pf = new PwFindDialog();

		}

	}

	class IdFindListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == findIDBtn) {
				IdFindDialog idf = new IdFindDialog();
			}
		}

	}

	class LoginEmpty extends JDialog {
		JLabel message = new JLabel("");

		public LoginEmpty(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}

	}

	class LoginFailed extends JDialog {
		JLabel message = new JLabel("");

		public LoginFailed(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}
	}

	public static void main(String[] args) {
		LoginFrame loginFrame = new LoginFrame();
	}
}