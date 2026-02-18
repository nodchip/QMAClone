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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.bbs.PanelBbs;
import tv.dyndns.kishibe.qmaclone.client.chat.PanelChat;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.creation.CreationUi;
import tv.dyndns.kishibe.qmaclone.client.creation.WrongAnswerPresenter;
import tv.dyndns.kishibe.qmaclone.client.creation.WrongAnswerViewImpl;
import tv.dyndns.kishibe.qmaclone.client.link.PanelLink;
import tv.dyndns.kishibe.qmaclone.client.lobby.SceneLobby;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLogin;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSetting;
import tv.dyndns.kishibe.qmaclone.client.statistics.PanelStatistics;
import tv.dyndns.kishibe.qmaclone.client.util.DetailRemoteLogger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.storage.client.Storage;

public class Controller extends SimplePanel {
	private static final Logger logger = Logger.getLogger(Controller.class.getName());
	private static final String MAIN_CONTENT_WIDTH = "800px";
	private static final Controller INSTANCE = new Controller();
	private static final String HISTORY_TOKEN_PREFIX_PROBLEM = "problem";
	private static final String LOCAL_STORAGE_KEY_CHAT_COLLAPSED = "qmaclone.chat.collapsed";
	private static final int MAX_ERROR_LOG_COUNT = 3;
	private static final int ERROR_LOG_AUTO_DISMISS_MS = 5000;

	public static Controller getInstance() {
		return INSTANCE;
	}

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel panelMain = new HorizontalPanel();
	private final VerticalPanel panelMainSidebar = new VerticalPanel();
	private final VerticalPanel panelMainContent = new VerticalPanel();
	private final VerticalPanel panelErrorMessage = new VerticalPanel();
	private final DeckPanel panelContentDeck = new DeckPanel();
	private final SimplePanel panelGame = new SimplePanel();
	private final SimplePanel panelChat = new SimplePanel();
	private final FlowPanel panelChatContainer = new FlowPanel();
	private final FlowPanel panelChatHeader = new FlowPanel();
	private final Label labelChatHeader = new Label("チャット");
	private final Button buttonToggleChat = new Button();
	private final SimplePanel panelChatBody = new SimplePanel();
	private final Button buttonShowChatPanel = new Button("チャットを開く");
	private final Storage localStorage = Storage.getLocalStorageIfSupported();
	private final LoginReporter loginReporter = new LoginReporter();
	private final QMACloneGinjector qmaCloneGinjector = GWT.create(QMACloneGinjector.class);
	private boolean chatEnabled = false;
	private boolean chatCollapsed = false;
	private SceneBase scene = null;
	private final List<Button> mainNavButtons = new ArrayList<Button>();
	private final List<Widget> mainNavWidgets = new ArrayList<Widget>();

	private final LazyPanel creationUi = new LazyPanel() {
		public CreationUi createWidget() {
			return new CreationUi(new WrongAnswerPresenter(new WrongAnswerViewImpl()));
		}
	};
	private final LazyPanel panelStatistics = new LazyPanel() {
		public PanelStatistics createWidget() {
			return new PanelStatistics();
		}
	};
	private final LazyPanel panelRatioReport = new LazyPanel() {
		public PanelRatioReport createWidget() {
			return new PanelRatioReport();
		}
	};
	private final LazyPanel panelSearchProblem = new LazyPanel() {
		protected Widget createWidget() {
			return new PanelSearchProblem();
		};
	};
	private final LazyPanel panelSetting = new LazyPanel() {
		public PanelSetting createWidget() {
			return qmaCloneGinjector.getSettingView();
		}
	};
	private final LazyPanel panelLoginPlayers = new LazyPanel() {
		public PanelLoginPlayers createWidget() {
			return new PanelLoginPlayers();
		}
	};
	private final LazyPanel panelRanking = new LazyPanel() {
		public Widget createWidget() {
			return qmaCloneGinjector.getRankingView().asWidget();
		}
	};
	private final LazyPanel panelBbs = new LazyPanel() {
		public PanelBbs createWidget() {
			return new PanelBbs(Constant.GENERIC_BBS_ID);
		}
	};
	private final LazyPanel panelLink = new LazyPanel() {
		public PanelLink createWidget() {
			return new PanelLink();
		}
	};

