package vn.ute.ui;

import vn.ute.model.Staff;
import vn.ute.model.Student;
import vn.ute.model.Teacher;
import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

public class PersonalInfoPanel extends JPanel {
	private enum ProfileType {
		STUDENT,
		TEACHER,
		STAFF,
		ACCOUNT_ONLY
	}

	private final ServiceManager serviceManager;
	private final SessionManager sessionManager;

	private JLabel lblWelcome;
	private JLabel lblUsername;
	private JLabel lblRole;
	private JLabel lblActive;

	private JTextField txtFullName;
	private JTextField txtPhone;
	private JTextField txtEmail;

	private JPanel studentOnlyPanel;
	private JPanel teacherOnlyPanel;
	private JPanel staffOnlyPanel;

	private JTextField txtDateOfBirth;
	private JComboBox<Student.Gender> cbGender;
	private JTextField txtRegistrationDate;
	private JTextArea txtAddress;

	private JTextField txtHireDate;
	private JTextField txtSpecialty;

	private JComboBox<Staff.StaffRole> cbStaffRole;

	private JCheckBox chkProfileActive;

	private JButton btnEdit;
	private JButton btnSave;
	private JButton btnCancel;
	private JButton btnChangePassword;

	private ProfileType profileType = ProfileType.ACCOUNT_ONLY;
	private Student student;
	private Teacher teacher;
	private Staff staff;

	public PersonalInfoPanel(ServiceManager serviceManager, SessionManager sessionManager) {
		this.serviceManager = serviceManager;
		this.sessionManager = sessionManager;
		setLayout(new BorderLayout());
		setOpaque(false);

		buildUI();
		reloadProfileData();
		setEditMode(false);
	}

	private void buildUI() {
		JPanel root = new JPanel();
		root.setOpaque(false);
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

		root.add(buildHeaderCard());
		root.add(Box.createVerticalStrut(14));
		root.add(buildAccountCard());
		root.add(Box.createVerticalStrut(14));
		root.add(buildProfileCard());
		root.add(Box.createVerticalStrut(14));
		root.add(buildActionBar());

		JScrollPane scrollPane = new JScrollPane(root);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(UIUtils.APP_BG);
		add(scrollPane, BorderLayout.CENTER);
	}

