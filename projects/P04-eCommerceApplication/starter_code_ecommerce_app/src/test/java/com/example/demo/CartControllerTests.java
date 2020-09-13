package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@Transactional
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CartControllerTests {

    @Autowired
    UserController userController;

    @Autowired
    CartController cartController;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<User> userJson;

    @Autowired
    private JacksonTester<ModifyCartRequest> cartRequestJacksonTester;

    @Autowired
    private JacksonTester<CreateUserRequest> userRequestJacksonTester;

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_VALID_PASSWORD = "testPassword!";

    @Test
    public void testAddItemToCartSuccessful() throws Exception {
        // Create a user
        MockHttpServletResponse createUserResponse = registerUser();

        // Login with that user
        MockHttpServletResponse response = loginWithValidUserAndGetResponse();

        // Use JWT Bearer token to call /addToCart
        String bearerToken = response.getHeader("Authorization");

        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setItemId(1);
        modifyCartRequest.setUsername(TEST_USERNAME);
        modifyCartRequest.setQuantity(4);
        MockHttpServletResponse cartResponse = mvc
                .perform(post(new URI("/api/cart/addToCart"))
                        .content(cartRequestJacksonTester.write(modifyCartRequest).getJson())
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        assertThat(cartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertNotNull(cartResponse.getContentAsString());
    }

    @Test
    public void testRemoveItemFromCartSuccessful() throws Exception {
        registerUser();
        // Login with that user
        MockHttpServletResponse response = loginWithValidUserAndGetResponse();

        // Add 4 items to Cart
        String bearerToken = response.getHeader("Authorization");
        addFourItemsToCart(bearerToken);

        // Remove 2 items from cart
        ModifyCartRequest removeFromCartRequest = new ModifyCartRequest();
        removeFromCartRequest.setItemId(1);
        removeFromCartRequest.setUsername(TEST_USERNAME);
        removeFromCartRequest.setQuantity(2);

        MockHttpServletResponse cartResponse = mvc
                .perform(post(new URI("/api/cart/removeFromCart"))
                        .content(cartRequestJacksonTester.write(removeFromCartRequest).getJson())
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        ObjectMapper mapper = new ObjectMapper();
        List<String> returnedItemsInCart = mapper.readTree(cartResponse.getContentAsString())
                .findValuesAsText("price");
        assertEquals(removeFromCartRequest.getQuantity(), returnedItemsInCart.size());
    }

    private void addFourItemsToCart(String bearerToken) throws Exception {
        ModifyCartRequest addToCartRequest = new ModifyCartRequest();
        addToCartRequest.setItemId(1);
        addToCartRequest.setUsername(TEST_USERNAME);
        addToCartRequest.setQuantity(4);

        // Add 4 items to cart
        MockHttpServletResponse addToCartResponse = mvc
                .perform(post(new URI("/api/cart/addToCart"))
                        .content(cartRequestJacksonTester.write(addToCartRequest).getJson())
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }

    private MockHttpServletResponse registerUser() throws Exception {
        return mvc.perform(post(new URI("/api/user/create"))
                .content(userRequestJacksonTester.write(getCreateUserRequest(TEST_VALID_PASSWORD))
                        .getJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }

    private MockHttpServletResponse loginWithValidUserAndGetResponse() throws Exception {
        return mvc
                .perform(post(new URI("/login"))
                        .content(getValidUserString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }


    private CreateUserRequest getCreateUserRequest(String testValidPassword) {
        CreateUserRequest registerUserRequest = new CreateUserRequest();
        registerUserRequest.setUsername(TEST_USERNAME);
        registerUserRequest.setPassword(TEST_VALID_PASSWORD);
        registerUserRequest.setConfirmPassword(testValidPassword);
        return registerUserRequest;
    }

    private String getValidUserString() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_VALID_PASSWORD);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.USE_ANNOTATIONS);

        String disabledAnnotationsUser = null;
        try {
            disabledAnnotationsUser = mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return disabledAnnotationsUser;
    }
}
