package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/**
 * Clase utilitaria encargada de administrar el ciclo de vida de los reportes HTML con ExtentReports.
 * Implementa el patrón Singleton básico y ThreadLocal para soportar reportes ordenados y seguros entre hilos.
 */
public class ReportManager {

    // Instancia única del reporte global
    private static ExtentReports extent;
    private static ReportManager instance;
    
    // ThreadLocal permite aislar la información de cada prueba individual sin interferencias
    private static final ThreadLocal<ExtentTest> testHarness = new ThreadLocal<>();

    /**
     * Retorna la instancia singleton de ReportManager para máxima compatibilidad.
     */
    public static synchronized ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
            iniciarReporte();
        }
        return instance;
    }

    /**
     * Inicializa y configura el reporte HTML ExtentReports si aún no ha sido creado.
     * Define la ruta del archivo de salida, el título y el tema visual.
     */
    public static synchronized ExtentReports iniciarReporte() {
        if (extent == null) {
            // Aseguramos que el directorio 'reports' exista en el proyecto
            File directorioReportes = new File("reports");
            if (!directorioReportes.exists()) {
                directorioReportes.mkdirs();
            }

            // Configuramos el reporter Spark HTML en la carpeta reports
            String rutaReporte = "reports/ExtentReport.html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(rutaReporte);
            
            // Personalización estética del reporte
            sparkReporter.config().setReportName("Reporte de Automatización - OrangeHRM");
            sparkReporter.config().setDocumentTitle("Test Results - Proyecto Final QA");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setEncoding("UTF-8");

            // Creamos la instancia principal de ExtentReports y vinculamos el reporter
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // Información de contexto del sistema para el reporte
            extent.setSystemInfo("Proyecto", "Automatización OrangeHRM");
            extent.setSystemInfo("Framework", "Selenium WebDriver 4 + TestNG");
            extent.setSystemInfo("Patrón", "Page Object Model (POM)");
            extent.setSystemInfo("Usuario QA", System.getProperty("user.name"));
            extent.setSystemInfo("SO", System.getProperty("os.name"));
        }
        return extent;
    }

    /**
     * Registra un nuevo caso de prueba en el reporte HTML.
     * @param nombreTest Nombre que aparecerá en el reporte
     * @param descripcion Breve explicación del objetivo de la prueba
     */
    public static void crearTest(String nombreTest, String descripcion) {
        ExtentTest test = iniciarReporte().createTest(nombreTest, descripcion);
        testHarness.set(test);
    }

    /**
     * Obtiene la instancia activa del test actual para registrar pasos (INFO, PASS, FAIL, WARNING).
     * @return Instancia ExtentTest del hilo en ejecución
     */
    public static ExtentTest getTest() {
        return testHarness.get();
    }

    /**
     * Escribe físicamente todos los eventos acumulados en el archivo HTML final.
     * Debe llamarse al final de toda la suite de pruebas.
     */
    public static synchronized void finalizarReporte() {
        if (extent != null) {
            extent.flush();
        }
    }
}
