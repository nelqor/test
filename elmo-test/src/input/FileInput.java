package input;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class FileInput {

	public static InputStream getFileStream(final String fileName)
			throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(fileName));
	}

}
