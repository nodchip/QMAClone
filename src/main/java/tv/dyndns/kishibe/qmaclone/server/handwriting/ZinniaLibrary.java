package tv.dyndns.kishibe.qmaclone.server.handwriting;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface ZinniaLibrary extends Library {
	public Pointer zinnia_recognizer_new();

	public int zinnia_recognizer_open(Pointer recognizer, String filename);

	public String zinnia_recognizer_strerror(Pointer recognizer);

	public Pointer zinnia_character_new();

	public void zinnia_character_clear(Pointer stroke);

	public void zinnia_character_set_width(Pointer character, long width);

	public void zinnia_character_set_height(Pointer character, long height);

	public int zinnia_character_add(Pointer character, long id, int x, int y);

	public Pointer zinnia_recognizer_classify(Pointer recognizer, Pointer character, long nbest);

	public long zinnia_result_size(Pointer result);

	public String zinnia_result_value(Pointer result, long i);

	public float zinnia_result_score(Pointer result, long i);

	public void zinnia_result_destroy(Pointer result);

	public void zinnia_character_destroy(Pointer character);

	public void zinnia_recognizer_destroy(Pointer recognizer);

	public long zinnia_recognizer_size(Pointer recognizer);

	public String zinnia_recognizer_value(Pointer recognizer, long i);
}
