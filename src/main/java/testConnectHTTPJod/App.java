package testConnectHTTPJod;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.xml.crypto.dsig.TransformException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerOptions;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;


public class App {
	public String getGreeting() {
		return "Hello world.";
	}

	private static final String DEFAULT_JODCONVERTER_ENDPOINT = "http://localhost:8080/converter";
	private static final Log logger = LogFactory.getLog(App.class);
	private static final List<String> TARGET_MIMETYPES = Arrays
			.asList(new String[] { MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_PDF });
	private static String endpoint = DEFAULT_JODCONVERTER_ENDPOINT;

	public static void main(String[] args) throws Exception {
		ContentReader reader = new FileContentReader(new File("/home/rox/Costanza.docx"));
		File fileSource = new File("/home/rox/Costanza.docx");
		transformInternal(reader,new FileContentWriter(new File("/home/rox/Costanza.docx.pdf")),new RuntimeExecutableContentTransformerOptions(),fileSource);
	}

	static void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options, File inputFile)
			throws Exception {

		String targetMimeType = writer.getMimetype();
		String sourceMimeType = reader.getMimetype();

		InputStream is = null;

		long startTime = 0;

		is = reader.getContentInputStream();

		Multipart part = new Multipart(DEFAULT_JODCONVERTER_ENDPOINT, "UTF-8");
		part.addFilePart("inputFile", inputFile);
		part.addFormFieldWithoutEnding("outputFormat", "pdf");

		try {
			part.finish(writer);
		} catch (Exception e) {
// Something went extremely wrong on remote server
			logger.error("Remote transformation failed, remote host returned response code :" + part.getStatus());
			if (logger.isDebugEnabled()) {
				logger.debug("Source MimeType : " + sourceMimeType);
				logger.debug("Target MimeType : " + targetMimeType);
				logger.debug("Source size : " + reader.getSize());
				logger.debug("Source ContentURL : " + reader.getContentUrl());
				logger.debug("Remote JODConverter instance : " + endpoint);
			}
			throw new RuntimeException(e);

		}finally {
			if (logger.isDebugEnabled()) {
			}

			if (is != null) {
				try {
					is.close();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}

		}
	}

/*    static void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
			throws Exception {

		OutputStream os = writer.getContentOutputStream();
		String encoding = writer.getEncoding();
		String targetMimeType = writer.getMimetype();
		String sourceMimeType = reader.getMimetype();

		Writer ow = new OutputStreamWriter(os, encoding);

		InputStream is = null;

		long startTime = 0;
		try {
			is = reader.getContentInputStream();
			if (logger.isDebugEnabled()) {
				startTime = System.currentTimeMillis();
			}

			URL obj = new URL(endpoint);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// Set up limits -- TODO check if these values are taken into
			// consideration? I am getting the feeling timeouts are handled
			// on the dedicated thread for this transformation
			long readLimitTimeMs = options.getReadLimitTimeMs();
			if (readLimitTimeMs != -1)
				con.setConnectTimeout((int) readLimitTimeMs);
			long timeoutMs = options.getTimeoutMs();
			if (timeoutMs != -1)
				con.setReadTimeout((int) timeoutMs);

			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
			multipartEntity.addPart("inputFile", new InputStreamBody(is, "bla bla")); // how to get real file name??
			multipartEntity.addPart("outputFormat", new StringBody("pdf"));

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());

*//*			OutputStream out = con.getOutputStream();
			try {
				multipartEntity.writeTo(out);
			} finally {
				out.close();
			}*//*



			// FIXME add support for 2GB+ content... Really ?
			IOUtils.copy(is, wr);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				// Something went extremely wrong on remote server
				logger.error("Remote transformation failed, remote host returned response code :" + responseCode);
				if (logger.isDebugEnabled()) {
					logger.debug("Source MimeType : " + sourceMimeType);
					logger.debug("Target MimeType : " + targetMimeType);
					logger.debug("Source size : " + reader.getSize());
					logger.debug("Source ContentURL : " + reader.getContentUrl());
					logger.debug("Remote JODConverter instance : " + endpoint);
				}
				throw new TransformException(con.getResponseMessage());
			}

			IOUtils.copy(con.getInputStream(), os);
		} finally {
			*//*if (logger.isDebugEnabled()) {
				logger.debug(calculateMemoryAndTimeUsage(reader, startTime));
			}*//*

			if (is != null) {
				try {
					is.close();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
			if (ow != null) {
				try {
					ow.close();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		}
	}*/


}
