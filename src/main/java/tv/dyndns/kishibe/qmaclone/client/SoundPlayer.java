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

import tv.dyndns.kishibe.qmaclone.client.sound.AudioEngine;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundAsset;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundCatalog;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundEvent;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundManager;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundSettings;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundSettingsStore;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

public class SoundPlayer {
	private final AudioEngine audioEngine;

	private static class Holder {
		private static final SoundPlayer INSTANCE = new SoundPlayer();
	}

	public static SoundPlayer getInstance() {
		return Holder.INSTANCE;
	}

	private SoundPlayer() {
		SoundManager.getInstance().setEmbedType(SoundManager.EMBED_TYPE_WAV);
		audioEngine = new AudioEngine(SoundManager.getInstance());
	}

	public void play(final String url) {
		if (UserData.get().isPlaySound()) {
			SoundSettings settings = SoundSettingsStore.loadFromLocalStorage();
			if (settings.isMuted()) {
				return;
			}
			SoundEvent event = toEvent(url);
			if (event != null) {
				play(event);
				return;
			}
			audioEngine.playUrl(url, settings.getMasterVolume(), settings.getUiVolume(), 1.0);
		}
	}

	public void play(SoundEvent event) {
		if (!UserData.get().isPlaySound()) {
			return;
		}
		SoundAsset asset = SoundCatalog.getAsset(event);
		if (asset == null) {
			return;
		}
		SoundSettings settings = SoundSettingsStore.loadFromLocalStorage();
		if (settings.isMuted()) {
			return;
		}
		audioEngine.playUrl(asset.getUrl(), settings.getMasterVolume(),
				settings.getCategoryVolume(asset.getCategory()), asset.getBaseGain());
	}

	public static SoundEvent toEvent(String url) {
		if (Constant.SOUND_URL_BUTTON_PUSH.equals(url)) {
			return SoundEvent.BUTTON_PUSH;
		}
		if (Constant.SOUND_URL_BUTTON_OK.equals(url)) {
			return SoundEvent.BUTTON_OK;
		}
		if (Constant.SOUND_URL_TIME_UP.equals(url)) {
			return SoundEvent.TIME_UP;
		}
		if (Constant.SOUND_URL_GOOD.equals(url)) {
			return SoundEvent.CORRECT;
		}
		if (Constant.SOUND_URL_BAD.equals(url)) {
			return SoundEvent.INCORRECT;
		}
		if (Constant.SOUND_URL_READY_FOR_GAME.equals(url)) {
			return SoundEvent.READY_FOR_GAME;
		}
		return null;
	}
}
