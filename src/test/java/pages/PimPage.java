package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.Helpers;

import java.util.List;

/**
 * Page Object correspondiente al módulo PIM (Personnel Information Management).
 * Gestiona:
 * - Navegación dentro del módulo PIM.
 * - Formulario de creación de empleados (Datos personales y detalles de inicio de sesión).
 * - Búsqueda de empleados en el listado (Employee List) y consulta de resultados.
 * 
 * Regla POM: Ninguna aserción (Assert) vive en esta clase.
 */
public class PimPage {

    private final WebDriver driver;

    // =========================================================================
    // LOCALIZADORES (By) - Menú y Navegación
    // =========================================================================
    private final By opcionMenuPim = By.xpath("//aside//span[text()='PIM']");
    private final By pestanaAddEmployee = By.xpath("//nav//a[normalize-space()='Add Employee']");
    private final By pestanaEmployeeList = By.xpath("//nav//a[normalize-space()='Employee List']");

    // Locators de spinners y cortinas de carga (vital para evitar intercepción de clics en Firefox/geckodriver)
    private final By loaderFormulario = By.className("oxd-form-loader");
    private final By spinnerCarga = By.cssSelector(".oxd-loading-spinner, .oxd-form-loader");

    // =========================================================================
    // LOCALIZADORES (By) - Formulario Nuevo Empleado (Add Employee)
    // =========================================================================
    private final By campoPrimerNombre = By.name("firstName");
    private final By campoSegundoNombre = By.name("middleName");
    private final By campoApellido = By.name("lastName");
    private final By campoIdEmpleado = By.xpath("//label[text()='Employee Id']/parent::div/following-sibling::div/input");

    // Switch "Create Login Details" y sus campos derivados
    private final By switchDetallesLogin = By.xpath("//div[contains(@class, 'oxd-switch-wrapper')]//span[contains(@class, 'oxd-switch-input')]");
    private final By campoLoginUsuario = By.xpath("//label[text()='Username']/parent::div/following-sibling::div/input");
    private final By radioStatusEnabled = By.xpath("//label[text()='Status']/parent::div/following-sibling::div//label[text()='Enabled']//span");
    private final By radioStatusDisabled = By.xpath("//label[text()='Status']/parent::div/following-sibling::div//label[text()='Disabled']//span");
    private final By campoPassword = By.xpath("//label[text()='Password']/parent::div/following-sibling::div/input");
    private final By campoConfirmarPassword = By.xpath("//label[text()='Confirm Password']/parent::div/following-sibling::div/input");
    private final By botonGuardar = By.xpath("//button[@type='submit']");
    private final By toastExito = By.xpath("//div[contains(@class, 'oxd-toast')]");

    // =========================================================================
    // LOCALIZADORES (By) - Búsqueda en Employee List y Grilla
    // =========================================================================
    private final By campoBuscarIdEmpleado = By.xpath("//label[text()='Employee Id']/parent::div/following-sibling::div/input");
    private final By botonBuscar = By.xpath("//button[@type='submit' and normalize-space()='Search']");
    private final By filasResultadosTabla = By.xpath("//div[@class='oxd-table-body']//div[contains(@class, 'oxd-table-card')]");

