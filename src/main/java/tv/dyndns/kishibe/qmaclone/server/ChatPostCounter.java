package tv.dyndns.kishibe.qmaclone.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class ChatPostCounter implements Runnable {

  @VisibleForTesting
  static final int LIMIT_PER_MINUTE = 10;
  @VisibleForTesting
  static final String LOCAL_HOST = "127.0.0.1";
  private final Multiset<Integer> userCodes = ConcurrentHashMultiset.create();
  private final Multiset<String> remoteAddresses = ConcurrentHashMultiset.create();

  @Override
  public void run() {
    userCodes.clear();
    remoteAddresses.clear();
  }

  public boolean isAbleToPost(int userCode, String remoteAddress) {
    return userCodes.count(userCode) <= LIMIT_PER_MINUTE
        && (Strings.isNullOrEmpty(remoteAddress) || remoteAddress.equals(LOCAL_HOST) || remoteAddresses
            .count(remoteAddress) <= LIMIT_PER_MINUTE);
  }

  public void add(int userCode, String remoteAddress) {
    userCodes.add(userCode);
    if (!Strings.isNullOrEmpty(remoteAddress) && !remoteAddress.equals(LOCAL_HOST)) {
      remoteAddresses.add(remoteAddress);
    }
  }

}
