package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.api.UpgradeRequest;

public class FakeUpgradeRequest extends UpgradeRequest {
  public FakeUpgradeRequest() {
    super(
        "ws://kishibe.dyndns.tv/QMAClone/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData");
  }

  @Override
  public void setParameterMap(Map<String, List<String>> parameters) {
    super.setParameterMap(parameters);
  }
}