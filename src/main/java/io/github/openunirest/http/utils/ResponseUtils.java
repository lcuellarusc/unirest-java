package io.github.openunirest.http.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

public class ResponseUtils {

    private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

    /**
     * Parse out a charset from a content type header.
     *
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */
    public static String getCharsetFromContentType(String contentType) {
        if (contentType == null)
            return null;

        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            return m.group(1).trim().toUpperCase();
        }
        return null;
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    public static boolean isGzipped(Header contentEncoding) {
        if (contentEncoding != null) {
            String value = contentEncoding.getValue();
            if (value != null && "gzip".equals(value.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    public static String getCharsetfromResponse(HttpEntity responseEntity) {
        Header contentType = responseEntity.getContentType();
        if (contentType != null) {
            String responseCharset = getCharsetFromContentType(contentType.getValue());
            if (responseCharset != null && !responseCharset.trim().equals("")) {
                return responseCharset;
            }
        }
        return "UTF-8";
    }

    public static byte[] getRawBody(HttpEntity responseEntity) {
        try {
            InputStream responseInputStream = responseEntity.getContent();
            if (isGzipped(responseEntity.getContentEncoding())) {
                responseInputStream = new GZIPInputStream(responseEntity.getContent());
            }
            return getBytes(responseInputStream);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }
}
