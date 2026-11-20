# HTML2Markdown

ESTUDIANTE: Santiago Beltran Astorga  
EXPEDIENTE: 225203551  

Un conversor de documentos HTML a formato Markdown desarrollado en Java. Este sistema permite descargar páginas web a través de peticiones HTTP/HTTPS o procesar contenido HTML local, parsear la estructura del documento y generar una representación limpia en Markdown preservando la semántica del contenido original.

## Arquitectura y Estructura del Proyecto

El proyecto está diseñado de manera modular para separar las responsabilidades de red, parsing HTML, conversión a Markdown y persistencia en disco.

```
HTML2Markdown/
├── README.md
├── .gitignore
├── src/
│   └── main/
│       └── java/
│           ├── HTML2Markdown.java      
│           ├── WebClient.java          
│           ├── HtmlParser.java         
│           ├── MarkdownConverter.java  
│           └── MarkdownWriter.java     
├── tests/
└── output/
```

### Componentes Principales

* **HTML2Markdown**: Orquesta el flujo completo de la aplicación, valida argumentos de entrada y maneja la interfaz de línea de comandos.
* **WebClient**: Realiza peticiones HTTP/HTTPS, valida códigos de respuesta, codificación de caracteres y limita el tamaño máximo de descarga.
* **HtmlParser**: Utiliza ParserDelegator de Java Swing para iterar sobre el documento HTML mediante eventos.
* **MarkdownConverter**: Transforma la estructura de bloques y elementos en línea a sintaxis Markdown.
* **MarkdownWriter**: Determina las rutas de salida y escribe el archivo final en UTF-8.

## Compilación y Ejecución

### 1. Compilar el proyecto

Desde la raíz del proyecto, ejecuta:

```cmd
javac -d src\main\java src\main\java\*.java
```

### 2. Ejecutar la aplicación

Para ejecutar la conversión sobre una URL:

```cmd
java -cp src\main\java HTML2Markdown <URL> [archivo-salida.md]
```

Ejemplo de uso:

```cmd
java -cp src\main\java HTML2Markdown http://localhost:8000/encabezados.html output\p04_encabezados.md
```

## Preguntas de Reflexión

### 1. ¿Qué papel desempeña la aplicación dentro de una arquitectura cliente-servidor?
Actúa como un cliente HTTP que solicita recursos web a un servidor remoto o local para consumirlos y procesarlos localmente.

### 2. ¿Qué responsabilidades corresponden al servidor y cuáles al cliente?
El servidor almacena y entrega los recursos HTML solicitados, mientras que el cliente descarga, parsea, procesa y convierte el contenido.

### 3. ¿Qué diferencia existe entre recuperar HTML e interpretar su estructura?
Recuperar HTML es transferir el texto plano por red; interpretarlo implica analizar su árbol sintáctico (DOM/eventos) para entender su jerarquía y semántica.

### 4. ¿Por qué no basta reemplazar etiquetas HTML con expresiones regulares?
Porque HTML es un lenguaje libre de contexto con anidamiento arbitrario, etiquetas opcionales y atributos complejos que las expresiones regulares no pueden procesar de forma confiable.

### 5. ¿Qué dificultades presentan los elementos anidados?
Requieren mantener una pila de estados para cerrar correctamente las etiquetas en orden inverso y aplicar adecuadamente los sangrados o formatos acumulados.

### 6. ¿Qué información puede perderse al convertir HTML a Markdown?
Se pierden los estilos CSS, scripts de interacción, atributos semánticos complejos, estructuras avanzadas como formularios o canvas, y maquetaciones dinámicas.

### 7. ¿Por qué deben resolverse las URL relativas?
Porque las rutas relativas solo funcionan dentro del contexto del servidor original y fallarían al ser abiertas desde un archivo Markdown almacenado localmente.

### 8. ¿Qué ventajas ofrece separar descarga, análisis y conversión en clases?
Mejora la mantenibilidad, facilita la realización de pruebas unitarias independientes y permite modificar o reemplazar cualquier módulo sin afectar al resto.

### 9. ¿Qué limitaciones presenta ParserDelegator con HTML moderno?
Está basado en HTML 3.2/4.0, por lo que no reconoce nativamente etiquetas de HTML5 (`<article>`, `<section>`, `<nav>`) ni maneja construcciones modernas de forma óptima.

### 10. ¿Qué modificaciones serían necesarias para convertir tablas HTML a Markdown?
Se requeriría procesar etiquetas `<table>`, `<tr>`, `<th>` y `<td>`, construyendo filas formateadas con tubos (`|`) y calcular el ancho de columnas o alinear encabezados. 
