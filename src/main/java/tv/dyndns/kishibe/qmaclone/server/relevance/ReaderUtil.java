package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;

public class ReaderUtil {

	public static Reader wrapWithNormalizer(Reader reader) {
		String s = null;
		try {
			s = CharStreams.toString(reader);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}

		return new StringReader(Normalizer.normalize(s));
	}

}
