package helper;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import utils.ReportManager;

/**
 * Clase utilitaria encargada de la captura de pantallas durante las pruebas automatizadas
 * e inserción directa en el reporte interactivo HTML de ExtentReports.
 * 
 * Implementación basada en ProyectoFinalOrangeHRM-Master:
 * - Utiliza formato BASE64 para que el reporte HTML sea 100% portable y autónomo.
 * - No depende de almacenamiento de archivos de imagen externos ni rutas locales.
 */
public class ScreenShotHelper {

    /**
     * Captura la pantalla del navegador actual en formato de cadena Base64.
     * 
     * @param webDriver Instancia activa del WebDriver
     * @return String codificado en Base64 con la imagen
     */
    public static String takeScreenShot(WebDriver webDriver) {
        TakesScreenshot takesScreenshot = (TakesScreenshot) webDriver;
        return takesScreenshot.getScreenshotAs(OutputType.BASE64);
    }

    /**
     * Captura la pantalla y la incrusta de forma visible en el reporte HTML de ExtentReports
     * con el estado y descripción proporcionados.
     * 
     * @param webDriver Instancia activa del WebDriver
     * @param status Estado del paso (Status.PASS, Status.FAIL, Status.INFO)
     * @param details Descripción o título explicativo de la captura
     */
    public static void takeScreenShotAndAdToHTMLReport(WebDriver webDriver, Status status, String details) {
        try {
            if (webDriver != null && ReportManager.getTest() != null) {
                String imageBase64 = takeScreenShot(webDriver);
                ReportManager.getTest().log(
                    status,
                    details,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(imageBase64).build()
                );
            }
        } catch (Exception e) {
            System.err.println(">>> [ADVERTENCIA] No fue posible capturar la pantalla: " + e.getMessage());
        }
    }
}
