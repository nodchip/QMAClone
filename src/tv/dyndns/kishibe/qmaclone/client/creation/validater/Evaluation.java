package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.List;

import com.google.common.collect.Lists;

public class Evaluation {
	public List<String> warn = Lists.newArrayList();
	public List<String> info = Lists.newArrayList();

	public boolean hasWarning() {
		return !warn.isEmpty();
	}
}
