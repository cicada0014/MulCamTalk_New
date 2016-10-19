package com.mc.mctalk.view.uiitem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.mc.mctalk.dao.UserDAO;

public class PwFindDialog extends JDialog {
	private JTextField inputId = new JTextField(10);
	private JTextField inputPhoneNum = new JTextField(10);
	private JButton checkBtn = new JButton("확인");
	private JButton cancelBtn = new JButton("취소");

	private findPwEmpty findPwEmp;
	private findPwFailed fpf;
	private showPw sp;

	public PwFindDialog() {

		setLayout(null);

		inputId.setBounds(50, 50, 150, 20);
		add(inputId);
		inputPhoneNum.setBounds(50, 95, 150, 20);
		add(inputPhoneNum);
		checkBtn.setBounds(50, 130, 70, 30);
		add(checkBtn);
		checkBtn.addActionListener(new pwFindListener());
		cancelBtn.setBounds(130, 130, 70, 30);
		add(cancelBtn);
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();

			}
		});

		setSize(250, 250);
		setModal(true);
		setVisible(true);
		setResizable(false);
		setTitle("비밀번호 찾기");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}

	class pwFindListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == checkBtn)
				;
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
