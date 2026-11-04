package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A21 판매 여부 변경 — 예외 응답이 명세 형식(code·message·fieldErrors)인지 확인
// MenuControllerTest(한수빈 담당)와 분리해서 A21만 따로 둔다
@WebMvcTest(MenuController.class)
class MenuAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Test
    @DisplayName("A21: available이 문자열 \"false\"면 400 VALIDATION_ERROR와 fieldErrors.available을 반환한다")
    void 판매여부가_문자열이면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/menus/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": "false"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.available").value("판매 여부는 true 또는 false로 입력해 주세요."));

        verify(menuService, never()).changeAvailability(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("A21: available이 null이면 400 VALIDATION_ERROR와 fieldErrors.available을 반환한다")
    void 판매여부가_null이면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/menus/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.available").exists());

        verify(menuService, never()).changeAvailability(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("A21: 없는 메뉴의 판매 여부를 바꾸면 404 MENU_NOT_FOUND를 반환한다")
    void 없는_메뉴의_판매여부를_바꾸면_404를_반환한다() throws Exception {
        when(menuService.changeAvailability(999L, false))
                .thenThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND));

        mockMvc.perform(put("/api/v1/menus/999/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": false}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("메뉴를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}
