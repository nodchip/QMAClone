package tv.dyndns.kishibe.qmaclone.server.image;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class ImageLinkCheckerTest {

  private static final String IMAGE_URL_OK = "https://www.google.co.jp/images/nav_logo170_hr.png";
  private static final String IMAGE_URL_NOT_FOUND = "https://www.google.co.jp/logo.png";
  private static final String IMAGE_URL_MALFORMED = "this is a malformed url";
  private static final String IMAGE_URL_UNKNOWN_HOST = "http://www.okayamamokei.com/logo.png";
  private static final int PROBLEM_ID = -111111;

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private ImageLinkChecker imageLinkChecker;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void processWorksWithStatusCodeOk() {
    imageLinkChecker.process(createProblem(IMAGE_URL_OK));

    assertThat(imageLinkChecker.getImageLinks()).isEmpty();
  }

  @Test
  public void processWorksWithStatusCodeNotFound() {
    imageLinkChecker.process(createProblem(IMAGE_URL_NOT_FOUND));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_NOT_FOUND;
    expectedImageLink.statusCode = 404;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(Lists.newArrayList(expectedImageLink));
  }

  @Test
  public void processWorksWithMalformedUrl() {
    imageLinkChecker.process(createProblem(IMAGE_URL_MALFORMED));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_MALFORMED;
    expectedImageLink.statusCode = ImageLinkChecker.STATUS_CODE_MALFORMED_URL_EXCEPTION;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(Lists.newArrayList(expectedImageLink));
  }

  @Test
  public void processWorksWithUnknownHost() {
    imageLinkChecker.process(createProblem(IMAGE_URL_UNKNOWN_HOST));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_UNKNOWN_HOST;
    expectedImageLink.statusCode = ImageLinkChecker.STATUS_CODE_DOWNLOAD_FAILURE;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(Lists.newArrayList(expectedImageLink));
  }

  @Test
  public void processReturnsMultipleImageLinks() {
    imageLinkChecker.process(createProblem(IMAGE_URL_NOT_FOUND));
    imageLinkChecker.process(createProblem(IMAGE_URL_NOT_FOUND));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_NOT_FOUND;
    expectedImageLink.statusCode = 404;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(
        Lists.newArrayList(expectedImageLink, expectedImageLink));
  }

  private static PacketProblem createProblem(String url) {
    PacketProblem problem = new PacketProblem();
    problem.id = PROBLEM_ID;
    problem.type = ProblemType.Click;
    problem.choices = new String[] { url };
    return problem;
  }
}
