package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Helpers;

/**
 * Page Object correspondiente a la pantalla de Inicio de Sesión (Login) de OrangeHRM.
 * Regla de Oro:
 * - Los localizadores (By) y las interacciones físicas (click, sendKeys) se definen aquí.
 * - Esta clase NUNCA debe contener aserciones (Assert).
 */
public class LoginPage {

    // Instancia del WebDriver recibida desde la prueba
    private final WebDriver driver;

    // =========================================================================
    // LOCALIZADORES (By): Encapsulados como privados para proteger su acceso
    // =========================================================================
    private final By campoUsuario = By.name("username");
    private final By campoContrasena = By.name("password");
    private final By botonLogin = By.cssSelector("button[type='submit']");
    private final By contenedorLogin = By.cssSelector(".orangehrm-login-slot");

    /**
     * Constructor del Page Object.
     * Recibe la sesión activa de Selenium creada en BaseTest.
     * 
     * @param driver Driver en ejecución
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // ACCIONES / MÉTODOS DE INTERACCIÓN
    // =========================================================================

    /**
     * Navega a la URL especificada para iniciar el flujo de prueba.
     * 
     * @param url Dirección web de OrangeHRM
     */
    public void navegarAlLogin(String url) {
        driver.get(url);
        // Esperamos a que la caja de login esté visible antes de continuar
        Helpers.esperarVisibilidad(driver, contenedorLogin, 15);
    }

    /**
     * Escribe el nombre de usuario en el campo correspondiente.
     * 
     * @param usuario Nombre de usuario (ej: Admin)
     */
    public void ingresarUsuario(String usuario) {
        Helpers.escribir(driver, campoUsuario, usuario);
    }

    /**
     * Escribe la contraseña en el campo correspondiente.
     * 
     * @param contrasena Clave del usuario (ej: admin123)
     */
    public void ingresarContrasena(String contrasena) {
        Helpers.escribir(driver, campoContrasena, contrasena);
    }

    /**
     * Presiona el botón de inicio de sesión.
     */
    public void presionarBotonLogin() {
        Helpers.hacerClic(driver, botonLogin);
    }

    /**
     * Método compuesto de negocio que resume el flujo de login administrativo.
     * Facilita que las clases de prueba se lean limpias y legibles.
     * 
     * @param url Dirección del sitio
     * @param usuario Usuario con permisos
     * @param contrasena Contraseña del usuario
     */
    public void iniciarSesionComoAdmin(String url, String usuario, String contrasena) {
        navegarAlLogin(url);
        ingresarUsuario(usuario);
        ingresarContrasena(contrasena);
        presionarBotonLogin();
    }
}
