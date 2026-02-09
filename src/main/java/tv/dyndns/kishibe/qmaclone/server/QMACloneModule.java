package tv.dyndns.kishibe.qmaclone.server;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;

import com.google.gson.Gson;
import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sun.jna.Native;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseModule;
import tv.dyndns.kishibe.qmaclone.server.handwriting.Recognizable;
import tv.dyndns.kishibe.qmaclone.server.handwriting.RecognizerZinnia;
import tv.dyndns.kishibe.qmaclone.server.handwriting.ZinniaLibrary;
import tv.dyndns.kishibe.qmaclone.server.handwriting.ZinniaObjectFactory;
import tv.dyndns.kishibe.qmaclone.server.image.ImageLinkChecker;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils;
import tv.dyndns.kishibe.qmaclone.server.relevance.RelevanceModule;
import tv.dyndns.kishibe.qmaclone.server.service.LinkServletStub;
import tv.dyndns.kishibe.qmaclone.server.sns.SnsClient;
import tv.dyndns.kishibe.qmaclone.server.sns.SnsClients;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderModule;
import tv.dyndns.kishibe.qmaclone.server.websocket.MessageSender;

public class QMACloneModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ChatManager.class).in(SINGLETON);
    bind(NormalModeProblemManager.class).in(SINGLETON);
    bind(ThemeModeProblemManager.class).in(SINGLETON);
    bind(GameManager.class).in(SINGLETON);
    bind(ServerStatusManager.class).in(SINGLETON);
    bind(BadUserDetector.class).in(SINGLETON);
    bind(ProblemCorrectCounterResetCounter.class).in(SINGLETON);
    bind(ProblemIndicationCounter.class).in(SINGLETON);
    bind(RestrictedUserUtils.class).in(SINGLETON);
    bind(ChatPostCounter.class).in(SINGLETON);
    bind(PlayerHistoryManager.class).in(SINGLETON);
    bind(VoteManager.class).in(SINGLETON);
    bind(Recognizable.class).to(RecognizerZinnia.class).in(SINGLETON);
    bind(ThemeModeEditorManager.class).in(SINGLETON);
    bind(ImageUtils.class).in(SINGLETON);
    bind(PrefectureRanking.class).in(SINGLETON);
    bind(RatingDistribution.class).in(SINGLETON);
    install(new FactoryModuleBuilder().build(Game.Factory.class));
    install(new FactoryModuleBuilder().build(ComputerPlayer.Factory.class));
    install(new FactoryModuleBuilder().build(PlayerAnswer.Factory.class));
    bind(RemoteLoggingServiceImpl.class).in(SINGLETON);
    bind(ZinniaLibrary.class)
        .toInstance((ZinniaLibrary) Native.loadLibrary("zinnia", ZinniaLibrary.class));
    bind(ZinniaObjectFactory.class).in(SINGLETON);
    bind(SnsClient.class).annotatedWith(named("SnsClients")).to(SnsClients.class);
    bind(ThreadPool.class).in(SINGLETON);
    install(new FactoryModuleBuilder().build(ImageLinkChecker.Factory.class));

    bind(ServiceServletStub.class).in(SINGLETON);
    bind(LinkServletStub.class).in(SINGLETON);
    bind(StatsServletStub.class).in(SINGLETON);
    bind(IconUploadServletStub.class).in(SINGLETON);
    bind(RssServletStub.class).in(SINGLETON);
    bind(ImageProxyServletStub.class).in(SINGLETON);
    bind(StatsServletStub.class).in(SINGLETON);
    bind(RemoteLoggingServlet.class).in(SINGLETON);

    install(new DatabaseModule());
    install(new RelevanceModule());
    install(new DownloaderModule());
  }

  @Provides
  @com.google.inject.Singleton
  MessageSender<PacketChatMessages> provideChatMessagesMessageSender(ThreadPool threadPool) {
    return new MessageSender<PacketChatMessages>(threadPool) {
      @Override
      protected String encode(PacketChatMessages data) {
        return new Gson().toJson(data);
      }
    };
  }

  @Provides
  @com.google.inject.Singleton
  MessageSender<PacketMatchingStatus> provideMatchingStatusMessageSender(
      ThreadPool threadPool) {
    return new MessageSender<PacketMatchingStatus>(threadPool) {
      @Override
      protected String encode(PacketMatchingStatus data) {
        return new Gson().toJson(data);
      }
    };
  }

  @Provides
  @com.google.inject.Singleton
  MessageSender<PacketReadyForGame> provideReadyForGameMessageSender(
      ThreadPool threadPool) {
    return new MessageSender<PacketReadyForGame>(threadPool) {
      @Override
      protected String encode(PacketReadyForGame data) {
        return new Gson().toJson(data);
      }
    };
  }

  @Provides
  @com.google.inject.Singleton
  MessageSender<PacketGameStatus> provideGameStatusMessageSender(ThreadPool threadPool) {
    return new MessageSender<PacketGameStatus>(threadPool) {
      @Override
      protected String encode(PacketGameStatus data) {
        return new Gson().toJson(data);
      }
    };
  }

  @Provides
  @com.google.inject.Singleton
  MessageSender<PacketServerStatus> provideServerStatusMessageSender(
      ThreadPool threadPool) {
    return new MessageSender<PacketServerStatus>(threadPool) {
      @Override
      protected String encode(PacketServerStatus data) {
        return new Gson().toJson(data);
      }
    };
  }
}
