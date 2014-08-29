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
package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class GeneralRankingPresenter {

	interface View extends IsWidget {
		void setRanking(List<List<PacketRankingData>> ranking);
	}

	private static final Logger logger = Logger.getLogger(GeneralRankingPresenter.class.getName());

	private View view;

	@Inject
	public GeneralRankingPresenter(ServiceAsync service) {
		service.getGeneralRanking(callbackGetRankingData);
	}

	public void setView(View view) {
		this.view = Preconditions.checkNotNull(view);
	}

	@VisibleForTesting
	final AsyncCallback<List<List<PacketRankingData>>> callbackGetRankingData = new AsyncCallback<List<List<PacketRankingData>>>() {
		public void onSuccess(List<List<PacketRankingData>> result) {
			view.setRanking(result);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "ランキングデータの取得に失敗しました", caught);
		}
	};

}
