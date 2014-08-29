//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingPlayer;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;

public class QMAClone implements EntryPoint {
	private static final Logger logger = Logger.getLogger(QMAClone.class.getName());
	private static final String ADMINISTRATOR_MODE = "administratormode";
	private static final int MAX_NUMBER_OF_UNCAUGHT_EXCEPTIONS = 3;
	private int numberOfUncaughtExceptions = 0;

	public void onModuleLoad() {
		// JsonReader初期化
		new PacketChatMessage.Json();
		new PacketChatMessages.Json();
		new PacketGameStatus.Json();
		new PacketGameStatus.GamePlayerStatus.Json();
		new PacketMatchingData.Json();
		new PacketMatchingPlayer.Json();
		new PacketPlayerSummary.Json();
		new PacketServerStatus.Json();

		// 各種初期化
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				// NS_ERROR_NOT_INITIALIZED というエラーログが大量に吐かれるため
				if (++numberOfUncaughtExceptions <= MAX_NUMBER_OF_UNCAUGHT_EXCEPTIONS) {
					logger.log(Level.WARNING, "不明なエラーが発生しました", e);
				}
			}
		});

		// Visualization apiの読み込みはhtmlで行う

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				onModuleLoad2();
			}
		});
	}

	public void onModuleLoad2() {
		RootPanel.get("slot").add(Controller.getInstance());
		RootPanel.get("position_selecter").add(WidgetPositionSelecter.getIstance());
		RootPanel.get("title").setVisible(false);

		final String token = History.getToken();
		if (token.equals(ADMINISTRATOR_MODE) || !GWT.isProdMode()) {
			SharedData.get().setAdministoratorMode(true);
		}
	}
}
