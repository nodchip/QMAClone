package tv.dyndns.kishibe.qmaclone.client.bbs;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PanelThreadList extends VerticalPanel {
	private static final Logger logger = Logger.getLogger(PanelThreadList.class.getName());

	public PanelThreadList(int bbsId) {
		Service.Util.getInstance().getBbsThreads(bbsId, 0, Integer.MAX_VALUE, callbackGetBbsThread);
	}

	private final AsyncCallback<List<PacketBbsThread>> callbackGetBbsThread = new AsyncCallback<List<PacketBbsThread>>() {
		public void onSuccess(List<PacketBbsThread> result) {
			for (final PacketBbsThread thread : result) {
				final LazyPanel lazyPanel = new LazyPanel() {
					@Override
					protected Widget createWidget() {
						DecoratorPanel decoratorPanel = new DecoratorPanel();
						decoratorPanel.setWidget(new PanelThread((int) thread.id, thread.title));
						return decoratorPanel;
					}
				};

				OpenHandler<DisclosurePanel> openHandler = new OpenHandler<DisclosurePanel>() {
					@Override
					public void onOpen(OpenEvent<DisclosurePanel> event) {
						lazyPanel.ensureWidget();
					}
				};

				DisclosurePanel disclosurePanel = new DisclosurePanel(thread.title);
				disclosurePanel.setContent(lazyPanel);
				disclosurePanel.addOpenHandler(openHandler);
				add(disclosurePanel);
			}
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "BBSスレッドの取得に失敗しました", caught);
		}
	};
}