	private Controller() {
		Logger.getLogger("").addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
					log(record.getMessage(), record.getLevel());
					ClientReloadPrompter.maybePrompt(record.getThrown());
				}
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		});
		Logger.getLogger("").addHandler(new DetailRemoteLogger());

		// setAlwaysShowScrollBars(false);
		addStyleName("app-shell");
		setWidth("100%");
		setHeight("99%");
		add(rootPanel);
		rootPanel.setWidth("100%");
		rootPanel.addStyleName("app-root-panel");
		panelMain.setWidth("100%");
		panelMain.setSpacing(0);
		panelMain.addStyleName("app-main-panel");
		panelMainSidebar.addStyleName("app-main-sidebar");
		panelMainSidebar.setWidth("190px");
		panelMainSidebar.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		panelMainContent.addStyleName("app-main-content");
		panelMainContent.setWidth("100%");
		panelMainContent.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		panelContentDeck.addStyleName("app-main-content-deck");
		panelContentDeck.setWidth("100%");
		panelGame.setWidth(MAIN_CONTENT_WIDTH);
		creationUi.setWidth(MAIN_CONTENT_WIDTH);
		panelStatistics.setWidth(MAIN_CONTENT_WIDTH);
		panelRatioReport.setWidth(MAIN_CONTENT_WIDTH);
		panelSearchProblem.setWidth(MAIN_CONTENT_WIDTH);
		panelSetting.setWidth(MAIN_CONTENT_WIDTH);
		panelLoginPlayers.setWidth(MAIN_CONTENT_WIDTH);
		panelRanking.setWidth(MAIN_CONTENT_WIDTH);
		panelBbs.setWidth(MAIN_CONTENT_WIDTH);
		panelLink.setWidth(MAIN_CONTENT_WIDTH);
		// チャットのレイアウト責務: 外枠(panel) -> ヘッダー(container/header/title/toggle) -> 本文(body/content)
		panelChat.addStyleName("app-chat-panel");
		panelChatContainer.addStyleName("app-chat-container");
		panelChatHeader.addStyleName("app-chat-header");
		labelChatHeader.addStyleName("app-chat-title");
		buttonToggleChat.addStyleName("app-chat-toggle");
		panelChatBody.addStyleName("app-chat-body");
		panelGame.addStyleName("app-game-panel");
		panelErrorMessage.addStyleName("app-error-panel");
		buttonShowChatPanel.addStyleName("app-chat-reopen");
		buttonShowChatPanel.setVisible(false);
		buttonToggleChat.setText("-");
		buttonToggleChat.setTitle("チャットを閉じる");
		buttonToggleChat.addClickHandler(event -> toggleChatCollapsed());
		buttonShowChatPanel.addClickHandler(event -> {
			chatCollapsed = false;
			saveChatCollapsedPreference(chatCollapsed);
			applyChatCollapsed(chatCollapsed);
		});
		panelChatHeader.setWidth("100%");
		panelChatHeader.add(labelChatHeader);
		panelChatHeader.add(buttonToggleChat);
		panelChatContainer.setWidth("100%");
		panelChatContainer.setHeight("100%");
		panelChatBody.setHeight("100%");
		panelChatContainer.add(panelChatHeader);
		panelChatContainer.add(panelChatBody);
		panelChat.setWidget(panelChatContainer);

		rootPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		rootPanel.add(new HTML("<h1>QMAClone</h1>"));
		rootPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		rootPanel.add(buttonShowChatPanel);
		// rootPanel.add(new PanelAdvertisement());
		UserData.get().addLoadListener(loadListener);
	}

	private final UserDataLoadListener loadListener = new UserDataLoadListener() {
		public void onLoad() {
			// ログイン状態の通知
			Service.Util.getInstance().login(UserData.get().getUserCode(), callbackLogin);

			// サウンドマネージャ初期化
			SoundPlayer.getInstance().play(Constant.SOUND_URL_BUTTON_PUSH);

			// ログイン通知インスタンスの開始
			loginReporter.start();

			// ゲームパネル
			addMainSection(panelGame, "ゲーム");

			// 問題作成パネル
			addMainSection(creationUi, "問題作成");

			// 問題統計パネル
			addMainSection(panelStatistics, "統計");

			// 登録問題一覧パネル
			addMainSection(panelRatioReport, "登録問題一覧");

			// 問題検索パネル
			addMainSection(panelSearchProblem, "検索");

			// 各種設定パネル
			addMainSection(panelSetting, "設定");

			// プレイヤー一覧
			addMainSection(panelLoginPlayers, "プレイヤー一覧");

			// ランキング
			addMainSection(panelRanking, "ランキング");

			// 掲示板
			addMainSection(panelBbs, "掲示板");

			// リンク
			addMainSection(panelLink, "リンク");

			selectMainSection(0);

			panelMainContent.add(panelContentDeck);
			panelMainContent.setCellWidth(panelContentDeck, "100%");
			panelMainContent.setCellHorizontalAlignment(panelContentDeck, HorizontalPanel.ALIGN_CENTER);
			panelMain.add(panelMainSidebar);
			panelMain.add(panelMainContent);
			panelMain.add(panelChat);
			panelMain.setCellWidth(panelMainSidebar, "190px");
			panelMain.setCellHorizontalAlignment(panelMainSidebar, HorizontalPanel.ALIGN_LEFT);
			panelMain.setCellWidth(panelMainContent, "100%");
			panelMain.setCellHorizontalAlignment(panelMainContent, HorizontalPanel.ALIGN_CENTER);
			panelMain.setCellWidth(panelChat, "0px");
			panelMain.setCellHorizontalAlignment(panelChat, HorizontalPanel.ALIGN_RIGHT);
			rootPanel.add(panelMain);
			rootPanel.add(panelErrorMessage);
			chatCollapsed = loadChatCollapsedPreference();

			try {
				setScene(new SceneLobby());
			} catch (Throwable caught) {
				logger.log(Level.SEVERE, "ロビー画面の初期化に失敗しました", caught);
				log("ロビー画面の初期化に失敗しました。詳細はログを確認してください。");
			}

			// チャットの表示
			setChatEnabled(UserData.get().isChatEnabled());

			String token = History.getToken();
			if (token.startsWith(HISTORY_TOKEN_PREFIX_PROBLEM)) {
				Controller.getInstance().showCreationProblem(Integer.valueOf(token.split("=")[1]));
			}
		}
	};
	private final AsyncCallback<PacketLogin> callbackLogin = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<PacketLogin>() {
		@Override
		public void onSuccess(PacketLogin result) {
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "ログインの送信に失敗しました", caught);
		}
	};

	// シーン関連
	public void setScene(SceneBase scene) {
		if (this.scene != null) {
			this.scene.onUnload();
		}

		this.scene = scene;

		if (this.scene != null) {
			this.scene.onLoad();
		}
	}

	public SceneBase getScene() {
		return scene;
	}

	// 表示パネル
	public void setGamePanel(Widget widget) {
		// TODO(nodchip): エラー調査
		widget.removeFromParent();
		panelGame.clear();
		panelGame.setWidget(widget);
	}

	public void scrollToTop() {
		Window.scrollTo(0, 0);
	}

	public void scrollToChat() {
		Window.scrollTo(0, panelChat.getAbsoluteTop());
	}

	public void scrollToBottom() {
		Window.scrollTo(0, rootPanel.getOffsetHeight());
	}

	public void showCreationProblem(int problemID) {
		// getWidget()だけだと初めて表示するときにnullが入っているのでだめ
		creationUi.ensureWidget();
		if (creationUi.getWidget() instanceof CreationUi) {
			((CreationUi) creationUi.getWidget()).setProblem(problemID);
		}
		selectMainSection(mainNavWidgets.indexOf(creationUi));
		scrollToTop();
	}

	/**
	 * エラーメッセージ表示を表示する
	 * 
	 * @param message
	 *            　エラーメッセージ
	 */
	public void log(String message) {
		log(message, Level.WARNING);
	}

	public void log(String message, Level level) {
		if (message == null || message.trim().isEmpty()) {
			return;
		}

		Level safeLevel = level == null ? Level.WARNING : level;
		FlowPanel panelLogEntry = new FlowPanel();
		panelLogEntry.addStyleName("app-error-log");
		HorizontalPanel panelLogHeader = new HorizontalPanel();
		panelLogHeader.addStyleName("app-error-log-header");

		String titleText = "通知";
		if (safeLevel.intValue() >= Level.SEVERE.intValue()) {
			panelLogEntry.addStyleName("app-error-log-severe");
			titleText = "エラー";
		} else if (safeLevel.intValue() >= Level.WARNING.intValue()) {
			panelLogEntry.addStyleName("app-error-log-warning");
			titleText = "警告";
		} else {
			panelLogEntry.addStyleName("app-error-log-info");
		}

		Label labelTitle = new Label(titleText);
		labelTitle.addStyleName("app-error-log-title");
		Button buttonClose = new Button("×");
		buttonClose.setTitle("閉じる");
		buttonClose.addStyleName("app-error-log-close");
		buttonClose.addClickHandler(event -> panelLogEntry.removeFromParent());

		HTML htmlMessage = new HTML(SafeHtmlUtils.fromString(message));
		htmlMessage.addStyleName("app-error-log-message");

		panelLogHeader.setWidth("100%");
		panelLogHeader.add(labelTitle);
		panelLogHeader.add(buttonClose);
		panelLogHeader.setCellWidth(labelTitle, "100%");
		panelLogEntry.add(panelLogHeader);
		panelLogEntry.add(htmlMessage);
		panelErrorMessage.add(panelLogEntry);

		if (safeLevel.intValue() < Level.SEVERE.intValue()) {
			new Timer() {
				@Override
				public void run() {
					panelLogEntry.removeFromParent();
				}
			}.schedule(ERROR_LOG_AUTO_DISMISS_MS);
		}

		while (panelErrorMessage.getWidgetCount() > MAX_ERROR_LOG_COUNT) {
			panelErrorMessage.remove(0);
		}
	}

	public void setChatEnabled(boolean chatEnabled) {
		this.chatEnabled = chatEnabled;
		if (chatEnabled) {
			panelChatBody.setWidget(new PanelChat());
		} else {
			panelChatBody.clear();
		}
		applyChatCollapsed(chatCollapsed);
	}

	private boolean loadChatCollapsedPreference() {
		if (localStorage == null) {
			return false;
		}
		return "1".equals(localStorage.getItem(LOCAL_STORAGE_KEY_CHAT_COLLAPSED));
	}

	private void saveChatCollapsedPreference(boolean collapsed) {
		if (localStorage == null) {
			return;
		}
		localStorage.setItem(LOCAL_STORAGE_KEY_CHAT_COLLAPSED, collapsed ? "1" : "0");
	}

	private void toggleChatCollapsed() {
		chatCollapsed = !chatCollapsed;
		saveChatCollapsedPreference(chatCollapsed);
		applyChatCollapsed(chatCollapsed);
	}

	private void applyChatCollapsed(boolean collapsed) {
		if (!chatEnabled) {
			panelChat.setVisible(false);
			buttonShowChatPanel.setVisible(false);
			rootPanel.removeStyleName("chat-collapsed");
			buttonToggleChat.setText("+");
			buttonToggleChat.setTitle("チャットを開く");
			updateErrorPanelOffset();
			return;
		}

		panelChat.setVisible(true);
		if (collapsed) {
			rootPanel.addStyleName("chat-collapsed");
			buttonShowChatPanel.setVisible(true);
			buttonToggleChat.setText("+");
			buttonToggleChat.setTitle("チャットを開く");
		} else {
			rootPanel.removeStyleName("chat-collapsed");
			buttonShowChatPanel.setVisible(false);
			buttonToggleChat.setText("-");
			buttonToggleChat.setTitle("チャットを閉じる");
		}
		updateErrorPanelOffset();
	}

	/**
	 * チャット表示状態に合わせて通知スタックの表示位置を調整する。
	 */
	private void updateErrorPanelOffset() {
		if (chatEnabled && !chatCollapsed) {
			panelErrorMessage.addStyleName("app-error-panel--chat-open");
		} else {
			panelErrorMessage.removeStyleName("app-error-panel--chat-open");
		}
	}

	private void addMainSection(Widget widget, String label) {
		final int sectionIndex = panelContentDeck.getWidgetCount();
		panelContentDeck.add(widget);

		Button button = new Button(label);
		button.addStyleName("app-main-nav-button");
		button.setTitle(label);
		button.addClickHandler(event -> {
			selectMainSection(sectionIndex);
			scrollToTop();
		});
		panelMainSidebar.add(button);
		panelMainSidebar.setCellWidth(button, "100%");
		mainNavButtons.add(button);
		mainNavWidgets.add(widget);
	}

	private void selectMainSection(int selectedIndex) {
		if (selectedIndex < 0 || selectedIndex >= panelContentDeck.getWidgetCount()) {
			return;
		}
		panelContentDeck.showWidget(selectedIndex);
		updateMainNavigationSelection(selectedIndex);
	}

	private void updateMainNavigationSelection(int selectedIndex) {
		for (int i = 0; i < mainNavButtons.size(); ++i) {
			Button button = mainNavButtons.get(i);
			if (i == selectedIndex) {
				button.addStyleName("app-main-nav-button-active");
			} else {
				button.removeStyleName("app-main-nav-button-active");
			}
		}
	}
}

