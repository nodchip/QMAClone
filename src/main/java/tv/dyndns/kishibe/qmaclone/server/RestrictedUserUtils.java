package tv.dyndns.kishibe.qmaclone.server;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class RestrictedUserUtils {

  private static final String LOCALHOST = "127.0.0.1";
  private final Database database;

  @Inject
  public RestrictedUserUtils(Database database) {
    this.database = Preconditions.checkNotNull(database);
  }

  /**
   * 制限ユーザーかどうかを調べる。 制限ユーザーの場合はユーザーコードとリモートアドレスのペアが保存される。
   *
   * @param userCode
   *          ユーザーコード
   * @param remoteAddress
   *          　リモートアドレス
   * @param restrictionType
   *          　制限種別
   * @return 制限ユーザーなら{@code true}、そうでなければ{@code false}。
   * @throws DatabaseException
   *           エラー発生時
   */
  public boolean checkAndUpdateRestrictedUser(int userCode, String remoteAddress,
      RestrictionType restrictionType) throws DatabaseException {
    boolean restrictedUserCode = database.getRestrictedUserCodes(restrictionType)
        .contains(userCode);
    boolean restrictedRemoteAddress = !remoteAddress.equals(LOCALHOST)
        && database.getRestrictedRemoteAddresses(restrictionType).contains(remoteAddress);

    if (!restrictedUserCode && !restrictedRemoteAddress) {
      return false;
    }

    if (!restrictedUserCode) {
      database.addRestrictedUserCode(userCode, restrictionType);
    }
    if (!restrictedRemoteAddress && !remoteAddress.equals(LOCALHOST)) {
      database.addRestrictedRemoteAddress(remoteAddress, restrictionType);
    }

    return true;
  }

}
