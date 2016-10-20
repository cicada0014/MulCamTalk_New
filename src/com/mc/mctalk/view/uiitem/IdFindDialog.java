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

public class IdFindDialog extends JDialog {
private JTextField inputName = new JTextField(10);
private JTextField inputPhoneNum = new JTextField(10);
private JButton checkBtn = new JButton("확인");
private JButton cancelBtn = new JButton("취소");
private Font font = new Font("맑은 고딕", Font.PLAIN, 10);
private LineBorder btnBorder = new LineBorder(Color.WHITE);
private JPanel backColorPanel = new JPanel();
private JLabel title = new JLabel("계정 찾기");

private findIdEmpty findIdEmp;
private findIdFailed fif;
private showId si;

public IdFindDialog() {
Dimension frameSize = this.getSize();
Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
this.setLocation((screenSize.width - frameSize.width - 300) / 2,
(screenSize.height - frameSize.height - 600) / 2);

setLayout(null);

title.setBounds(90, 15, 100, 20);
title.setForeground(Color.WHITE);
add(title);

inputName.setBorder(BorderFactory.createEmptyBorder());
PromptSupport.setPrompt("이름", inputName);
inputName.setBounds(50, 40, 150, 20);
add(inputName);

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
checkBtn.addActionListener(new idFindListener());

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
IdFindDialog pf = new IdFindDialog();
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