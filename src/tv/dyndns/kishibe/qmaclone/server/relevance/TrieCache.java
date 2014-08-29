package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

public class TrieCache {

	private static final File DICTIONARY_FILE = new File("/var/www/qmaclone/trie.bin");
	private static final Object STATIC_KEY = new Object();
	private final Dictionaries.Factory dictionariesFactory;
	private final Trie.Factory trieFactory;
	private final LoadingCache<Object, Trie> cache = CacheBuilder.newBuilder().concurrencyLevel(1)
			.expireAfterWrite(1, TimeUnit.DAYS).maximumSize(1)
			.build(new CacheLoader<Object, Trie>() {
				@Override
				public Trie load(Object key) throws Exception {
					if (DICTIONARY_FILE.isFile()
							&& System.currentTimeMillis() < DICTIONARY_FILE.lastModified() + 7L
									* 24 * 60 * 60 * 1000) {
						Trie trie = trieFactory.create();
						trie.load(DICTIONARY_FILE);
						return trie;
					}

					Trie trie = trieFactory.create();
					trie.build(dictionariesFactory.create().getWords());
					trie.save(DICTIONARY_FILE);
					return trie;
				}
			});

	@Inject
	public TrieCache(Dictionaries.Factory dictionariesFactory, Trie.Factory trieFactory) {
		this.dictionariesFactory = Preconditions.checkNotNull(dictionariesFactory);
		this.trieFactory = Preconditions.checkNotNull(trieFactory);
	}

	public Trie get() {
		try {
			return cache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}

}
