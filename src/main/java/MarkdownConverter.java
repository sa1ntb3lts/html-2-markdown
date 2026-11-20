import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class MarkdownConverter {

    private static class ContextoLista {
        final boolean ordenada;
        int contador = 0;

        ContextoLista(boolean ordenada) {
            this.ordenada = ordenada;
        }
    }

    public String convertir(String html, URL baseUrl) throws IOException {
        HtmlParser parser = new HtmlParser();
        StringBuilder salida = new StringBuilder();
        Deque<StringBuilder> pilaBuffers = new ArrayDeque<>();
        pilaBuffers.push(salida);

        Deque<ContextoLista> listas = new ArrayDeque<>();
        Deque<String> enlacesActivos = new ArrayDeque<>();
        Deque<String> etiquetasAbiertas = new ArrayDeque<>();

        int[] nivelEncabezado = {0};
        boolean enBloqueCodigo = false;
        boolean enCodigoLinea = false;
        boolean enCita = false;
        int omitirNivel = 0;

        parser.parsear(html, new HTMLEditorKit.ParserCallback() {

            @Override
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                String tag = t.toString().toLowerCase();
                etiquetasAbiertas.push(tag);

                if (omitirNivel > 0) {
                    if (tag.equals("script") || tag.equals("style")) {
                        omitirNivel++;
                    }
                    return;
                }

                if (tag.equals("script") || tag.equals("style")) {
                    omitirNivel = 1;
                    return;
                }

                StringBuilder actual = pilaBuffers.peek();

                if (tag.matches("h[1-6]")) {
                    nivelEncabezado[0] = Character.getNumericValue(tag.charAt(1));
                    prepararBloque(actual);
                    actual.append("#".repeat(nivelEncabezado[0])).append(" ");
                } else if (tag.equals("p")) {
                    prepararBloque(actual);
                } else if (tag.equals("blockquote")) {
                    prepararBloque(actual);
                    actual.append("> ");
                } else if (tag.equals("pre")) {
                    prepararBloque(actual);
                    actual.append("```\n");
                    enBloqueCodigo = true;
                } else if (tag.equals("code") && !enBloqueCodigo) {
                    actual.append("`");
                    enCodigoLinea = true;
                } else if (tag.equals("b") || tag.equals("strong")) {
                    actual.append("**");
                } else if (tag.equals("i") || tag.equals("em")) {
                    actual.append("*");
                } else if (tag.equals("a")) {
                    String href = obtenerAtributo(a, HTML.Attribute.HREF);
                    enlacesActivos.push(resolverUrl(baseUrl, href));
                    pilaBuffers.push(new StringBuilder());
                } else if (tag.equals("ul")) {
                    if (listas.isEmpty()) {
                        prepararBloque(actual);
                    } else {
                        actual.append("\n");
                    }
                    listas.push(new ContextoLista(false));
                } else if (tag.equals("ol")) {
                    if (listas.isEmpty()) {
                        prepararBloque(actual);
                    } else {
                        actual.append("\n");
                    }
                    listas.push(new ContextoLista(true));
                } else if (tag.equals("li")) {
                    int profundidad = Math.max(0, listas.size() - 1);
                    String indent = "  ".repeat(profundidad);
                    ContextoLista ctx = listas.peek();
                    if (ctx != null && ctx.ordenada) {
                        ctx.contador++;
                        actual.append(indent).append(ctx.contador).append(". ");
                    } else {
                        actual.append(indent).append("- ");
                    }
                }
            }

            @Override
            public void handleEndTag(HTML.Tag t, int pos) {
                String tag = t.toString().toLowerCase();
                if (!etiquetasAbiertas.isEmpty()) {
                    etiquetasAbiertas.pop();
                }

                if (omitirNivel > 0) {
                    if (tag.equals("script") || tag.equals("style")) {
                        omitirNivel--;
                    }
                    return;
                }

                StringBuilder actual = pilaBuffers.peek();

                if (tag.matches("h[1-6]")) {
                    nivelEncabezado[0] = 0;
                    actual.append("\n\n");
                } else if (tag.equals("p") || tag.equals("blockquote")) {
                    actual.append("\n\n");
                } else if (tag.equals("pre")) {
                    if (actual.length() > 0 && actual.charAt(actual.length() - 1) != '\n') {
                        actual.append("\n");
                    }
                    actual.append("```\n\n");
                    enBloqueCodigo = false;
                } else if (tag.equals("code") && !enBloqueCodigo) {
                    actual.append("`");
                    enCodigoLinea = false;
                } else if (tag.equals("b") || tag.equals("strong")) {
                    actual.append("**");
                } else if (tag.equals("i") || tag.equals("em")) {
                    actual.append("*");
                } else if (tag.equals("a")) {
                    String textoEnlace = pilaBuffers.pop().toString().trim();
                    String urlResuelta = enlacesActivos.isEmpty() ? "" : enlacesActivos.pop();
                    String destino = urlResuelta.isEmpty() ? textoEnlace : "[" + textoEnlace + "](" + urlResuelta + ")";
                    pilaBuffers.peek().append(destino);
                } else if (tag.equals("li")) {
                    if (actual.length() > 0 && actual.charAt(actual.length() - 1) != '\n') {
                        actual.append("\n");
                    }
                } else if (tag.equals("ul") || tag.equals("ol")) {
                    if (!listas.isEmpty()) {
                        listas.pop();
                    }
                    if (listas.isEmpty()) {
                        actual.append("\n");
                    }
                }
            }

            @Override
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (omitirNivel > 0) {
                    return;
                }
                String tag = t.toString().toLowerCase();
                StringBuilder actual = pilaBuffers.peek();

                if (tag.equals("br")) {
                    actual.append("  \n");
                } else if (tag.equals("hr")) {
                    prepararBloque(actual);
                    actual.append("---\n\n");
                } else if (tag.equals("img")) {
                    String src = obtenerAtributo(a, HTML.Attribute.SRC);
                    String alt = obtenerAtributo(a, HTML.Attribute.ALT);
                    actual.append("![").append(alt).append("](").append(resolverUrl(baseUrl, src)).append(")");
                }
            }

            @Override
            public void handleText(char[] data, int pos) {
                if (omitirNivel > 0) {
                    return;
                }
                String texto = new String(data);
                if (enBloqueCodigo || enCodigoLinea) {
                    pilaBuffers.peek().append(texto);
                    return;
                }

                String limpio = texto.replaceAll("\\s+", " ");
                if (limpio.isBlank()) {
                    return;
                }
                pilaBuffers.peek().append(escaparMarkdown(limpio));
            }
        });

        return salida.toString().replaceAll("\\n{3,}", "\n\n").trim() + "\n";
    }

    private void prepararBloque(StringBuilder sb) {
        if (sb.length() == 0) {
            return;
        }
        if (!sb.toString().endsWith("\n\n")) {
            if (sb.charAt(sb.length() - 1) == '\n') {
                sb.append("\n");
            } else {
                sb.append("\n\n");
            }
        }
    }

    private String obtenerAtributo(MutableAttributeSet attrs, HTML.Attribute attr) {
        if (attrs == null) {
            return "";
        }
        Object val = attrs.getAttribute(attr);
        return val != null ? val.toString().trim() : "";
    }

    private String resolverUrl(URL base, String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return "";
        }
        if (base == null) {
            return referencia;
        }
        try {
            return new URL(base, referencia).toString();
        } catch (MalformedURLException e) {
            return referencia;
        }
    }

    private String escaparMarkdown(String texto) {
        return texto.replace("\\", "\\\\")
                    .replace("*", "\\*")
                    .replace("_", "\\_")
                    .replace("[", "\\[")
                    .replace("]", "\\]");
    }
} 
