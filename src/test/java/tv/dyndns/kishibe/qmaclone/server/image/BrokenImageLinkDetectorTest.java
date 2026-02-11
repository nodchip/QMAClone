package tv.dyndns.kishibe.qmaclone.server.image;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BrokenImageLinkDetectorTest {

  @Mock
  private ImageLinkChecker.Factory mockImageLinkCheckerFactory;
  @Mock
  private ImageLinkChecker mockImageLinkChecker;
  @Mock
  private Database mockDatabase;
  private BrokenImageLinkDetector brokenImageLinkDetector;

  @BeforeEach
  public void setUp() throws Exception {
    BrokenImageLinkDetector.SKIP_HOST_CHECK_FOR_TESTING = true;
    brokenImageLinkDetector = new BrokenImageLinkDetector(mockImageLinkCheckerFactory,
        mockDatabase);
  }

  @AfterEach
  public void tearDown() {
    BrokenImageLinkDetector.SKIP_HOST_CHECK_FOR_TESTING = false;
  }

  @Test
  public void runProcessesProblems() throws Exception {
    when(mockImageLinkCheckerFactory.create()).thenReturn(mockImageLinkChecker);

    brokenImageLinkDetector.run();

    verify(mockDatabase).processProblems(mockImageLinkChecker);
  }

  @Test
  public void getBrokenImageLinksReturnsImageLinks() throws Exception {
    List<PacketImageLink> imageLinks = Lists.newArrayList(new PacketImageLink());

    when(mockImageLinkCheckerFactory.create()).thenReturn(mockImageLinkChecker);
    when(mockImageLinkChecker.getImageLinks()).thenReturn(imageLinks);

    brokenImageLinkDetector.run();

    assertThat(brokenImageLinkDetector.getBrokenImageLinks()).isEqualTo(imageLinks);
  }
}
