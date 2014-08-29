package tv.dyndns.kishibe.qmaclone.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class PlusOne {
	public interface PlusOneTemplates extends SafeHtmlTemplates {
		@Template("<g:plusone href=\"{0}\"></g:plusone>")
		SafeHtml plusOne(SafeUri url);

		@Template("<g:plusone href=\"{0}\" annotation=\"inline\"></g:plusone>")
		SafeHtml plusOneInline(SafeUri url);
	}

	private static final PlusOneTemplates TEMPLATES = GWT.create(PlusOneTemplates.class);
	// http://simon.pamies.de/archives/194
	private static HeadElement head;

	public static void render() {
		ScriptElement scriptElement = Document.get().createScriptElement();
		scriptElement.setType("text/javascript");
		scriptElement.setAttribute("async", "true");
		scriptElement.setSrc("https://apis.google.com/js/plusone.js");
		getHead().appendChild(scriptElement);
	}

	private static HeadElement getHead() {
		if (head == null) {
			Element element = Document.get().getElementsByTagName("head").getItem(0);
			assert element != null : "HTML Head element required";
			HeadElement head = HeadElement.as(element);
			PlusOne.head = head;
		}
		return PlusOne.head;
	}

	public static SafeHtml getButton(int problemId, boolean inline) {
		SafeUri uri = UriUtils.fromTrustedString("http://kishibe.dyndns.tv:8080/QMAClone#problem="
				+ problemId);
		if (inline) {
			return TEMPLATES.plusOneInline(uri);
		} else {
			return TEMPLATES.plusOne(uri);
		}
	}
}
