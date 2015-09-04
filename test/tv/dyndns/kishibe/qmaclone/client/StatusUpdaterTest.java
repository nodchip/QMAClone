package tv.dyndns.kishibe.qmaclone.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class StatusUpdaterTest extends QMACloneGWTTestCaseBase {
  private StatusUpdater<Object> updater;
  private AsyncCallback<Object> callbackArgument;
  private Object statusArgument;

  // private String jsonArgument;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    callbackArgument = null;
    statusArgument = null;
    // jsonArgument = null;

    updater = new StatusUpdater<Object>("protocol", 10 * 1000) {
      @Override
      protected void request(AsyncCallback<Object> callback) {
        callbackArgument = callback;
      }

      @Override
      protected Object parse(String json) {
        // jsonArgument = json;
        return new Object();
      }

      @Override
      protected void onReceived(Object status) {
        statusArgument = status;
      }
    };
  }

  @Test
  public void testCommandUpdate() {
    updater.status = StatusUpdater.Status.RPC;
    assertTrue(updater.commandUpdate.execute());
    assertNotNull(callbackArgument);
  }

  @Test
  public void testCallback() {
    updater.callback.onSuccess(new Object());
    assertNotNull(statusArgument);
  }

  @Test
  public void testStart() throws Exception {
    updater.start();
  }

  @Test
  public void testStop() {
    updater.stop();
  }
}
