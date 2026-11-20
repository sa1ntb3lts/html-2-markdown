import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownWriter {

    public Path resolverDestino(Path destinoOpcional, URL url) {
        if (destinoOpcional != null) {
            return destinoOpcional;
        }
        String ruta = (url != null) ? url.getPath() : "";
        String nombreBase = "salida";
        if (ruta != null && !ruta.isBlank()) {
            int ultimoSlash = ruta.lastIndexOf('/');
            String segmento = (ultimoSlash >= 0) ? ruta.substring(ultimoSlash + 1) : ruta;
            if (!segmento.isBlank()) {
                int punto = segmento.lastIndexOf('.');
                nombreBase = (punto > 0) ? segmento.substring(0, punto) : segmento;
            }
        }
        return Path.of(nombreBase + ".md");
    }

    public void guardar(Path destino, String contenido) throws IOException {
        if (destino == null) {
            throw new IOException("La ruta de destino no puede ser nula.");
        }
        Path padre = destino.getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
        String texto = (contenido != null) ? contenido : "";
        Files.writeString(destino, texto, StandardCharsets.UTF_8);
    }
} 
