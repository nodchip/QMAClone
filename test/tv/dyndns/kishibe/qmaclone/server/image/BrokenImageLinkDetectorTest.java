package tv.dyndns.kishibe.qmaclone.server.image;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

@RunWith(MockitoJUnitRunner.class)
public class BrokenImageLinkDetectorTest {

  @Mock
  private ImageLinkChecker.Factory mockImageLinkCheckerFactory;
  @Mock
  private ImageLinkChecker mockImageLinkChecker;
  @Mock
  private Database mockDatabase;
  private BrokenImageLinkDetector brokenImageLinkDetector;

  @Before
  public void setUp() throws Exception {
    BrokenImageLinkDetector.SKIP_HOST_CHECK_FOR_TESTING = true;
    brokenImageLinkDetector = new BrokenImageLinkDetector(mockImageLinkCheckerFactory,
        mockDatabase);
  }

  @After
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
