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
package tv.dyndns.kishibe.qmaclone.client.sound;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class SoundManager {
	private static final SoundManager INSTANCE = new SoundManager();

	public static SoundManager getInstance() {
		return INSTANCE;
	}

	public static final int EMBED_TYPE_WAV = 0;
	public static final int EMBED_TYPE_FLASH = 1;
	private final static int MAX_SOUND = 4;
	private final Element[] element = new Element[MAX_SOUND];
	private int index = 0;
	private int embedType = 0;

	private SoundManager() {
		for (int i = 0; i < MAX_SOUND; ++i) {
			element[i] = DOM.createDiv();
			DOM.setElementAttribute(element[i], "id", "sound_channel_" + i);
			DOM.appendChild(RootPanel.getBodyElement(), element[i]);
		}
	}

	public void play(String url) {
		play(url, false, 0);
	}

	public void play(String url, double volume) {
		play(url, false, 0, volume);
	}

	public void setEmbedType(int type) {
		embedType = type;
	}

	public void play(String url, boolean loop, int repeat) {
		play(url, loop, repeat, 1.0);
	}

	public void play(String url, boolean loop, int repeat, double volume) {
		String sanitizedUrl = SoundUrlSanitizer.sanitizeSoundUrl(url);
		if (sanitizedUrl == null) {
			return;
		}
		Element channel = element[index++ % MAX_SOUND];
		clearChannel(channel);
		Element audioElement = createAudioElement(sanitizedUrl, loop);
		DOM.appendChild(channel, audioElement);
		playAudio(audioElement, loop, repeat, clampVolume(volume));
	}

	public void clear() {
		for (int i = 0; i < MAX_SOUND; ++i) {
			clearChannel(element[i]);
		}
	}

	/**
	 * 再生チャンネルの子要素をクリアする。
	 */
	private void clearChannel(Element channel) {
		while (DOM.getFirstChild(channel) != null) {
			DOM.removeChild(channel, DOM.getFirstChild(channel));
		}
	}

	/**
	 * audio要素を生成する。
	 */
	private Element createAudioElement(String url, boolean loop) {
		Element audioElement = DOM.createElement("audio");
		DOM.setElementAttribute(audioElement, "src", url);
		DOM.setElementAttribute(audioElement, "preload", "auto");
		if (loop) {
			DOM.setElementAttribute(audioElement, "loop", "loop");
		}
		return audioElement;
	}

	/**
	 * 音声再生を開始し、必要に応じて繰り返す。
	 */
	private static native void playAudio(Element audioElement, boolean loop, int repeat, double volume) /*-{
		if (!audioElement) {
			return;
		}
		try {
			audioElement.volume = volume;
			var playCount = repeat > 0 ? repeat : 1;
			if (!loop && playCount > 1 && audioElement.addEventListener) {
				var played = 1;
				audioElement.addEventListener("ended", function() {
					played++;
					if (played <= playCount) {
						try {
							audioElement.currentTime = 0;
						} catch (e) {
						}
						if (audioElement.play) {
							audioElement.play();
						}
					}
				}, false);
			}
			if (audioElement.play) {
				audioElement.play();
			}
		} catch (e) {
		}
	}-*/;

	/**
	 * ボリューム値を0..1に正規化する。
	 */
	private double clampVolume(double volume) {
		if (volume < 0) {
			return 0;
		}
		if (1 < volume) {
			return 1;
		}
		return volume;
	}
}
