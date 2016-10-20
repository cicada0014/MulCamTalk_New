package com.mc.mctalk.view.uiitem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.prompt.PromptSupport;

import com.mc.mctalk.dao.UserDAO;

public class PwFindDialog extends JDialog {
	private JTextField inputId = new JTextField(10);
	private JTextField inputPhoneNum = new JTextField(10);
	private JButton checkBtn = new JButton("확인");
	private JButton cancelBtn = new JButton("취소");
	private JPanel backColorPanel = new JPanel();
	private Font font = new Font("맑은 고딕", Font.PLAIN, 10);
	private LineBorder btnBorder = new LineBorder(Color.WHITE);
	private JLabel title = new JLabel("비밀번호 찾기");

	private findPwEmpty findPwEmp;
	private findPwFailed fpf;
	private showPw sp;

	public PwFindDialog() {

		Dimension frameSize = this.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - frameSize.width - 300) / 2,
				(screenSize.height - frameSize.height - 600) / 2);

		setLayout(null);

		title.setBounds(90, 15, 100, 20);
		title.setForeground(Color.WHITE);
		add(title);
		inputId.setBorder(BorderFactory.createEmptyBorder());
		PromptSupport.setPrompt("계정", inputId);
		inputId.setBounds(50, 40, 150, 20);
		add(inputId);

		inputPhoneNum.setBorder(BorderFactory.createEmptyBorder());
		PromptSupport.setPrompt("폰번호", inputPhoneNum);
		inputPhoneNum.setBounds(50, 75, 150, 20);
		add(inputPhoneNum);

		checkBtn.setContentAreaFilled(false);
		checkBtn.setForeground(Color.WHITE);
		checkBtn.setFont(font);
		checkBtn.setBorder(btnBorder);
		checkBtn.setBounds(50, 110, 70, 30);
		add(checkBtn);

		cancelBtn.setContentAreaFilled(false);
		cancelBtn.setForeground(Color.WHITE);
		cancelBtn.setFont(font);
		cancelBtn.setBorder(btnBorder);
		cancelBtn.setBounds(130, 110, 70, 30);
		add(cancelBtn);

		cancelBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		Color backColor = new Color(82, 134, 198);
		backColorPanel.setBackground(backColor);
		backColorPanel.setBounds(0, 0, 250, 200);
		add(backColorPanel);

		setSize(250, 200);
		setModal(true);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}

	public static void main(String[] args) {
		PwFindDialog pf = new PwFindDialog();
	}

	class pwFindListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == checkBtn);
			String ID = inputId.getText();
			System.out.println(ID);
			String phoneNum = inputPhoneNum.getText();
			System.out.println(phoneNum);

			if (ID == null || phoneNum == null) {
				findPwEmp = new findPwEmpty(e.getActionCommand() + "입력이 안 되었습니다");
			} else {
				UserDAO ud = new UserDAO();
				String jdb;
				jdb = ud.findPw(ID, phoneNum);
				if (jdb == null) {
					fpf = new findPwFailed(e.getActionCommand() + "ID와 PW재확인");
				} else {
					sp = new showPw(e.getActionCommand() + jdb);
					System.out.println(jdb);
					dispose();
				}
			}
		}
	}

	class findPwEmpty extends JDialog {

		JLabel message = new JLabel("");

		public findPwEmpty(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}
	}

	class findPwFailed extends JDialog {
		JLabel message = new JLabel("");

		public findPwFailed(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}
	}

	class showPw extends JDialog {
		JLabel message = new JLabel("");

		public showPw(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);

		}
	}

}