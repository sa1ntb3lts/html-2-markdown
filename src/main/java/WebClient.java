import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class WebClient {

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 10_000;
    private static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    public String descargarHTML(URL url) throws IOException {
        if (url == null) {
            throw new IOException("URL nula.");
        }

        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        HttpURLConnection httpConn = (conn instanceof HttpURLConnection)
                ? (HttpURLConnection) conn
                : null;

        try {
            if (httpConn != null) {
                int status = httpConn.getResponseCode();
                if (status < 200 || status >= 300) {
                    throw new IOException("Código HTTP no exitoso: " + status);
                }
            }

            String contentType = conn.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
                throw new IOException("El contenido no es HTML: " + contentType);
            }

            Charset charset = obtenerCharset(contentType);

            try (InputStream in = conn.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                long total = 0;
                int leidos;

                while ((leidos = in.read(buffer)) != -1) {
                    total += leidos;
                    if (total > MAX_SIZE_BYTES) {
                        throw new IOException("El documento excede el límite de tamaño permitido.");
                    }
                    out.write(buffer, 0, leidos);
                }

                return out.toString(charset);
            }
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    private Charset obtenerCharset(String contentType) {
        for (String parte : contentType.split(";")) {
            String limpia = parte.trim().toLowerCase(Locale.ROOT);
            if (limpia.startsWith("charset=")) {
                String nombre = limpia.substring("charset=".length()).replace("\"", "").trim();
                if (!nombre.isEmpty() && Charset.isSupported(nombre)) {
                    return Charset.forName(nombre);
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
} 
