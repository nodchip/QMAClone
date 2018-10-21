package tv.dyndns.kishibe.qmaclone.client.util;

import java.util.List;

public class CommandRunner implements Runnable {
	private List<Runnable> commands;
	private int index = 0;

	public CommandRunner(List<Runnable> commands) {
		this.commands = commands;
	}

	@Override
	public void run() {
		if (commands == null || index >= commands.size()) {
			commands = null;
			return;
		}

		commands.get(index++).run();
	}
}
