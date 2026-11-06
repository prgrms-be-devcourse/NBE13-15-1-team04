package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A16 주문 취소 · A22 주문 메모 수정 · A23 재주문 — 예외 응답이 명세 형식(code·message·fieldErrors)인지 확인
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    // ---------- A16 주문 취소 ----------

    @Test
    @DisplayName("A16: 배송일 14시 마감이 지난 주문을 취소하면 409 ORDER_NOT_CANCELLABLE을 반환한다")
    void 마감이_지난_주문을_취소하면_409를_반환한다() throws Exception {
        when(orderService.cancel(24L))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE));

        mockMvc.perform(post("/api/v1/orders/24/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_CANCELLABLE"))
                .andExpect(jsonPath("$.message").value("배송 대상일 14시 전의 접수 상태 주문만 취소할 수 있습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A16: 없는 주문을 취소하면 404 ORDER_NOT_FOUND를 반환한다")
    void 없는_주문을_취소하면_404를_반환한다() throws Exception {
        when(orderService.cancel(999L))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(post("/api/v1/orders/999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A16: 주문 ID가 0이면 400 VALIDATION_ERROR와 fieldErrors.orderId를 반환한다")
    void 주문_취소_ID가_0이면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/orders/0/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.orderId").exists());

        verify(orderService, never()).cancel(anyLong());
    }

    // ---------- A22 주문 메모 수정 ----------

    @Test
    @DisplayName("A22: 메모가 문자열이 아니면 400 VALIDATION_ERROR와 fieldErrors.memo를 반환한다")
    void 메모가_숫자면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/orders/1/memo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo": 123}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.memo").value("메모는 문자열로 입력해 주세요."));

        verify(orderService, never()).changeMemo(anyLong(), anyString());
    }

    @Test
    @DisplayName("A22: 메모 필드가 없으면 400 VALIDATION_ERROR와 fieldErrors.memo를 반환한다")
    void 메모가_누락되면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/orders/1/memo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.memo").exists());

        verify(orderService, never()).changeMemo(anyLong(), anyString());
    }

    @Test
    @DisplayName("A22: 메모가 201자면 400 VALIDATION_ERROR와 fieldErrors.memo를 반환한다")
    void 메모가_201자면_400을_반환한다() throws Exception {
        String memo201 = "a".repeat(201);

        mockMvc.perform(put("/api/v1/orders/1/memo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memo\": \"" + memo201 + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.memo").value("메모는 200자 이하로 입력해 주세요."));

        verify(orderService, never()).changeMemo(anyLong(), anyString());
    }

    @Test
    @DisplayName("A22: 없는 주문의 메모를 수정하면 404 ORDER_NOT_FOUND를 반환한다")
    void 없는_주문의_메모를_수정하면_404를_반환한다() throws Exception {
        when(orderService.changeMemo(eq(999L), anyString()))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(put("/api/v1/orders/999/memo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo": "주문 확인 완료"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A22: JSON 문법이 틀리면 400 VALIDATION_ERROR와 빈 fieldErrors를 반환한다")
    void 메모_수정_JSON_문법이_틀리면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/orders/1/memo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memo\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    // ---------- A23 재주문 ----------

    @Test
    @DisplayName("A23: 없는 주문을 재주문하면 404 ORDER_NOT_FOUND를 반환한다")
    void 없는_주문을_재주문하면_404를_반환한다() throws Exception {
        when(orderService.reorder(999L))
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
        when(orderService.reorder(1L))
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
        when(orderService.reorder(2L))
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

        verify(orderService, never()).reorder(anyLong());
    }
}
