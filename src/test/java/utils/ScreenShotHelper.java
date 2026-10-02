package utils;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Clase utilitaria encargada de la captura de pantallas durante las pruebas automatizadas
 * e inserción directa en el reporte interactivo HTML de ExtentReports.
 * Ubicada en paquete utils para consistencia de importaciones.
 */
public class ScreenShotHelper {

    public static String takeScreenShot(WebDriver webDriver) {
        TakesScreenshot takesScreenshot = (TakesScreenshot) webDriver;
        return takesScreenshot.getScreenshotAs(OutputType.BASE64);
    }

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
