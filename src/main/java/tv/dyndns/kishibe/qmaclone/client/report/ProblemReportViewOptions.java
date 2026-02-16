package tv.dyndns.kishibe.qmaclone.client.report;

/**
 * 問題レポートUIの表示列オプション。
 */
public final class ProblemReportViewOptions {
	public final boolean showProblemId;
	public final boolean showSimilarity;
	public final boolean showGenre;
	public final boolean showType;
	public final boolean showSentence;
	public final boolean showCreator;
	public final boolean showAccuracyRate;
	public final boolean showAnswerCount;
	public final boolean showIndication;
	public final boolean showRegister;
	public final boolean showOperation;
	public final boolean useRatioDefaultSort;

	public ProblemReportViewOptions(boolean showProblemId, boolean showSimilarity,
			boolean showGenre, boolean showType, boolean showSentence, boolean showCreator,
			boolean showAccuracyRate, boolean showAnswerCount, boolean showIndication,
			boolean showRegister, boolean showOperation, boolean useRatioDefaultSort) {
		this.showProblemId = showProblemId;
		this.showSimilarity = showSimilarity;
		this.showGenre = showGenre;
		this.showType = showType;
		this.showSentence = showSentence;
		this.showCreator = showCreator;
		this.showAccuracyRate = showAccuracyRate;
		this.showAnswerCount = showAnswerCount;
		this.showIndication = showIndication;
		this.showRegister = showRegister;
		this.showOperation = showOperation;
		this.useRatioDefaultSort = useRatioDefaultSort;
	}

	/**
	 * 検索画面・類似問題画面で使う既定の表示設定。
	 *
	 * @return 既定設定
	 */
	public static ProblemReportViewOptions defaults() {
		return new ProblemReportViewOptions(true, true, true, true, true, true, true, false, true,
				true, true, false);
	}

	/**
	 * 登録問題一覧（旧: 正解率統計）向けの表示設定。
	 *
	 * @return 登録問題一覧向け設定
	 */
	public static ProblemReportViewOptions forRatioReport() {
		return new ProblemReportViewOptions(true, false, true, true, true, false, true, true, true,
				false, true, true);
	}
}
