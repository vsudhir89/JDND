package com.udacity.pricing;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.udacity.pricing.api.PricingController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(PricingController.class)
public class PricingServiceUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingController pricingController;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testGetPriceForVehicleId2() throws Exception {
        mockMvc.perform(get("/services/price/?vehicleId=2"))
                .andExpect(status().isOk());

        verify(pricingController, times(1)).get(2L);
    }


}
