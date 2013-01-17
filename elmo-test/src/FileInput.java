import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class FileInput {

	static InputStream getFileStream(final String fileName)
			throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(fileName));
	}

}
