package tv.dyndns.kishibe.qmaclone.client.bbs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PanelThreadTest {

	@Test
	public void toDisplayText_removesHtmlTagsAndKeepsLineBreaks() {
		String input = "<div>Hello<br>World</div><p>Line2</p><span>Tail</span>";
		assertEquals("Hello\nWorld\nLine2\nTail", PanelThread.toDisplayText(input));
	}

	@Test
	public void toDisplayText_handlesNull() {
		assertEquals("", PanelThread.toDisplayText(null));
	}
}
