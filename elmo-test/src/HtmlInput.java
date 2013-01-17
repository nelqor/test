import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

public class HtmlInput {

	@SuppressWarnings("unused")
	private static InputStream getUrlStream(final String fileURL)
			throws IOException {
		final HttpUriRequest get = new HttpGet(fileURL);
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpParams params = client.getParams();
		params.setParameter(
				org.apache.http.params.CoreConnectionPNames.SO_TIMEOUT, 2000);
		client.setParams(params);
		HttpResponse response;
		try {
			response = client.execute(get);
		} catch (final ConnectException e) {
			// Add some context to the exception and rethrow
			throw new IOException("ConnectionException trying to GET "
					+ fileURL, e);
		}
	
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new FileNotFoundException("Server returned "
					+ response.getStatusLine());
		}
	
		// Get the input stream
		final BufferedInputStream bis = new BufferedInputStream(response
				.getEntity().getContent());
	
		// Read the file and stream it out
		return bis;
	}

}