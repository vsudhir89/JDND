package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.controllers.UserController;
import com.example.demo.exception.PasswordInvalidException;
import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserControllerTests {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_VALID_PASSWORD = "testPassword!";
    private static final String TEST_INVALID_PASSWORD = "testPassword";

    @Autowired
    private UserController userController;

    @Test
    public void testRegisterUserSuccessful() {
        // Create a user request. Include the right username, password and confirmPassword
        CreateUserRequest registerUserRequest = getCreateUserRequest(TEST_VALID_PASSWORD);

        ResponseEntity<User> responseEntity = userController.createUser(registerUserRequest);
        User registeredUser = responseEntity.getBody();
        assertNotNull(registeredUser);
        assertNotEquals(TEST_VALID_PASSWORD, registeredUser.getPassword());
        assertEquals(TEST_USERNAME, registeredUser.getUsername());
    }

    @Test
    public void testRegisterUserPasswordFail() {
        CreateUserRequest registerUserRequest = getCreateUserRequest(TEST_INVALID_PASSWORD);
        assertThrows(PasswordInvalidException.class, () -> userController.createUser(registerUserRequest));
    }

    @Test
    public void testFindUserByValidUsername() {
        CreateUserRequest registerUserRequest = getCreateUserRequest(TEST_VALID_PASSWORD);
        userController.createUser(registerUserRequest);

        ResponseEntity<User> responseEntity = userController.findByUsername(TEST_USERNAME);
        User savedUser = responseEntity.getBody();
        assertNotNull(savedUser);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testFindUserByInvalidUsername() {
        CreateUserRequest registerUserRequest = getCreateUserRequest(TEST_VALID_PASSWORD);
        userController.createUser(registerUserRequest);

        ResponseEntity<User> responseEntity = userController.findByUsername(TEST_USERNAME + " ");
        User savedUser = responseEntity.getBody();
        assertNull(savedUser);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Disabled
    public void testSuccessfulFindUserById() {
        CreateUserRequest registerUserRequest = getCreateUserRequest(TEST_VALID_PASSWORD);
        ResponseEntity<User> registeredUser = userController.createUser(registerUserRequest);

        ResponseEntity<User> responseEntity = userController.findById(1L);
        User savedUser = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }


    private CreateUserRequest getCreateUserRequest(String testValidPassword) {
        CreateUserRequest registerUserRequest = new CreateUserRequest();
        registerUserRequest.setUsername(TEST_USERNAME);
        registerUserRequest.setPassword(TEST_VALID_PASSWORD);
        registerUserRequest.setConfirmPassword(testValidPassword);
        return registerUserRequest;
    }
}
