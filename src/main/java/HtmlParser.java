import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class HtmlParser {

    public void parsear(String html, HTMLEditorKit.ParserCallback callback) throws IOException {
        ParserDelegator delegator = new ParserDelegator();
        delegator.parse(new StringReader(html), callback, true);
    }

    public static void imprimirAtributos(MutableAttributeSet atributos) {
        if (atributos == null) {
            return;
        }
        Enumeration<?> nombres = atributos.getAttributeNames();
        while (nombres.hasMoreElements()) {
            Object nombre = nombres.nextElement();
            Object valor = atributos.getAttribute(nombre);
            if (nombre != null && valor != null) {
                System.out.println("  " + nombre + ": " + valor);
            }
        }
    }
} 
