package tv.dyndns.kishibe.qmaclone.server.database;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public interface ProblemMinimumProcessable {
	void process(PacketProblemMinimum problem) throws Exception;
}
