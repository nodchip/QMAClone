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
	private static final String[] htmlTag = { "<embed width='0' height='0' src='__URL__' autostart='true' loop='__LOOP__' repeat='__REPEAT__' hidden='true'></embed>", "<object classid='clsid:d27cdb6e-ae6d-11cf-96b8-444553540000' codebase='http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0' width='0' height='0' id='SoundUnit'><param name='movie' value='__URL__' /><embed src='__URL__' width='0' height='0' name='SoundUnit' type='application/x-shockwave-flash' pluginspage='http://www.macromedia.com/go/getflashplayer' /></object>" };
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

	public void setEmbedType(int type) {
		embedType = type;
	}

	public void play(String url, boolean loop, int repeat) {
		String tag = htmlTag[embedType];
		tag = tag.replaceAll("__URL__", url);
		tag = tag.replaceAll("__LOOP__", loop ? "true" : "false");
		tag = tag.replaceAll("__REPEAT__", "" + repeat);
		final String html = tag;
		DOM.setInnerHTML(element[index++ % MAX_SOUND], html);
	}

	public void clear() {
		for (int i = 0; i < MAX_SOUND; ++i) {
			element[i] = DOM.createDiv();
			DOM.setInnerHTML(element[index++ % MAX_SOUND], "");
		}
	}
}
