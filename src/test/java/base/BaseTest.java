package base;

import com.aventstack.extentreports.Status;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.ReportManager;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Clase Base de la cual heredan todas las clases de prueba (Test Classes).
 * Responsabilidades:
 * 1. Inicializar y cerrar la suite de reportes HTML (ExtentReports).
 * 2. Gestionar la creación del WebDriver según el navegador especificado (Cross-browser).
 * 3. Registrar el estado final de cada prueba (Pass / Fail) en el reporte.
 * 4. Limpiar y cerrar el navegador tras cada prueba para garantizar aislamiento.
 */
public class BaseTest {

    // Instancia del WebDriver protegida para que las clases hijas puedan acceder a ella
    protected WebDriver driver;

    /**
     * Se ejecuta una sola vez antes de que arranque cualquier prueba de la suite.
     * Prepara el entorno del ReportManager.
     */
    @BeforeSuite
    public void setupSuite() {
        System.out.println(">>> [INFO] Inicializando ExtentReports para la suite de pruebas...");
        ReportManager.iniciarReporte();
    }

    /**
     * Se ejecuta antes de cada método de prueba (@Test).
     * Recibe el parámetro del navegador desde 'testng.xml' (ej: chrome o firefox).
     * Si se corre la prueba individualmente sin suite, toma "chrome" como valor por defecto.
     * 
     * @param browser Nombre del navegador a levantar ("chrome" o "firefox")
     * @param method Información reflectiva del método de prueba en ejecución
     */
    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser, Method method) {
        System.out.println(">>> [INFO] Iniciando prueba: " + method.getName() + " en navegador: " + browser);

        // 1. Configuración de Cross-Browser usando Selenium 4 (Selenium Manager resuelve los drivers automáticamente)
        if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            // Permite ejecuciones en servidores sin interfaz gráfica si se activa headless
            // options.addArguments("-headless");
            driver = new FirefoxDriver(options);
        } else {
            // Por defecto usamos Google Chrome
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            // options.addArguments("--headless=new"); // Activar si se desea modo headless
            driver = new ChromeDriver(options);
        }

        // 2. Ajustes iniciales de la ventana y timeouts básicos
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // 3. Crear el nodo de la prueba en ExtentReports
        ReportManager.crearTest(
            method.getName() + " [" + browser.toUpperCase() + "]", 
            "Ejecución automatizada en OrangeHRM usando " + browser
        );
        ReportManager.getTest().log(Status.INFO, "Navegador " + browser + " iniciado correctamente.");
    }

    /**
     * Se ejecuta inmediatamente después de cada prueba (@Test).
     * Evalúa el resultado final y cierra la instancia del navegador.
     * 
     * @param result Objeto de TestNG que contiene el estado de ejecución (PASS, FAIL, SKIP)
     */
    @AfterMethod
    public void tearDown(ITestResult result) {
        // Registramos el resultado en el reporte ExtentReports
        if (result.getStatus() == ITestResult.FAILURE) {
            ReportManager.getTest().log(Status.FAIL, "❌ La prueba falló: " + result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            ReportManager.getTest().log(Status.PASS, "✅ La prueba concluyó exitosamente.");
        } else if (result.getStatus() == ITestResult.SKIP) {
            ReportManager.getTest().log(Status.SKIP, "⚠️ La prueba fue omitida (Skipped).");
        }

        // Cerramos el navegador y liberamos la sesión de WebDriver
        if (driver != null) {
            ReportManager.getTest().log(Status.INFO, "Cerrando navegador...");
            driver.quit();
            System.out.println(">>> [INFO] Navegador cerrado exitosamente.");
        }
    }

    /**
     * Se ejecuta una sola vez al terminar todas las pruebas de la suite.
     * Escribe físicamente el archivo HTML de ExtentReports en disco.
     */
    @AfterSuite
    public void tearDownSuite() {
        System.out.println(">>> [INFO] Finalizando y generando reporte HTML...");
        ReportManager.finalizarReporte();
        System.out.println(">>> [INFO] Reporte generado en: reports/ExtentReport.html");
    }
}
