package tests;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import helper.ScreenShotHelper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.PimPage;
import utils.Helpers;
import utils.ReportManager;

/**
 * Clase de Prueba Principal para el flujo de automatización en OrangeHRM.
 * 
 * Reglas de Arquitectura aplicadas:
 * 1. Hereda de BaseTest para reutilizar el ciclo de vida del WebDriver y ReportManager.
 * 2. Cero Localizadores: NO utiliza 'driver.findElement'. Toda interacción se delega a las Pages.
 * 3. Legibilidad de Negocio: Se lee como pasos de usuario en lenguaje claro.
 * 4. Aserciones Centralizadas: Todas las validaciones 'Assert' se ejecutan aquí.
 * 5. Data-Driven Testing: Los datos se consumen desde un archivo CSV con TestNG DataProvider.
 * 6. Datos Únicos en Vivo: Se concatena un identificador dinámico para evitar duplicidad.
 */
public class OrangeHrmTest extends BaseTest {

    // Constante con la URL base del entorno de pruebas
    private final String URL_APP = "https://opensource-demo.orangehrmlive.com/";

    /**
     * DataProvider de TestNG que lee el archivo de datos 'testdata.csv'
     * y entrega cada fila como parámetros al método de prueba.
     * 
     * @return Matriz de objetos con los datos de cada empleado a probar
     */
    @DataProvider(name = "proveedorDatosEmpleados")
    public Object[][] obtenerDatosEmpleados() {
        // Leemos los registros desde el archivo CSV usando nuestro Helper
        return Helpers.leerDatosCsv("testdata.csv");
    }

    /**
     * Caso de Prueba: Creación y verificación de empleado con credenciales de acceso.
     * Este test se ejecutará tantas veces como empleados existan en el archivo CSV (2 iteraciones).
     * 
     * @param primerNombre Nombre base leído del CSV
     * @param segundoNombre Segundo nombre leído del CSV
     * @param apellido Apellido leído del CSV
     * @param idEmpleado ID base del empleado leído del CSV
     * @param username Usuario base para login leído del CSV
     * @param password Contraseña leída del CSV
     * @param status Estado de la cuenta (Enabled/Disabled)
     */
    @Test(
        dataProvider = "proveedorDatosEmpleados",
        description = "Validar el alta y búsqueda de un nuevo empleado con credenciales de login en el módulo PIM"
    )
    public void testCrearYBuscarEmpleadoConLogin(
            String primerNombre,
            String segundoNombre,
            String apellido,
            String idEmpleado,
            String username,
            String password,
            String status) {

        // =====================================================================
        // PASO 0: REGLA DINÁMICA - Generar valores únicos en tiempo de ejecución
        // =====================================================================
        String sufijo = Helpers.generarSufijoUnico();
        String idDinamico = idEmpleado + sufijo;       // Ejemplo: "8001" + "4921" -> "80014921"
        String usuarioDinamico = username + "_" + sufijo; // Ejemplo: "cgomez_4921"
        String apellidoDinamico = apellido + "_" + sufijo; // Permite reconocerlo fácilmente en pantalla

        ReportManager.getTest().log(Status.INFO, "Iniciando caso con Empleado ID: " + idDinamico + " y Usuario: " + usuarioDinamico);

        // =====================================================================
        // PASO 1: Instanciar los Page Objects requeridos
        // =====================================================================
        LoginPage loginPage = new LoginPage(driver);
        PimPage pimPage = new PimPage(driver);

        // =====================================================================
        // PASO 2: Iniciar sesión en OrangeHRM con credenciales de Administrador
        // =====================================================================
        ReportManager.getTest().log(Status.INFO, "Paso 1: Iniciando sesión como Administrador en OrangeHRM.");
        loginPage.iniciarSesionComoAdmin(URL_APP, "Admin", "admin123");

        // =====================================================================
        // PASO 3: Crear el nuevo empleado en el módulo PIM
        // =====================================================================
        ReportManager.getTest().log(Status.INFO, "Paso 2: Navegando al módulo PIM y creando nuevo empleado con credenciales.");
        pimPage.crearEmpleadoConCredenciales(
                primerNombre,
                segundoNombre,
                apellidoDinamico,
                idDinamico,
                usuarioDinamico,
                password,
                status
        );

        // =====================================================================
        // PASO 4: Buscar al empleado recién creado en la lista de empleados
        // =====================================================================
        ReportManager.getTest().log(Status.INFO, "Paso 3: Buscando el empleado creado por ID: " + idDinamico);
        pimPage.buscarEmpleadoPorId(idDinamico);

        // =====================================================================
        // PASO 5: ASERCIÓN OBLIGATORIA (Regla POM: El Assert se ejecuta aquí)
        // =====================================================================
        ReportManager.getTest().log(Status.INFO, "Paso 4: Validando presencia del empleado en la grilla de resultados.");
        boolean estaEnLista = pimPage.estaEmpleadoEnResultados(idDinamico);

        Assert.assertTrue(
                estaEnLista,
                "ERROR: El empleado con ID " + idDinamico + " (" + primerNombre + " " + apellidoDinamico + ") " +
                "no apareció en la grilla de resultados del módulo PIM."
        );

        ReportManager.getTest().log(Status.PASS, "¡Aserción exitosa! El empleado " + idDinamico + " figura en el listado de PIM.");
        // Captura de pantalla de evidencia adjunta al reporte HTML
        ScreenShotHelper.takeScreenShotAndAdToHTMLReport(driver, Status.PASS, "Evidencia: Empleado " + idDinamico + " encontrado en la grilla de PIM");
    }
}
