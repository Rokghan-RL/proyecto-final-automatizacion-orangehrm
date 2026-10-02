package base;

import com.aventstack.extentreports.Status;
import helper.ScreenShotHelper;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Clase Base de la cual heredan todas las clases de prueba (Test Classes).
 * Responsabilidades:
 * 1. Inicializar y cerrar la suite de reportes HTML (ExtentReports).
 * 2. Gestionar la creación del WebDriver según el navegador especificado (Cross-browser).
 * 3. Registrar el estado final de cada prueba (Pass / Fail) en el reporte.
 * 4. Capturar pantalla automáticamente y adjuntarla al reporte HTML ante fallos o éxitos (ScreenShotHelper).
 * 5. Limpiar y cerrar el navegador tras cada prueba para garantizar aislamiento.
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
            driver = new FirefoxDriver(options);
        } else {
            // Configuración optimizada de Google Chrome sin alertas invasivas de contraseñas
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-features=PasswordLeakDetection,AutofillServerCommunication");

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            prefs.put("profile.password_manager_leak_detection", false);
            options.setExperimentalOption("prefs", prefs);

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
     * Evalúa el resultado final, toma captura de pantalla para el reporte HTML y cierra la instancia.
     * 
     * @param result Objeto de TestNG que contiene el estado de ejecución (PASS, FAIL, SKIP)
     */
    @AfterMethod
    public void tearDown(ITestResult result) {
        try {
            // Registramos el resultado y adjuntamos screenshot según el estado
            if (result.getStatus() == ITestResult.FAILURE) {
                ReportManager.getTest().log(Status.FAIL, "❌ La prueba falló: " + result.getThrowable());
                // Captura automática de pantalla al fallar (Failure Image)
                ScreenShotHelper.takeScreenShotAndAdToHTMLReport(driver, Status.FAIL, "Captura del Fallo / Failure Image");
            } else if (result.getStatus() == ITestResult.SUCCESS) {
                ReportManager.getTest().log(Status.PASS, "✅ La prueba concluyó exitosamente.");
            } else if (result.getStatus() == ITestResult.SKIP) {
                ReportManager.getTest().log(Status.SKIP, "⚠️ La prueba fue omitida (Skipped).");
            }
        } catch (Exception e) {
            System.err.println(">>> [ERROR en AfterMethod]: " + e.getMessage());
        } finally {
            // Cerramos el navegador y liberamos la sesión de WebDriver
            if (driver != null) {
                ReportManager.getTest().log(Status.INFO, "Cerrando navegador...");
                driver.quit();
                System.out.println(">>> [INFO] Navegador cerrado exitosamente.");
            }
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
