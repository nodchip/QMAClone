package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class Dictionaries {

	public interface Factory {
		Dictionaries create();
	}

	private final Set<Dictionary> dictionaries;
	private Set<String> words;

	@Inject
	public Dictionaries(Set<Dictionary> dictionaries) {
		this.dictionaries = Preconditions.checkNotNull(dictionaries);
	}

	public Set<String> getWords() {
		if (words == null) {
			words = Sets.newHashSet();
			for (Dictionary dictionary : dictionaries) {
				words.addAll(dictionary.getWords());
			}
		}

		return words;
	}

}
