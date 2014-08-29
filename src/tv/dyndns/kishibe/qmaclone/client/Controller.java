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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Controller extends SimplePanel {
	private static final Logger logger = Logger.getLogger(Controller.class.getName());
	private static final Controller INSTANCE = new Controller();
	private static final String HISTORY_TOKEN_PREFIX_PROBLEM = "problem";

	public static Controller getInstance() {
		return INSTANCE;
	}

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final VerticalPanel panelErrorMessage = new VerticalPanel();
	private final DecoratedTabPanel tabPanel = new DecoratedTabPanel(); // 中のコンテンツのサイズによって自動的にリサイズされるのでTabPanelを使用する
	private final SimplePanel panelGame = new SimplePanel();
	private final DecoratorPanel panelChat = new DecoratorPanel();
	private final LoginReporter loginReporter = new LoginReporter();
	private final QMACloneGinjector qmaCloneGinjector = GWT.create(QMACloneGinjector.class);
	private SceneBase scene = null;

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
				log(record.getMessage());
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
		setWidth("100%");
		setHeight("99%");
		add(rootPanel);
		rootPanel.setWidth("100%");

		rootPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		rootPanel.add(new HTML("<h1>QMAClone by nodchip</h1>"));
		rootPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
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

			tabPanel.setAnimationEnabled(true);

			// 遅延ローディング
			tabPanel.addSelectionHandler(selectionHandlerTab);

			// ゲームパネル
			tabPanel.add(panelGame, "ゲーム");

			// 問題作成パネル
			tabPanel.add(creationUi, "問題作成");

			// 問題統計パネル
			tabPanel.add(panelStatistics, "統計");

			// 正解率表示パネル
			tabPanel.add(panelRatioReport, "正解率統計");

			// 問題検索パネル
			tabPanel.add(panelSearchProblem, "検索");

			// 各種設定パネル
			tabPanel.add(panelSetting, "設定");

			// プレイヤー一覧
			tabPanel.add(panelLoginPlayers, "ﾌﾟﾚｲﾔｰ一覧");

			// ランキング
			tabPanel.add(panelRanking, "ﾗﾝｷﾝｸﾞ");

			// 掲示板
			tabPanel.add(panelBbs, "掲示板");

			// リンク
			tabPanel.add(panelLink, "リンク");

			tabPanel.selectTab(0);

			rootPanel.add(tabPanel);
			rootPanel.add(panelErrorMessage);
			// rootPanel.add(new PanelAdvertisement());
			rootPanel.add(panelChat);

			setScene(new SceneLobby());

			// チャットの表示
			setChatEnabled(UserData.get().isChatEnabled());

			String token = History.getToken();
			if (token.startsWith(HISTORY_TOKEN_PREFIX_PROBLEM)) {
				Controller.getInstance().showCreationProblem(Integer.valueOf(token.split("=")[1]));
			}
		}
	};
	private final AsyncCallback<PacketLogin> callbackLogin = new AsyncCallback<PacketLogin>() {
		@Override
		public void onSuccess(PacketLogin result) {
		}

		@Override
		public void onFailure(Throwable caught) {
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
		tabPanel.selectTab(tabPanel.getWidgetIndex(creationUi));
		scrollToTop();
	}

	/**
	 * エラーメッセージ表示を表示する
	 * 
	 * @param message
	 *            　エラーメッセージ
	 */
	private void log(String message) {
		HTML html = new HTML(SafeHtmlUtils.fromString(message));
		html.addStyleDependentName("errorMessage");
		panelErrorMessage.add(html);
	}

	public void setChatEnabled(boolean chatEnabled) {
		if (chatEnabled) {
			panelChat.setWidget(new PanelChat());
		} else {
			panelChat.clear();
		}
	}

	private SelectionHandler<Integer> selectionHandlerTab = new SelectionHandler<Integer>() {
		@Override
		public void onSelection(SelectionEvent<Integer> event) {
			tabPanel.getWidget(event.getSelectedItem()).setVisible(true);
		}
	};
}
