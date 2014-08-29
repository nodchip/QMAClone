package tv.dyndns.kishibe.qmaclone.server.util;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbutils.QueryRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.server.QMACloneModule;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.database.ProblemProcessable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class CreationLogUpdater {

  private static final Logger logger = Logger.getLogger(CreationLogUpdater.class.toString());

  private final Database database;
  private final QueryRunner runner;

  @Inject
  public CreationLogUpdater(Database database, QueryRunner queryRunner) {
    this.database = Preconditions.checkNotNull(database);
    this.runner = Preconditions.checkNotNull(queryRunner);
  }

  private void run() {
    final PacketUserData userData = new PacketUserData();
    userData.userCode = 123456789;
    userData.playerName = "QMAClone";

    try {
      database.setUserData(userData);
      final List<Object[]> params = Lists.newArrayList();

      System.out.println("Parsing problems");
      database.processProblems(new ProblemProcessable() {
        @Override
        public void process(PacketProblem problem) throws Exception {
          params.add(new Object[] { problem.id, userData.userCode,
              new Timestamp(System.currentTimeMillis()), "kishibe.dyndns.tv",
              problem.toChangeSummary() });
        }
      });

      System.out.println("Inserting creation logs");
      runner
          .batch(
              "INSERT INTO creation_log (PROBLEM_ID, USER_CODE, DATE, MACHINE_IP, SUMMARY) VALUES (?, ?, ?, ?, ?)",
              params.toArray(new Object[0][]));

    } catch (DatabaseException | SQLException e) {
      logger.log(Level.WARNING, "問題更新ログの更新に失敗しました", e);
    }
  }

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new QMACloneModule());
    CreationLogUpdater updater = injector.getInstance(CreationLogUpdater.class);
    updater.run();
  }

}
