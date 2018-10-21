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
package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketLinkData implements IsSerializable {

	public int id;
	public String homePageName;
	public String authorName;
	public String url;
	public String bannerUrl;
	public String description;
	public int userCode;
	public long lastUpdate;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PacketLinkData)) {
			return false;
		}
		PacketLinkData rh = (PacketLinkData) obj;
		return id == rh.id && Objects.equal(homePageName, rh.homePageName)
				&& Objects.equal(authorName, rh.authorName) && Objects.equal(url, rh.url)
				&& Objects.equal(bannerUrl, rh.bannerUrl)
				&& Objects.equal(description, rh.description) && userCode == rh.userCode
				&& lastUpdate == rh.lastUpdate;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, homePageName, authorName, url, bannerUrl, description,
				userCode, lastUpdate);
	}

}
