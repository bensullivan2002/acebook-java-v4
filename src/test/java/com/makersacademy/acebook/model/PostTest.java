package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import com.github.javafaker.Faker;
import com.makersacademy.acebook.Application;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.PostRepository;
import org.flywaydb.core.Flyway;
import com.makersacademy.acebook.repository.UserRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostTest {

	private Post post = new Post("hello");

	@LocalServerPort
	private int port;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private UserRepository userRepository;

	static WebDriver driver;
	static Faker faker;

	@Before
	public void setup() {
		System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
		driver = new ChromeDriver();
		faker = new Faker();
	}

	@After
	public void tearDown() {
		driver.close();
	}


	public WebDriver getDriver() {
		return driver;
	}

	public void login() {
		driver.get("http://localhost:" + port + "/login");
		// Login
		driver.findElement(By.id("username")).sendKeys("testing");
		driver.findElement(By.id("password")).sendKeys("Password123!");
		driver.findElement(By.tagName("button")).click();
	}

	@Test
	public void signInViewPost() {
		login();
		List<WebElement> element = driver.findElements(By.className("post"));
		System.out.println(element);
		WebElement element1 = element.get(element.size() - 1);
		Assert.assertEquals("John\nThis is my first post!\nGreat post!\nLikes: 15\nComment", element1.getText());
	}

	@Test
	public void signInCreatePostCheckLikesIs0() {
		login();
		// Create a new post
		driver.findElement(By.id("content")).sendKeys("post test");
		driver.findElement(By.id("content_create")).click();

		List<WebElement> element = driver.findElements(By.className("post"));
		WebElement element1 = element.get(0);
		Assert.assertEquals("Mariam\npost test\nLikes: 0\nComment", element1.getText());
	}
	//
	@Test
	public void signInLikeTestPost() {
		login();
//		Find the id of the test post (Last Post)
		Post find = postRepository.findTopByOrderByIdDesc();
		Long id = find.getId();

//		Finding the like button for the test post and clicking
		WebElement like_element = driver.findElement(By.id(String.format("like_button%s", id)));
		like_element.click();

		List<WebElement> post_element = driver.findElements(By.className("post"));
		WebElement element1 = post_element.get(0);

		long time = 3L;
		Wait<WebDriver> wait = new WebDriverWait(driver, time);
		WebElement finalElement = element1;
		wait.until(d -> finalElement.isDisplayed());

//		Find the test post and asserts that the like count is 1
        post_element = driver.findElements(By.className("post"));
        element1 = post_element.get(0);

		Assert.assertEquals("Mariam\npost test\nLikes: 1\nComment", element1.getText());
		postRepository.deleteTestPost();
	}

	@Test
	public void signInCreatePostWithImage() {
		// Create a new post
		Post post = new Post();
		post.setContent("post test");
		String expectedUrl = "https://res.cloudinary.com/dk3vxa56n/image/upload/c_limit,h_60,w_90/v1717424673/o2zfyet3gtglmdmszi4r.png";
		post.setImg_url(expectedUrl);
		User user = userRepository.findByUsername("johndoe");
		post.setUser(user);
		postRepository.save(post);
		login();
//      Finds all posts then finds the most recent post
		List<WebElement> post_elements = driver.findElements(By.className("post"));
		WebElement firstPostElement = post_elements.get(0);
		// Verify the image URL
		WebElement postImageElement = firstPostElement.findElement(By.id("postImage"));
		String actualUrl = postImageElement.getAttribute("src");
		Assert.assertEquals(expectedUrl, actualUrl);
		postRepository.deleteTestPost();
	}

	@Test
	public void postHasContent() {
		assertThat(post.getContent(), containsString("hello"));
	}
}



