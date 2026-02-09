package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.common.UpgradeRequestAdapter;

public class FakeUpgradeRequest extends UpgradeRequestAdapter {
  public FakeUpgradeRequest() {
    super(
        "ws://kishibe.dyndns.tv/QMAClone/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData");
  }

  @Override
  public void setParameterMap(Map<String, List<String>> parameters) {
    super.setParameterMap(parameters);
  }
}
