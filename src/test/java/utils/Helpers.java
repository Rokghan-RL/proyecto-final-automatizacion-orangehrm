package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria que provee funciones auxiliares comunes:
 * - Generador de identificadores dinámicos únicos (para cumplir la regla anti-duplicados).
 * - Esperas explícitas para sincronización con elementos interactivos.
 * - Acciones seguras sobre elementos web (clics limpios, escritura robusta).
 * - Lector de archivos CSV compatible con TestNG DataProvider.
 */
public class Helpers {

    /**
     * Genera un sufijo numérico único basado en el tiempo actual en milisegundos.
     * Ejemplo: "48291"
     * Esto evita errores por nombres de usuario o IDs de empleado duplicados en el sistema OrangeHRM.
     * 
     * @return String con un número único de 5 dígitos
     */
    public static String generarSufijoUnico() {
        // Tomamos el residuo de milisegundos para obtener un número corto y único
        long timestamp = System.currentTimeMillis() % 100000;
        return String.valueOf(timestamp);
    }

    /**
     * Espera de manera explícita a que un elemento web esté visible en el DOM y en pantalla.
     * 
     * @param driver Instancia del WebDriver en ejecución
     * @param locator Localizador By del elemento
     * @param tiempoSegundos Tiempo máximo de espera en segundos
     * @return WebElement una vez que esté visible
     */
    public static WebElement esperarVisibilidad(WebDriver driver, By locator, int tiempoSegundos) {
        // WebDriverWait sondea el DOM continuamente hasta que se cumple la condición o expira el tiempo
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(tiempoSegundos));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Espera de manera explícita a que un elemento web sea clickeable (visible y habilitado).
     * 
     * @param driver Instancia del WebDriver en ejecución
     * @param locator Localizador By del elemento
     * @param tiempoSegundos Tiempo máximo de espera en segundos
     * @return WebElement listo para recibir clic
     */
    public static WebElement esperarClickeable(WebDriver driver, By locator, int tiempoSegundos) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(tiempoSegundos));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Espera de manera explícita a que un elemento (como un spinner, loader o cortina de carga)
     * desaparezca o se vuelva invisible en el DOM.
     * Esencial para evitar condiciones de carrera con loaders (ej: .oxd-form-loader en Firefox / geckodriver).
     * 
     * @param driver Instancia del WebDriver en ejecución
     * @param locator Localizador By del elemento a desaparecer
     * @param tiempoSegundos Tiempo máximo de espera en segundos
     * @return boolean true si desapareció exitosamente
     */
    public static boolean esperarInvisibilidad(WebDriver driver, By locator, int tiempoSegundos) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(tiempoSegundos));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Realiza un clic seguro sobre un elemento web tras esperar a que sea clickeable.
     * Si una animación o overlay de OrangeHRM intercepta el clic nativo, recurre a JavaScriptExecutor como respaldo.
     * 
     * @param driver Instancia del WebDriver
     * @param locator Localizador By del elemento a presionar
     */
    public static void hacerClic(WebDriver driver, By locator) {
        try {
            // Espera hasta 10 segundos y ejecuta el clic nativo de Selenium
            WebElement elemento = esperarClickeable(driver, locator, 10);
            elemento.click();
        } catch (Exception e) {
            // Respaldo vía JavaScript en caso de overlay o interceptación de eventos por el framework web
            WebElement elemento = driver.findElement(locator);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", elemento);
        }
    }

    /**
     * Limpia un campo de texto y escribe el valor indicado de forma segura.
     * En OrangeHRM, campos como 'Employee Id' vienen con valores autogenerados; por ello,
     * este método selecciona todo el texto previo y lo borra con teclas de teclado antes de escribir.
     * 
     * @param driver Instancia del WebDriver
     * @param locator Localizador By del campo de texto
     * @param texto Valor que se desea escribir
     */
    public static void escribir(WebDriver driver, By locator, String texto) {
        WebElement campo = esperarVisibilidad(driver, locator, 10);
        
        // 1. Clic para enfocar el campo
        campo.click();

        // 2. Limpieza exhaustiva: Seleccionar todo (Ctrl/Cmd + A) y presionar Backspace
        campo.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        campo.sendKeys(Keys.chord(Keys.COMMAND, "a")); // Para compatibilidad en macOS
        campo.sendKeys(Keys.BACK_SPACE);
        
        // Limpieza nativa adicional
        campo.clear();

        // 3. Escribir el nuevo texto
        campo.sendKeys(texto);
    }

    /**
     * Lee un archivo CSV de datos y lo transforma en una matriz bidimensional Object[][]
     * requerida por los DataProviders de TestNG.
     * Busca el archivo tanto en la ruta del sistema de archivos como en la carpeta resources del classpath.
     * 
     * @param rutaArchivo Ruta relativa o nombre del archivo CSV
     * @return Matriz Object[][] donde cada fila representa un caso de prueba
     */
    public static Object[][] leerDatosCsv(String rutaArchivo) {
        List<Object[]> listaFilas = new ArrayList<>();

        try {
            BufferedReader lector;
            
            // Intentamos cargar el archivo desde el Classpath (src/test/resources)
            InputStream is = Helpers.class.getClassLoader().getResourceAsStream(rutaArchivo);
            if (is != null) {
                lector = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            } else {
                // Si no está en el classpath, lo abrimos directamente desde el sistema de archivos
                lector = new BufferedReader(new FileReader(rutaArchivo, StandardCharsets.UTF_8));
            }

            String linea;
            boolean esPrimeraLinea = true;

            // Leemos línea por línea el archivo CSV
            while ((linea = lector.readLine()) != null) {
                // Saltamos la fila de cabeceras (headers: firstName, middleName, etc.)
                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue;
                }

                // Omitimos líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }

                // Dividimos los campos separados por comas
                String[] datos = linea.split(",");
                for (int i = 0; i < datos.length; i++) {
                    datos[i] = datos[i].trim();
                }

                listaFilas.add(datos);
            }
            lector.close();

        } catch (Exception e) {
            System.err.println("Error al leer el archivo CSV de datos: " + e.getMessage());
            e.printStackTrace();
        }

        // Convertimos la lista dinámica en la matriz bidimensional que exige TestNG
        return listaFilas.toArray(new Object[0][0]);
    }
}
