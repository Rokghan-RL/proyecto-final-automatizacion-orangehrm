# 🧪 Proyecto Final: Automatización E2E OrangeHRM

Proyecto de pruebas automatizadas end-to-end sobre la plataforma web OrangeHRM (`https://opensource-demo.orangehrmlive.com/`), aplicando el patrón de diseño **Page Object Model (POM)**.

## 🛠️ Tecnologías y Herramientas
* **Lenguaje:** Java (JDK 17+)
* **Framework de Automatización:** Selenium WebDriver
* **Framework de Pruebas:** TestNG
* **Gestor de Dependencias:** Maven
* **Estrategia de Datos:** DataProvider dinámico con timestamps para usuarios únicos

## 🏗️ Arquitectura del Framework
* `src/main/java/pages/`: Clases Page Object (Locators y acciones aisladas).
* `src/test/java/tests/`: Clases de prueba y aserciones de negocio.
* `src/test/java/helpers/`: Métodos utilitarios (generadores dinámicos y esperas).
* `src/test/resources/`: Datos de prueba externos (JSON/CSV) y suites XML.

## 🚀 Cómo Ejecutar las Pruebas

### Vía Terminal / Maven:
Para ejecutar la suite completa en modo cross-browser:
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
