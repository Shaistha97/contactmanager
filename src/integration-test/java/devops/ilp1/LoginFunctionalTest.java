package devops.ilp1;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.openqa.selenium.firefox.FirefoxOptions;

import devops.ilp1.IntegrationTest;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class LoginFunctionalTest {

	static WebDriver driver;
	
	@BeforeClass
	public static void setup() {
	
		FirefoxBinary firefoxBinary = new FirefoxBinary();
		firefoxBinary.addCommandLineOptions("--headless");
		System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");
		FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setBinary(firefoxBinary);
        
        driver = new FirefoxDriver(firefoxOptions);
	}
	
	@AfterClass
	public static void cleanUp() {
		driver.quit();
	}
	
	@Test
	public void loginSuccess() {
		driver.get("http://localhost:5052/capstone/signin");
		WebElement email = driver.findElement(By.name("username"));
        WebElement pass = driver.findElement(By.name("password"));
        WebElement button = driver.findElement(By.xpath("/html/body/section/div/div/div/div/div/form/div[3]/div/input"));      
        email.sendKeys("fans3097@gmail.com");
        pass.sendKeys("shaistha");
        button.click();
        assertTrue(driver.getPageSource().contains("Start Adding Your Contacts!"));
	}
	
	@Test
	public void forgotPasswordSuccess() {
        driver.get("http://localhost:5052/capstone/forgot");      
        WebElement email = driver.findElement(By.name("email"));
        WebElement button = driver.findElement(By.xpath("/html/body/section/div/div/div/div/div/form/div[2]/button"));      
        email.sendKeys("fans3097@gmail.com");
        button.click();
        assertTrue(driver.getPageSource().contains("We have successfully sent OTP to your email!"));
	}
}
