import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class HTML2Markdown {

    public static void main(String[] args) {
        if (args.length < 1 || args[0].isBlank()) {
            System.err.println("Uso: java HTML2Markdown <url-http-o-https> [archivo-salida.md]");
            return;
        }

        String urlTexto = args[0].trim();
        Path destinoOpcional = (args.length >= 2 && !args[1].isBlank()) ? Path.of(args[1].trim()) : null;

        System.out.println("HTML2Markdown");
        System.out.println("URL: " + urlTexto);

        URL url;
        try {
            URI uri = new URI(urlTexto);
            if (!uri.isAbsolute() || uri.getScheme() == null) {
                System.err.println("Error: URL inválida.");
                return;
            }
            String esquema = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!esquema.equals("http") && !esquema.equals("https")) {
                System.err.println("Error: La URL debe usar protocolo HTTP o HTTPS.");
                return;
            }
            url = uri.toURL();
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            System.err.println("Error: URL inválida (" + e.getMessage() + ").");
            return;
        }

        WebClient webClient = new WebClient();
        HtmlParser htmlParser = new HtmlParser();
        MarkdownConverter converter = new MarkdownConverter();
        MarkdownWriter writer = new MarkdownWriter();

        String html;
        try {
            System.out.println("Conectando con el servidor...");
            html = webClient.descargarHTML(url);
            System.out.println("Respuesta HTTP: 200");
        } catch (IOException e) {
            System.err.println("Error de conexión o descarga: " + e.getMessage());
            return;
        }

        String markdown;
        try {
            System.out.println("Analizando HTML...");
            System.out.println("Convirtiendo a Markdown...");
            markdown = converter.convertir(html, url);
        } catch (IOException e) {
            System.err.println("Error al analizar o convertir el HTML: " + e.getMessage());
            return;
        }

        Path destino = writer.resolverDestino(destinoOpcional, url);
        try {
            writer.guardar(destino, markdown);
            long tamano = Files.size(destino);
            System.out.println("Archivo generado: " + destino);
            System.out.println("Tamaño: " + tamano + " bytes");
            System.out.println("Conversión finalizada.");
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo de salida: " + e.getMessage());
        }
    }
} 
