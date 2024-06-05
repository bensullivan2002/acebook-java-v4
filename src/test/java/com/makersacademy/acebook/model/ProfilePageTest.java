package com.makersacademy.acebook.model;

import static org.junit.Assert.assertEquals;

import java.util.List;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.PostRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.github.javafaker.Faker;
import com.makersacademy.acebook.Application;
import com.makersacademy.acebook.repository.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProfilePageTest {

    static WebDriver driver;
    static Faker faker;

    @LocalServerPort
    private int port;

    private String page = String.format("http://localhost:%s", port);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void pageTest() {
        System.out.println(page);
        System.out.println(port);
    }

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

    public void login() {
        driver.get("http://localhost:" +port+ "/users/my-profile");
        // Login
        driver.findElement(By.id("username")).sendKeys("testing");
        driver.findElement(By.id("password")).sendKeys("Password123!");
        driver.findElement(By.tagName("button")).click();
    }

    @Test
    public void profilePageDisplaysUserInfo() {
        login();

        User user = userRepository.findByUsername("testing");

        String firstname = user.getFirstname();
        String lastname = user.getLastname();
        String bio = user.getBio();

        System.out.println(firstname);


        WebElement firstNameElement = driver.findElement(By.id("firstname"));
        WebElement lastNameElement = driver.findElement(By.id("lastname"));
        WebElement bioElement = driver.findElement(By.id("bio"));

        assertEquals(firstname, firstNameElement.getText());
        assertEquals(lastname, lastNameElement.getText());
        assertEquals(bio, bioElement.getText());
    }
    @Test
    public void profilePageDisplaysUserPosts() {
        login();

        String newPostContent = "This is a testing post!";
        WebElement postInput = driver.findElement(By.id("content"));
        WebElement addPostButton = driver.findElement(By.id("content_create"));
        postInput.sendKeys(newPostContent);
        addPostButton.click();

        List<WebElement> posts = driver.findElements(By.id("post-content"));
        String expectedPost = "This is a testing post!";

        for (int i = 0; i < posts.size(); i++) {
            assertEquals(expectedPost, posts.get(i).getText());
        }
    }

    @Test
    public void profilePageAllowsAddingNewPost() {
        login();

        String newPostContent = "This is a testing post!";
        WebElement postInput = driver.findElement(By.id("content"));
        WebElement addPostButton = driver.findElement(By.id("content_create"));
        postInput.sendKeys(newPostContent);
        addPostButton.click();
        List<WebElement> posts = driver.findElements(By.id("post-content"));
        WebElement latestPost = posts.get(0);
        assertEquals(newPostContent, latestPost.getText());
        postRepository.deleteTestPost();
    }

    @Test
    public void ProfileLikeTestPost() {
        login();

        String newPostContent = "This is a testing post!";
        WebElement postInput = driver.findElement(By.id("content"));
        WebElement addPostButton = driver.findElement(By.id("content_create"));
        postInput.sendKeys(newPostContent);
        addPostButton.click();

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

        Assert.assertEquals("This is a testing post!\nLikes: 1\nLike\nComment", element1.getText());
        postRepository.deleteTestPost();
    }


    @Test
    public void ProfileCreateComment() {
        login();

        // Create a new post
        driver.findElement(By.id("content")).sendKeys("post test");
        driver.findElement(By.id("content_create")).click();

        Post find = postRepository.findTopByOrderByIdDesc();
        Long id = find.getId();

        // Comment Button is clicked in order for the comment-input field to appear
        driver.findElement(By.id(String.format("comment_button%d", id))).click();
        WebElement comment_element = driver.findElement(By.id(String.format("comment-input%s", id)));
        comment_element.sendKeys("comment test");

        driver.findElement(By.id(String.format("submit_button%s", id))).click();

        List<WebElement> post_element = driver.findElements(By.className("post"));
        WebElement element1 = post_element.get(0);

        WebElement comment = element1.findElement(By.className("comment"));

        Assert.assertEquals("comment test", comment.getText());
        commentRepository.deleteTestComment();
        postRepository.deleteTestPost();
    }

}