    /**
     * Constructor del Page Object PIM.
     * @param driver Driver en ejecución
     */
    public PimPage(WebDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // MÉTODOS DE NAVEGACIÓN DENTRO DE PIM
    // =========================================================================

    /**
     * Hace clic en la opción PIM del menú lateral izquierdo.
     */
    public void irAModuloPim() {
        Helpers.hacerClic(driver, opcionMenuPim);
        // Esperamos a que la pestaña principal de PIM cargue
        Helpers.esperarVisibilidad(driver, pestanaEmployeeList, 15);
    }

    /**
     * Navega a la pestaña de creación de empleados "Add Employee".
     */
    public void irAAgregarEmpleado() {
        Helpers.hacerClic(driver, pestanaAddEmployee);
        // Esperamos a que el spinner/cortina de carga desaparezca
        Helpers.esperarInvisibilidad(driver, loaderFormulario, 10);
        // Esperamos que el campo de primer nombre esté visible
        Helpers.esperarVisibilidad(driver, campoPrimerNombre, 15);
    }

    /**
     * Navega a la pestaña de listado de empleados "Employee List".
     */
    public void irAListaEmpleados() {
        Helpers.hacerClic(driver, pestanaEmployeeList);
        Helpers.esperarVisibilidad(driver, campoBuscarIdEmpleado, 15);
    }

    // =========================================================================
    // MÉTODOS PARA CREACIÓN DE EMPLEADO
    // =========================================================================

    /**
     * Completa los campos básicos de identidad del empleado.
     * Incluye espera explícita a la desaparición del loader para Firefox (geckodriver).
     * 
     * @param primerNombre Primer nombre
     * @param segundoNombre Segundo nombre
     * @param apellido Apellido
     * @param idEmpleado Código identificador único del empleado
     */
    public void completarDatosBasicos(String primerNombre, String segundoNombre, String apellido, String idEmpleado) {
        // 1. Espera explícita a que el spinner/loader del formulario desaparezca (vital para Firefox/geckodriver)
        Helpers.esperarInvisibilidad(driver, loaderFormulario, 10);

        // 2. Continuar con el llenado habitual
        Helpers.escribir(driver, campoPrimerNombre, primerNombre);
        Helpers.escribir(driver, campoSegundoNombre, segundoNombre);
        Helpers.escribir(driver, campoApellido, apellido);
        Helpers.escribir(driver, campoIdEmpleado, idEmpleado);
    }

    /**
     * Activa el switch "Create Login Details" para desplegar los campos de credenciales.
     */
    public void activarSwitchDetallesLogin() {
        Helpers.hacerClic(driver, switchDetallesLogin);
        // Esperamos que el campo de usuario de login se despliegue
        Helpers.esperarVisibilidad(driver, campoLoginUsuario, 10);
    }

    /**
     * Completa los datos de acceso para el empleado (Usuario, Estado y Contraseñas).
     * 
     * @param username Nombre de usuario para el sistema
     * @param password Contraseña válida (mínimo 8 caracteres, números y letras)
     * @param status Estado de la cuenta ("Enabled" o "Disabled")
     */
    public void completarDetallesLogin(String username, String password, String status) {
        Helpers.escribir(driver, campoLoginUsuario, username);

        // Selección del radio button según el estado solicitado
        if (status.equalsIgnoreCase("Disabled")) {
            Helpers.hacerClic(driver, radioStatusDisabled);
        } else {
            Helpers.hacerClic(driver, radioStatusEnabled);
        }

        Helpers.escribir(driver, campoPassword, password);
        Helpers.escribir(driver, campoConfirmarPassword, password);
    }

    /**
     * Presiona el botón de guardar y aguarda la notificación de éxito de OrangeHRM.
     */
    public void guardarEmpleado() {
        Helpers.hacerClic(driver, botonGuardar);
        // Esperamos confirmación visual del toast de guardado y desaparición de spinners
        try {
            Helpers.esperarVisibilidad(driver, toastExito, 15);
            Helpers.esperarInvisibilidad(driver, spinnerCarga, 10);
        } catch (Exception e) {
            System.out.println(">>> [INFO] El toast de éxito desapareció rápidamente o fue redirigido.");
        }
    }

    /**
     * Método compuesto de alto nivel para crear un empleado completo con credenciales de login.
     */
    public void crearEmpleadoConCredenciales(String primerNombre, String segundoNombre, String apellido,
                                            String idEmpleado, String username, String password, String status) {
        irAModuloPim();
        irAAgregarEmpleado();
        completarDatosBasicos(primerNombre, segundoNombre, apellido, idEmpleado);
        activarSwitchDetallesLogin();
        completarDetallesLogin(username, password, status);
        guardarEmpleado();
    }

    // =========================================================================
    // MÉTODOS DE BÚSQUEDA Y CONSULTA EN LISTADO (Sin Aserciones)
    // =========================================================================

    /**
     * Realiza la búsqueda de un empleado por su ID en la lista de empleados.
     * 
     * @param idEmpleado ID del empleado a buscar
     */
    public void buscarEmpleadoPorId(String idEmpleado) {
        irAListaEmpleados();
        // Esperamos que desaparezca cualquier spinner del listado antes de escribir
        Helpers.esperarInvisibilidad(driver, spinnerCarga, 10);
        Helpers.escribir(driver, campoBuscarIdEmpleado, idEmpleado);
        Helpers.hacerClic(driver, botonBuscar);

        // Esperamos a que la grilla termine de filtrar y el loader desaparezca
        Helpers.esperarInvisibilidad(driver, spinnerCarga, 10);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
    }

    /**
     * Verifica si el ID del empleado buscado se encuentra visible en la grilla de resultados.
     * Retorna un valor booleano para que la clase Test realice el Assert correspondiente.
     * 
     * @param idEmpleado ID del empleado que se espera encontrar
     * @return true si el empleado aparece en la grilla; false en caso contrario
     */
    public boolean estaEmpleadoEnResultados(String idEmpleado) {
        // Localizador dinámico que busca el texto exacto del ID en el cuerpo de la tabla
        By celdaConId = By.xpath("//div[@class='oxd-table-body']//div[contains(text(), '" + idEmpleado + "')]");
        List<WebElement> elementosEncontrados = driver.findElements(celdaConId);
        
        return !elementosEncontrados.isEmpty() && elementosEncontrados.get(0).isDisplayed();
    }
}