	private JComponent buildHeaderCard() {
		JPanel card = new GradientHeaderPanel();
		card.setLayout(new BorderLayout());
		card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

		lblWelcome = new JLabel("Thong tin ca nhan", SwingConstants.LEFT);
		lblWelcome.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 22f));
		lblWelcome.setForeground(Color.WHITE);

		JLabel subtitle = new JLabel("Cap nhat ho so nhanh gon, de doc, de su dung", SwingConstants.LEFT);
		subtitle.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 13f));
		subtitle.setForeground(new Color(219, 236, 255));

		JPanel textBox = new JPanel();
		textBox.setOpaque(false);
		textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
		textBox.add(lblWelcome);
		textBox.add(Box.createVerticalStrut(4));
		textBox.add(subtitle);

		card.add(textBox, BorderLayout.WEST);
		return card;
	}

	private JComponent buildAccountCard() {
		JPanel card = createCardPanel();
		card.setLayout(new GridBagLayout());

		lblUsername = new JLabel("-");
		lblRole = new JLabel("-");
		lblActive = new JLabel("-");

		GridBagConstraints gbc = defaultGbc();
		int row = 0;
		row = addInfoRow(card, gbc, row, "Ten dang nhap", lblUsername);
		row = addInfoRow(card, gbc, row, "Vai tro", lblRole);
		addInfoRow(card, gbc, row, "Trang thai tai khoan", lblActive);

		return card;
	}

	private JComponent buildProfileCard() {
		JPanel card = createCardPanel();
		card.setLayout(new GridBagLayout());

		txtFullName = new JTextField(28);
		txtPhone = new JTextField(28);
		txtEmail = new JTextField(28);
		chkProfileActive = new JCheckBox("Dang hoat dong");

		txtDateOfBirth = new JTextField(28);
		cbGender = new JComboBox<>(Student.Gender.values());
		txtRegistrationDate = new JTextField(28);
		txtAddress = new JTextArea(3, 28);
		txtAddress.setLineWrap(true);
		txtAddress.setWrapStyleWord(true);

		txtHireDate = new JTextField(28);
		txtSpecialty = new JTextField(28);

		cbStaffRole = new JComboBox<>(Staff.StaffRole.values());

		GridBagConstraints gbc = defaultGbc();
		int row = 0;
		row = addFieldRow(card, gbc, row, "Ho va ten", txtFullName);
		row = addFieldRow(card, gbc, row, "So dien thoai", txtPhone);
		row = addFieldRow(card, gbc, row, "Email", txtEmail);

		studentOnlyPanel = new JPanel(new GridBagLayout());
		studentOnlyPanel.setOpaque(false);
		GridBagConstraints sGbc = defaultGbc();
		int sRow = 0;
		sRow = addFieldRow(studentOnlyPanel, sGbc, sRow, "Ngay sinh (yyyy-MM-dd)", txtDateOfBirth);
		sRow = addFieldRow(studentOnlyPanel, sGbc, sRow, "Gioi tinh", cbGender);
		sRow = addFieldRow(studentOnlyPanel, sGbc, sRow, "Ngay dang ky", txtRegistrationDate);
		addFieldRow(studentOnlyPanel, sGbc, sRow, "Dia chi", new JScrollPane(txtAddress));

		teacherOnlyPanel = new JPanel(new GridBagLayout());
		teacherOnlyPanel.setOpaque(false);
		GridBagConstraints tGbc = defaultGbc();
		int tRow = 0;
		tRow = addFieldRow(teacherOnlyPanel, tGbc, tRow, "Ngay vao lam (yyyy-MM-dd)", txtHireDate);
		addFieldRow(teacherOnlyPanel, tGbc, tRow, "Chuyen mon", txtSpecialty);

		staffOnlyPanel = new JPanel(new GridBagLayout());
		staffOnlyPanel.setOpaque(false);
		addFieldRow(staffOnlyPanel, defaultGbc(), 0, "Vai tro nhan su", cbStaffRole);

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		card.add(studentOnlyPanel, gbc);

		row++;
		gbc.gridy = row;
		card.add(teacherOnlyPanel, gbc);

		row++;
		gbc.gridy = row;
		card.add(staffOnlyPanel, gbc);

		row++;
		gbc.gridy = row;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		card.add(new JLabel("Trang thai ho so"), gbc);

		gbc.gridx = 1;
		card.add(chkProfileActive, gbc);

		return card;
	}

	private JComponent buildActionBar() {
		JPanel actions = createCardPanel();
		actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

		btnEdit = UIUtils.createSecondaryButton("Chinh sua");
		btnSave = UIUtils.createPrimaryButton("Luu");
		btnCancel = UIUtils.createSecondaryButton("Huy");
		btnChangePassword = UIUtils.createSecondaryButton("Doi mat khau");

		btnEdit.addActionListener(e -> setEditMode(true));
		btnSave.addActionListener(e -> saveProfile());
		btnCancel.addActionListener(e -> {
			reloadProfileData();
			setEditMode(false);
		});
		btnChangePassword.addActionListener(e -> changePassword());

		actions.add(btnChangePassword);
		actions.add(btnEdit);
		actions.add(btnCancel);
		actions.add(btnSave);
		return actions;
	}

	private JPanel createCardPanel() {
		JPanel card = new JPanel();
		card.setOpaque(true);
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(206, 218, 232)),
				BorderFactory.createEmptyBorder(14, 16, 14, 16)
		));
		return card;
	}

	private GridBagConstraints defaultGbc() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 4, 6, 8);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		return gbc;
	}

	private int addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valueLabel) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		panel.add(createFieldLabel(label), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		valueLabel.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 14f));
		valueLabel.setForeground(new Color(40, 62, 85));
		panel.add(valueLabel, gbc);
		return row + 1;
	}

	private int addFieldRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		panel.add(createFieldLabel(label), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(field, gbc);
		return row + 1;
	}

	private JLabel createFieldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 13f));
		label.setForeground(new Color(64, 82, 103));
		return label;
	}

	private void reloadProfileData() {
		UserAccount currentUser = sessionManager.getCurrentUser();
		if (currentUser == null) {
			return;
		}

		lblUsername.setText(currentUser.getUsername());
		lblRole.setText(currentUser.getRole() != null ? currentUser.getRole().name() : "-");
		lblActive.setText(currentUser.isActive() ? "Dang hoat dong" : "Da khoa");

		student = null;
		teacher = null;
		staff = null;

		try {
			if (currentUser.getStudent() != null) {
				student = serviceManager.getStudentService().findById(currentUser.getStudent().getId()).orElse(currentUser.getStudent());
				profileType = ProfileType.STUDENT;
				fillStudent(student);
			} else if (currentUser.getTeacher() != null) {
				teacher = serviceManager.getTeacherService().findById(currentUser.getTeacher().getId()).orElse(currentUser.getTeacher());
				profileType = ProfileType.TEACHER;
				fillTeacher(teacher);
			} else if (currentUser.getStaff() != null) {
				staff = serviceManager.getStaffService().findById(currentUser.getStaff().getId()).orElse(currentUser.getStaff());
				profileType = ProfileType.STAFF;
				fillStaff(staff);
			} else {
				profileType = ProfileType.ACCOUNT_ONLY;
				clearFormFields();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Khong the tai thong tin ca nhan: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
		}

		applyTypeVisibility();
		lblWelcome.setText(buildWelcomeTitle(currentUser));
	}

	private String buildWelcomeTitle(UserAccount account) {
		String role = account.getRole() != null ? account.getRole().name() : "User";
		return "Thong tin ca nhan - " + role;
	}

	private void clearFormFields() {
		txtFullName.setText("");
		txtPhone.setText("");
		txtEmail.setText("");
		txtDateOfBirth.setText("");
		cbGender.setSelectedItem(null);
		txtRegistrationDate.setText("");
		txtAddress.setText("");
		txtHireDate.setText("");
		txtSpecialty.setText("");
		cbStaffRole.setSelectedItem(Staff.StaffRole.Other);
		chkProfileActive.setSelected(true);
	}

	private void fillStudent(Student s) {
		txtFullName.setText(s.getFullName());
		txtPhone.setText(nullToEmpty(s.getPhone()));
		txtEmail.setText(nullToEmpty(s.getEmail()));
		txtDateOfBirth.setText(s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "");
		cbGender.setSelectedItem(s.getGender());
		txtRegistrationDate.setText(s.getRegistrationDate() != null ? s.getRegistrationDate().toString() : "");
		txtAddress.setText(nullToEmpty(s.getAddress()));
		chkProfileActive.setSelected(s.getStatus() == Student.Status.Active);
	}

	private void fillTeacher(Teacher t) {
		txtFullName.setText(t.getFullName());
		txtPhone.setText(nullToEmpty(t.getPhone()));
		txtEmail.setText(nullToEmpty(t.getEmail()));
		txtHireDate.setText(t.getHireDate() != null ? t.getHireDate().toString() : "");
		txtSpecialty.setText(nullToEmpty(t.getSpecialty()));
		chkProfileActive.setSelected(t.getStatus() == Teacher.Status.Active);
	}

	private void fillStaff(Staff s) {
		txtFullName.setText(s.getFullName());
		txtPhone.setText(nullToEmpty(s.getPhone()));
		txtEmail.setText(nullToEmpty(s.getEmail()));
		cbStaffRole.setSelectedItem(s.getRole());
		chkProfileActive.setSelected(s.getStatus() == Staff.Status.Active);
	}

	private void applyTypeVisibility() {
		studentOnlyPanel.setVisible(profileType == ProfileType.STUDENT);
		teacherOnlyPanel.setVisible(profileType == ProfileType.TEACHER);
		staffOnlyPanel.setVisible(profileType == ProfileType.STAFF);
		revalidate();
		repaint();
	}

	private void setEditMode(boolean editing) {
		boolean hasProfile = profileType != ProfileType.ACCOUNT_ONLY;

		txtFullName.setEditable(editing && hasProfile);
		txtPhone.setEditable(editing && hasProfile);
		txtEmail.setEditable(editing && hasProfile);

		txtDateOfBirth.setEditable(editing && profileType == ProfileType.STUDENT);
		cbGender.setEnabled(editing && profileType == ProfileType.STUDENT);
		txtRegistrationDate.setEditable(false);
		txtAddress.setEditable(editing && profileType == ProfileType.STUDENT);

		txtHireDate.setEditable(editing && profileType == ProfileType.TEACHER);
		txtSpecialty.setEditable(editing && profileType == ProfileType.TEACHER);

		cbStaffRole.setEnabled(false);
		chkProfileActive.setEnabled(false);

		btnEdit.setEnabled(!editing && hasProfile);
		btnSave.setVisible(editing && hasProfile);
		btnCancel.setVisible(editing && hasProfile);
	}

	private void saveProfile() {
		try {
			validateCommonFields();

			if (profileType == ProfileType.STUDENT && student != null) {
				student.setFullName(txtFullName.getText().trim());
				student.setPhone(normalize(txtPhone.getText()));
				student.setEmail(normalize(txtEmail.getText()));
				student.setDateOfBirth(parseDateOrNull(txtDateOfBirth.getText()));
				student.setGender((Student.Gender) cbGender.getSelectedItem());
				student.setAddress(normalize(txtAddress.getText()));
				serviceManager.getStudentService().updateStudent(student);
			} else if (profileType == ProfileType.TEACHER && teacher != null) {
				teacher.setFullName(txtFullName.getText().trim());
				teacher.setPhone(normalize(txtPhone.getText()));
				teacher.setEmail(normalize(txtEmail.getText()));
				teacher.setHireDate(parseDateOrNull(txtHireDate.getText()));
				teacher.setSpecialty(normalize(txtSpecialty.getText()));
				serviceManager.getTeacherService().updateTeacher(teacher);
			} else if (profileType == ProfileType.STAFF && staff != null) {
				staff.setFullName(txtFullName.getText().trim());
				staff.setPhone(normalize(txtPhone.getText()));
				staff.setEmail(normalize(txtEmail.getText()));
				serviceManager.getStaffService().updateStaff(staff);
			} else {
				JOptionPane.showMessageDialog(this, "Tai khoan hien tai khong co ho so de cap nhat.");
				return;
			}

			refreshSessionUser();
			reloadProfileData();
			setEditMode(false);
			JOptionPane.showMessageDialog(this, "Cap nhat thong tin ca nhan thanh cong.");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Khong the luu thong tin: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void validateCommonFields() throws Exception {
		if (txtFullName.getText() == null || txtFullName.getText().trim().isEmpty()) {
			throw new Exception("Ho va ten khong duoc de trong.");
		}
	}

	private void refreshSessionUser() {
		UserAccount current = sessionManager.getCurrentUser();
		if (current == null) {
			return;
		}
		try {
			serviceManager.getUserAccountService().findById(current.getId()).ifPresent(sessionManager::setCurrentUser);
		} catch (Exception ignored) {
		}
	}

	private void changePassword() {
		UserAccount current = sessionManager.getCurrentUser();
		if (current == null) {
			return;
		}

		JPasswordField newPasswordField = new JPasswordField(24);
		JPasswordField confirmPasswordField = new JPasswordField(24);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = defaultGbc();
		addFieldRow(panel, gbc, 0, "Mat khau moi", newPasswordField);
		addFieldRow(panel, gbc, 1, "Nhap lai mat khau", confirmPasswordField);

		int option = JOptionPane.showConfirmDialog(
				this,
				panel,
				"Doi mat khau",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
		);

		if (option != JOptionPane.OK_OPTION) {
			return;
		}

		String newPassword = new String(newPasswordField.getPassword());
		String confirmPassword = new String(confirmPasswordField.getPassword());

		if (newPassword.length() < 6) {
			JOptionPane.showMessageDialog(this, "Mat khau moi phai co it nhat 6 ky tu.", "Canh bao", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!newPassword.equals(confirmPassword)) {
			JOptionPane.showMessageDialog(this, "Mat khau nhap lai khong khop.", "Canh bao", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			serviceManager.getUserAccountService().updatePassword(current.getId(), newPassword);
			JOptionPane.showMessageDialog(this, "Doi mat khau thanh cong.");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Doi mat khau that bai: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
		}
	}

	private LocalDate parseDateOrNull(String raw) {
		String normalized = normalize(raw);
		return normalized == null ? null : LocalDate.parse(normalized);
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static class GradientHeaderPanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				GradientPaint paint = new GradientPaint(
						0, 0, new Color(17, 86, 165),
						getWidth(), getHeight(), new Color(68, 154, 235)
				);
				g2.setPaint(paint);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
			} finally {
				g2.dispose();
			}
			super.paintComponent(g);
		}

		private GradientHeaderPanel() {
			setOpaque(false);
		}
	}
}
