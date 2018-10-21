package tv.dyndns.kishibe.qmaclone.client.chat;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

public class PanelRealtimeTest extends QMACloneGWTTestCaseBase {
	private PanelRealtime panel;
	private PacketChatMessage data;
	private PacketChatMessage last;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		panel = new PanelRealtime();
		data = new PacketChatMessage();
		last = new PacketChatMessage();
		UserData.get().setUserCode(0);
	}

	@Override
	protected void gwtTearDown() throws Exception {
		super.gwtTearDown();
	}

	// @Test
	// public void testShouldShowAcceptsPostFromThisUser() {
	// int userCode = 12345678;
	// String remoteAddress = "1.2.3.4";
	//
	// data.userCode = userCode;
	// data.remoteAddress = remoteAddress;
	//
	// UserData.get().setUserCode(userCode);
	// SharedData.get().setRemoveAddress(remoteAddress);
	//
	// assertTrue(panel.shouldShow(data, last));
	// }
	//
	// @Test
	// public void testShouldShowDeclinePostFromIgnoredUser() {
	// int userCode = 12345678;
	// String remoteAddress = "1.2.3.4";
	//
	// data.userCode = userCode;
	// data.remoteAddress = remoteAddress;
	//
	// UserData.get().setUserCode(0);
	// SharedData.get().setLimitedUserCodes(ImmutableSet.of(userCode));
	// SharedData.get().setLimitedRemoteAddresses(ImmutableSet.of(remoteAddress));
	//
	// assertFalse(panel.shouldShow(data, last));
	// }

	@Test
	public void testShouldShowAcceptPostIfItIsFirstPost() {
		last = null;
		data.userCode = 12345678;
		assertTrue(panel.shouldShow(data, last));
	}

	@Test
	public void testShouldShowDeclinePostWithSameContents() {
		String message = "message";
		int userCode = 12345678;
		String remoteAddress = "1.2.3.4";

		data.body = message;
		data.userCode = userCode;
		data.remoteAddress = remoteAddress;

		last.body = message;
		last.userCode = userCode;
		last.remoteAddress = remoteAddress;

		assertFalse(panel.shouldShow(data, last));
	}

	@Test
	public void testShouldShowDeclineSamePost() {
		String message = "message";
		data.resId = 10000;
		last.resId = 10000;

		data.userCode = 12345678;
		data.body = message;

		assertFalse(panel.shouldShow(data, last));
	}

	@Test
	public void testShouldShowDeclineOldPost() {
		String message = "message";
		data.remoteAddress = "remote addresss";
		data.resId = 9000;
		last.resId = 10000;
		++data.userCode;

		data.body = message;

		assertFalse(panel.shouldShow(data, last));
	}

	@Test
	public void testShouldShowRejectNgWords() {
		String message = "金太負けるな金太負けるな金玉蹴るな";
		data.body = message;
		data.resId = 1;
		data.userCode = 12345678;

		assertFalse(panel.shouldShow(data, last));
	}

	@Test
	public void testShouldShowAcceptNormalPost() {
		String message = "message";
		data.body = message;
		data.resId = 1;
		data.userCode = 12345678;

		assertTrue(panel.shouldShow(data, last));
	}

}
