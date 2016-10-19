package com.mc.mctalk.view.uiitem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.mc.mctalk.dao.UserDAO;

public class IdFindDialog extends JDialog {
	private JTextField inputName = new JTextField(10);
	private JTextField inputPhoneNum = new JTextField(10);
	private JButton checkBtn = new JButton("확인");
	private JButton cancelBtn = new JButton("취소");

	private findIdEmpty findIdEmp;
	private findIdFailed fif;
	private showId si;

	public IdFindDialog() {

		setLayout(null);

		inputName.setBounds(50, 50, 150, 20);
		add(inputName);
		inputPhoneNum.setBounds(50, 95, 150, 20);
		add(inputPhoneNum);
		checkBtn.setBounds(50, 130, 70, 30);
		add(checkBtn);
		checkBtn.addActionListener(new idFindListener());
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

	class idFindListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == checkBtn)
				;
			String name = inputName.getText();
			System.out.println(name);
			String phoneNum = inputPhoneNum.getText();
			System.out.println(phoneNum);

			if (name == null || phoneNum == null) {
				findIdEmp = new findIdEmpty(e.getActionCommand() + "입력이 안 되었습니다");
			} else {
				UserDAO ud = new UserDAO();
				String jdb;
				jdb = ud.findPw(name, phoneNum);
				if (jdb == null) {
					fif = new findIdFailed(e.getActionCommand() + "이름과 PW재확인");
				} else {
					si = new showId(e.getActionCommand() + jdb);
					System.out.println(jdb);
					dispose();
				}
			}
		}
	}

	class findIdEmpty extends JDialog {

		JLabel message = new JLabel("");

		public findIdEmpty(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}
	}

	class findIdFailed extends JDialog {
		JLabel message = new JLabel("");

		public findIdFailed(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}
	}

	class showId extends JDialog {
		JLabel message = new JLabel("");

		public showId(String str) {

			getContentPane().add(message);
			message.setText(str.toString());

			setSize(150, 150);
			setModal(true);
			setVisible(true);
		}
	}

}
