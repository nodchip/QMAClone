package tv.dyndns.kishibe.qmaclone.client.game;

public enum LetterType {
	Hiragana("ひらがな"), Katakana("カタカナ"), Alphabet("英数字");
	private final String humanReadableName;

	private LetterType(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	@Override
	public String toString() {
		return humanReadableName;
	}
}
