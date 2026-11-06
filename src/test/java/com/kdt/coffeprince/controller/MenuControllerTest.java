package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuPageResponse;
import com.kdt.coffeprince.dto.MenuRequest;
import com.kdt.coffeprince.dto.MenuResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Test
    void createMenu() throws Exception {
        MenuResponse response = new MenuResponse(
                2L, 1L, "카페라떼", 5000, "우유가 들어간 커피", true
        );

        when(menuService.createMenu(any(MenuRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "name": "카페라떼",
                                  "price": 5000,
                                  "description": "우유가 들어간 커피"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("카페라떼"))
                .andExpect(jsonPath("$.price").value(5000));

        verify(menuService).createMenu(any(MenuRequest.class));
    }

    @Test
    void updateMenu() throws Exception {
        MenuResponse response = new MenuResponse(
                1L, 1L, "아메리카노", 4500, "가격 수정 테스트", true
        );

        when(menuService.updateMenu(eq(1L), any(MenuRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/menus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "name": "아메리카노",
                                  "price": 4500,
                                  "description": "가격 수정 테스트"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(4500))
                .andExpect(jsonPath("$.description")
                        .value("가격 수정 테스트"));

        verify(menuService).updateMenu(eq(1L), any(MenuRequest.class));
    }

    @Test
    void deleteMenu() throws Exception {
        mockMvc.perform(delete("/api/v1/menus/2"))
                .andExpect(status().isNoContent());

        verify(menuService).deleteMenu(2L);
    }

    @Test
    void getAvailableMenus() throws Exception {
        MenuResponse response = new MenuResponse(
                1L, 1L, "아메리카노", 4500, "기본 커피", true
        );

        when(menuService.available())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/menus/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].available").value(true));

        verify(menuService).available();
    }

    @Test
    void searchMenus() throws Exception {
        MenuResponse response = new MenuResponse(
                1L, 1L, "아메리카노", 4500, "기본 커피", true
        );

        when(menuService.searchMenus("아메리카노"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/menus/search")
                        .param("keyword", "아메리카노"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("아메리카노"))
                .andExpect(jsonPath("$[0].price").value(4500));

        verify(menuService).searchMenus("아메리카노");
    }

    @Test
    void deleteMenu_notFound() throws Exception {
        doThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND))
                .when(menuService).deleteMenu(999L);

        mockMvc.perform(delete("/api/v1/menus/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("메뉴를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        verify(menuService).deleteMenu(999L);
    }

    @Test
    void createMenu_invalidName() throws Exception {
        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "name": "",
                                  "price": 5000,
                                  "description": "테스트 메뉴"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(menuService, never())
                .createMenu(any(MenuRequest.class));
    }

    // A21 판매 여부 변경 — 예외 응답이 명세 형식(code·message·fieldErrors)인지 확인
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
                .andExpect(jsonPath("$.fieldErrors.available").value("true 또는 false로 입력해 주세요."));

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

    @Test
    @DisplayName("페이징: 파라미터가 없으면 page=0, size=10 으로 조회하고 페이지 정보를 반환한다")
    void 페이징_기본값은_page0_size10이다() throws Exception {
        MenuResponse menu = new MenuResponse(1L, 1L, "Columbia Nariñó", 5000, "설명", true);
        when(menuService.list(0, 10))
                .thenReturn(new MenuPageResponse(List.of(menu), 0, 10, 11, 2));

        mockMvc.perform(get("/api/v1/menus/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(menuService).list(0, 10);
    }

    @Test
    @DisplayName("페이징: page=1&size=5 를 그대로 서비스에 전달한다")
    void 페이징_파라미터를_서비스에_전달한다() throws Exception {
        when(menuService.list(1, 5))
                .thenReturn(new MenuPageResponse(List.of(), 1, 5, 11, 3));

        mockMvc.perform(get("/api/v1/menus/page").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(menuService).list(1, 5);
    }

    @Test
    @DisplayName("페이징: page 가 음수·소수·문자면 400 VALIDATION_ERROR 와 fieldErrors.page 를 반환한다")
    void 페이징_page가_잘못되면_400을_반환한다() throws Exception {
        for (String page : List.of("-1", "1.5", "abc", "9999999999")) {
            mockMvc.perform(get("/api/v1/menus/page").param("page", page))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.page").value("0 이상의 정수여야 합니다."));
        }

        verify(menuService, never()).list(anyInt(), anyInt());
    }

    @Test
    @DisplayName("페이징: size 가 0·51·문자면 400 VALIDATION_ERROR 와 fieldErrors.size 를 반환한다")
    void 페이징_size가_범위를_벗어나면_400을_반환한다() throws Exception {
        for (String size : List.of("0", "51", "abc")) {
            mockMvc.perform(get("/api/v1/menus/page").param("size", size))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.size").value("1 이상 50 이하의 정수여야 합니다."));
        }

        verify(menuService, never()).list(anyInt(), anyInt());
    }
}
