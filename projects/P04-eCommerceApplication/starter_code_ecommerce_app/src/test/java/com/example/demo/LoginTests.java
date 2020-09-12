package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import java.net.URI;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Transactional
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
public class LoginTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<User> userJson;

    @Autowired
    private JacksonTester<CreateUserRequest> createUserRequestJacksonTester;

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_VALID_PASSWORD = "testPassword!";

    @Test
    public void testLoginFailedWithoutRegisteredUser() throws Exception {
        MockHttpServletResponse response = mvc
                .perform(post(new URI("/login"))
                        .content(userJson.write(getValidUser()).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void testLoginResponseContainsBearerToken() throws Exception {
        MockHttpServletResponse mockRegisterUserResponse = mvc
                .perform(post(new URI("/api/user/create"))
                        .content(createUserRequestJacksonTester.write(getCreateUserRequest())
                                .getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertThat(mockRegisterUserResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        MockHttpServletResponse response = mvc
                .perform(post(new URI("/login"))
                        .content(userJson.write(getValidUser()).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Authorization")).contains("Bearer ");
    }

    private CreateUserRequest getCreateUserRequest() {
        CreateUserRequest registerUserRequest = new CreateUserRequest();
        registerUserRequest.setUsername(TEST_USERNAME);
        registerUserRequest.setPassword(TEST_VALID_PASSWORD);
        registerUserRequest.setConfirmPassword(LoginTests.TEST_VALID_PASSWORD);
        return registerUserRequest;
    }

    private User getValidUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_VALID_PASSWORD);
        return user;
    }
}
