package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A14 배송 묶음 — 날짜 누락·형식 오류가 명세 형식(VALIDATION_ERROR + fieldErrors.shippingDate)인지 확인
@WebMvcTest(DeliveryBatchesController.class)
class DeliveryBatchesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("A14: shippingDate로 조회하면 200을 반환한다")
    void 배송일로_조회하면_200을_반환한다() throws Exception {
        when(orderService.findDeliveryBatches(LocalDate.of(2026, 11, 3))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("A14: 날짜가 없으면 400 VALIDATION_ERROR와 fieldErrors.shippingDate를 반환한다")
    void 날짜가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/delivery-batches"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").exists());

        verify(orderService, never()).findDeliveryBatches(any());
    }

    @Test
    @DisplayName("A14: 날짜 형식이 틀리면 400 VALIDATION_ERROR와 fieldErrors.shippingDate를 반환한다")
    void 날짜_형식이_틀리면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "2026-13-45"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").exists());

        verify(orderService, never()).findDeliveryBatches(any());
    }
}
