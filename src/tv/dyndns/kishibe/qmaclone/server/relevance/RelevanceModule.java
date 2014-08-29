package tv.dyndns.kishibe.qmaclone.server.relevance;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

public class RelevanceModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<Dictionary> binder = Multibinder.newSetBinder(binder(), Dictionary.class);
		binder.addBinding().to(WikipediaAllTitlesDictionary.class);
		binder.addBinding().to(NicoVideoDicImeDictionary.class);

		install(new FactoryModuleBuilder().build(ViterbiAnalyzer.Factory.class));
		install(new FactoryModuleBuilder().build(ViterbiTokenizer.Factory.class));
		install(new FactoryModuleBuilder().build(Dictionaries.Factory.class));
		install(new FactoryModuleBuilder().build(Trie.Factory.class));
		bind(TrieCache.class).in(Scopes.SINGLETON);
	}

}
