package tv.dyndns.kishibe.qmaclone.client.sound;

import java.util.Objects;

/**
 * 効果音設定を表す。
 */
public class SoundSettings {
	public static final int CURRENT_SCHEMA_VERSION = 1;

	private final double masterVolume;
	private final double uiVolume;
	private final double gameplayVolume;
	private final double resultVolume;
	private final boolean muted;
	private final int schemaVersion;

	/**
	 * 効果音設定を構築する。
	 */
	public SoundSettings(double masterVolume, double uiVolume, double gameplayVolume,
			double resultVolume, boolean muted, int schemaVersion) {
		this.masterVolume = clamp(masterVolume);
		this.uiVolume = clamp(uiVolume);
		this.gameplayVolume = clamp(gameplayVolume);
		this.resultVolume = clamp(resultVolume);
		this.muted = muted;
		this.schemaVersion = schemaVersion;
	}

	/**
	 * 既定の効果音設定を返す。
	 */
	public static SoundSettings defaults() {
		return new SoundSettings(1.0, 1.0, 1.0, 1.0, false, CURRENT_SCHEMA_VERSION);
	}

	/**
	 * マスター音量を返す。
	 */
	public double getMasterVolume() {
		return masterVolume;
	}

	/**
	 * UIカテゴリ音量を返す。
	 */
	public double getUiVolume() {
		return uiVolume;
	}

	/**
	 * ゲームカテゴリ音量を返す。
	 */
	public double getGameplayVolume() {
		return gameplayVolume;
	}

	/**
	 * 結果カテゴリ音量を返す。
	 */
	public double getResultVolume() {
		return resultVolume;
	}

	/**
	 * ミュート状態を返す。
	 */
	public boolean isMuted() {
		return muted;
	}

	/**
	 * 設定スキーマバージョンを返す。
	 */
	public int getSchemaVersion() {
		return schemaVersion;
	}

	/**
	 * カテゴリ別音量を返す。
	 */
	public double getCategoryVolume(SoundCategory category) {
		if (category == SoundCategory.UI) {
			return uiVolume;
		}
		if (category == SoundCategory.GAMEPLAY) {
			return gameplayVolume;
		}
		if (category == SoundCategory.RESULT) {
			return resultVolume;
		}
		return 1.0;
	}

	private static double clamp(double value) {
		if (value < 0) {
			return 0;
		}
		if (1 < value) {
			return 1;
		}
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SoundSettings)) {
			return false;
		}
		SoundSettings other = (SoundSettings) obj;
		return Double.compare(masterVolume, other.masterVolume) == 0
				&& Double.compare(uiVolume, other.uiVolume) == 0
				&& Double.compare(gameplayVolume, other.gameplayVolume) == 0
				&& Double.compare(resultVolume, other.resultVolume) == 0 && muted == other.muted
				&& schemaVersion == other.schemaVersion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(masterVolume, uiVolume, gameplayVolume, resultVolume, muted,
				schemaVersion);
	}
}
