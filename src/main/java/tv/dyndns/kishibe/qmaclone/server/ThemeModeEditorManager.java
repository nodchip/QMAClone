package tv.dyndns.kishibe.qmaclone.server;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

public class ThemeModeEditorManager {
	private static final Logger logger = Logger.getLogger(ThemeModeEditorManager.class.toString());
	private static final String MAIL_USER_NAME = "nodchip@gmail.com";
	private static final String MAIL_PASSWORD_KEY = "mail.nodchip.gmail.com";
	private final Database database;

	@Inject
	public ThemeModeEditorManager(Database database) {
		this.database = Preconditions.checkNotNull(database);
	}

	public boolean isThemeModeEditor(int userCode) throws DatabaseException {
		return database.getThemeModeEditorsStatus(userCode) == ThemeModeEditorStatus.Accepted;
	}

	public void applyThemeModeEditor(int userCode, String text) throws DatabaseException,
			MessagingException {
		database.updateThemeModeEdtorsStatus(userCode, ThemeModeEditorStatus.Applying);

		// メール送信
		PacketUserData userData = database.getUserData(userCode);
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", "smtp.gmail.com");
		properties.setProperty("mail.smtp.socketFactory.port", "465");
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.port", "465");

		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				try {
					return new PasswordAuthentication(MAIL_USER_NAME, database
							.getPassword(MAIL_PASSWORD_KEY));
				} catch (DatabaseException e) {
					logger.log(Level.WARNING, "メールパスワードの読み込みに失敗しました", e);
					return null;
				}
			}
		});

		MimeMessage mimeMessage = new MimeMessage(session);
		try {
			mimeMessage.setFrom(new InternetAddress("nodchip@gmail.com",
					"QMAClone.ThemeModeEditorManager", "iso-2022-jp"));
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "サポートされていないエンコーディングです", e);
			throw Throwables.propagate(e);
		}
		mimeMessage.setRecipients(Message.RecipientType.TO, "nodchip@gmail.com");
		mimeMessage.setSubject("[QMAClone] テーマモード編集権限申請 " + userData.playerName, "iso-2022-jp");

		StringBuilder sb = new StringBuilder();
		sb.append("QMAClone管理者様:\n\n");
		sb.append("テーマモード編集権限の申請がありました。\n");
		sb.append("プレイヤーコード : ").append(userData.userCode).append("\n");
		sb.append("プレイヤー名 : ").append(userData.playerName).append("\n");
		sb.append("プレイ回数 : ").append(userData.playCount).append("\n");
		sb.append("レーティング : ").append(userData.rating).append("\n");
		sb.append("一言 : ").append(text).append("\n");
		sb.append("以上、よろしくお願いいたします。\n\n");
		sb.append("http://kishibe.dyndns.tv/QMAClone/#administratormode\n");

		mimeMessage.setText(sb.toString(), "iso-2022-jp");
		mimeMessage.setHeader("Content-Type", "text/plain");
		mimeMessage.setSentDate(new Date());
		Transport.send(mimeMessage);
	}

	public void acceptThemeModeEditor(int userCode) throws DatabaseException {
		database.updateThemeModeEdtorsStatus(userCode, ThemeModeEditorStatus.Accepted);
	}

	public void rejectThemeModeEditor(int userCode) throws DatabaseException {
		database.updateThemeModeEdtorsStatus(userCode, ThemeModeEditorStatus.Refected);
	}

	public List<PacketThemeModeEditor> getThemeModeEditors() throws DatabaseException {
		return database.getThemeModeEditors();
	}

	public boolean isApplyingThemeModeEditor(int userCode) throws DatabaseException {
		return database.getThemeModeEditorsStatus(userCode) == ThemeModeEditorStatus.Applying;
	}

	// public static void main(String[] args) throws DatabaseException, MessagingException {
	// Guice.createInjector(new
	// QMACloneModule()).getInstance(ThemeModeEditorManager.class).applyThemeModeEditor(12345678,
	// "hoge");
	// }
}
