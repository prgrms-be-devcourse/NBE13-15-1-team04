package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.ReorderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A23 재주문 — 예외 응답이 명세 형식(code·message·fieldErrors)인지 확인
@WebMvcTest(ReorderController.class)
class ReorderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReorderService reorderService;

    @Test
    @DisplayName("A23: 없는 주문을 재주문하면 404 ORDER_NOT_FOUND를 반환한다")
    void 없는_주문을_재주문하면_404를_반환한다() throws Exception {
        when(reorderService.reorder(999L))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(post("/api/v1/orders/999/reorder"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A23: 판매 중지된 메뉴가 포함되면 409 MENU_UNAVAILABLE을 반환한다")
    void 판매중지_메뉴가_있으면_409를_반환한다() throws Exception {
        when(reorderService.reorder(1L))
                .thenThrow(new BusinessException(ErrorCode.MENU_UNAVAILABLE));

        mockMvc.perform(post("/api/v1/orders/1/reorder"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MENU_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("판매 중지된 메뉴가 포함되어 있습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A23: 같은 이메일·배송일의 기존 주문과 배송지가 다르면 409 ADDRESS_MISMATCH를 반환한다")
    void 기존_묶음과_배송지가_다르면_409를_반환한다() throws Exception {
        when(reorderService.reorder(2L))
                .thenThrow(new BusinessException(ErrorCode.ADDRESS_MISMATCH));

        mockMvc.perform(post("/api/v1/orders/2/reorder"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADDRESS_MISMATCH"))
                .andExpect(jsonPath("$.message").value("같은 이메일과 배송일의 기존 주문과 배송지가 다릅니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A23: 주문 ID가 0이면 400 VALIDATION_ERROR와 fieldErrors.orderId를 반환한다")
    void 재주문_ID가_0이면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/orders/0/reorder"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.orderId").exists());

        verify(reorderService, never()).reorder(anyLong());
    }
}
