package tv.dyndns.kishibe.qmaclone.client.sound;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.storage.client.Storage;

/**
 * 効果音設定のシリアライズとlocalStorage保存を扱う。
 */
public final class SoundSettingsStore {
	private static final String LOCAL_STORAGE_KEY = "qmaclone.sound.settings";

	private SoundSettingsStore() {
	}

	/**
	 * 設定をJSONへ変換する。
	 */
	public static String toJson(SoundSettings settings) {
		return "{"
				+ "\"masterVolume\":" + settings.getMasterVolume() + ","
				+ "\"uiVolume\":" + settings.getUiVolume() + ","
				+ "\"gameplayVolume\":" + settings.getGameplayVolume() + ","
				+ "\"resultVolume\":" + settings.getResultVolume() + ","
				+ "\"muted\":" + settings.isMuted() + ","
				+ "\"schemaVersion\":" + settings.getSchemaVersion() + "}";
	}

	/**
	 * JSON文字列を設定へ変換する。
	 */
	public static SoundSettings fromJson(String json) {
		if (Strings.isNullOrEmpty(json)) {
			return SoundSettings.defaults();
		}
		SoundSettings defaults = SoundSettings.defaults();
		return new SoundSettings(
				readDouble(json, "masterVolume", defaults.getMasterVolume()),
				readDouble(json, "uiVolume", defaults.getUiVolume()),
				readDouble(json, "gameplayVolume", defaults.getGameplayVolume()),
				readDouble(json, "resultVolume", defaults.getResultVolume()),
				readBoolean(json, "muted", defaults.isMuted()),
				readInt(json, "schemaVersion", defaults.getSchemaVersion()));
	}

	/**
	 * localStorageから設定を読み込む。
	 */
	public static SoundSettings loadFromLocalStorage() {
		if (!GWT.isClient()) {
			return SoundSettings.defaults();
		}
		Storage localStorage = Storage.getLocalStorageIfSupported();
		if (localStorage == null) {
			return SoundSettings.defaults();
		}
		return fromJson(localStorage.getItem(LOCAL_STORAGE_KEY));
	}

	/**
	 * 設定をlocalStorageへ保存する。
	 */
	public static void saveToLocalStorage(SoundSettings settings) {
		if (!GWT.isClient()) {
			return;
		}
		Storage localStorage = Storage.getLocalStorageIfSupported();
		if (localStorage == null) {
			return;
		}
		localStorage.setItem(LOCAL_STORAGE_KEY, toJson(settings));
	}

	private static double readDouble(String json, String key, double defaultValue) {
		String value = readRawValue(json, key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}

	private static int readInt(String json, String key, int defaultValue) {
		String value = readRawValue(json, key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}

	private static boolean readBoolean(String json, String key, boolean defaultValue) {
		String value = readRawValue(json, key);
		if (value == null) {
			return defaultValue;
		}
		return "true".equalsIgnoreCase(value);
	}

	private static String readRawValue(String json, String key) {
		String keyToken = "\"" + key + "\":";
		int keyIndex = json.indexOf(keyToken);
		if (keyIndex < 0) {
			return null;
		}
		int valueStart = keyIndex + keyToken.length();
		int valueEnd = valueStart;
		while (valueEnd < json.length()) {
			char c = json.charAt(valueEnd);
			if (c == ',' || c == '}') {
				break;
			}
			valueEnd++;
		}
		return json.substring(valueStart, valueEnd).trim();
	}
}
