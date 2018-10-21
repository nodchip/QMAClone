package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class PatternMatchingAutomaton {

  private class PMA {
    public Map<Character, PMA> next = Maps.newHashMap();
    public List<String> accept = Lists.newArrayList();

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("next", next).add("accept", accept).toString();
    }
  }

  private static final Logger logger = Logger.getLogger(PatternMatchingAutomaton.class.getName());
  private final Set<Dictionary> dictionaries;
  private PMA pma;

  @Inject
  public PatternMatchingAutomaton(Set<Dictionary> dictionaries) {
    this.dictionaries = Preconditions.checkNotNull(dictionaries);
  }

  public List<String> segment(String sentence) {
    if (pma == null) {
      Set<String> words = Sets.newHashSet();
      for (Dictionary dictionary : dictionaries) {
        words.addAll(dictionary.getWords());
      }
      pma = build(words);
    }

    sentence = Normalizer.normalize(sentence).replaceAll("　", "").replaceAll(" ", "");

    List<String> words = Lists.newArrayList();
    PMA v = pma;
    for (char c : sentence.toCharArray()) {
      while (!v.next.containsKey(c))
        v = v.next.get('\0');
      v = v.next.get(c);
      words.addAll(v.accept);
    }

    return words;
  }

  private PMA build(Set<String> words) {
    int counter = 0;

    PMA root = new PMA();
    for (String word : words) {
      if (++counter % 10000 == 0) {
        logger.log(Level.INFO, "trie: " + counter);
      }

      // make trie
      PMA t = root;
      for (char c : word.toCharArray()) {
        if (!t.next.containsKey(c))
          t.next.put(c, new PMA());
        t = t.next.get(c);
      }
      t.accept.add(word);
    }

    counter = 0;

    Deque<PMA> Q = Queues.newArrayDeque();
    // make failure link using bfs
    for (char c = 'a'; c <= 'z'; ++c) {
      if (root.next.containsKey(c)) {
        root.next.get(c).next.put('\0', root);
        Q.push(root.next.get(c));
      } else {
        root.next.put(c, root);
      }
    }
    while (!Q.isEmpty()) {
      if (++counter % 10000 == 0) {
        logger.log(Level.INFO, "failure link: " + counter);
      }

      PMA t = Q.removeFirst();
      for (char c = 'a'; c <= 'z'; ++c) {
        if (t.next.containsKey(c)) {
          Q.addLast(t.next.get(c));
          PMA r = t.next.get('\0');
          while (!r.next.containsKey(c))
            r = r.next.get('\0');
          t.next.get(c).next.put('\0', r.next.get(c));
          t.next.get(c).accept.addAll(t.next.get(c).next.get('\0').accept);
        }
      }
    }
    return root;
  }

}
