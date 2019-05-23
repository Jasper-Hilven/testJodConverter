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


public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    private static final String DEFAULT_JODCONVERTER_ENDPOINT = "http://localhost:5050/converter";
    private static final Log logger = LogFactory.getLog(App.class);
    private static final List<String> TARGET_MIMETYPES = Arrays
            .asList(new String[] { MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_PDF });
    private static String endpoint = DEFAULT_JODCONVERTER_ENDPOINT;

    public static void main(String[] args) throws Exception {
       ContentReader reader = new FileContentReader(new File("/home/jasper/Downloads/test.vsd"));
        File file = new File("/home/jasper/Downloads/outputTest.vsd");
        transformInternal(reader,new FileContentWriter(file),new RuntimeExecutableContentTransformerOptions(),file);
    }

    static void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options,File inputFile)
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

            URL obj = new URL(endpoint);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();


            Multipart part = new Multipart(DEFAULT_JODCONVERTER_ENDPOINT,"UTF_8");
            part.addFilePart("inputFile",inputFile);
            part.addHeaderField("outputFormat","pdf");
            List<String> result = part.finish();

            // add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", sourceMimeType);
            con.setRequestProperty("Accept", targetMimeType);
            //curl -XPOST -F inputFile=@Costanza.docx -F outputFormat='pdf' http://localhost:8080/converter --output aaa
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());

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
            if (logger.isDebugEnabled()) {
            }

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
    }
}
