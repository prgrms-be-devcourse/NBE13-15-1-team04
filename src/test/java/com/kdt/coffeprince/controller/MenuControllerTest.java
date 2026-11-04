package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CreateMenuRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.kdt.coffeprince.exception.NotFoundException;
import com.kdt.coffeprince.exception.ApiExceptionAdvice;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@Import(ApiExceptionAdvice.class)
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

        when(menuService.createMenu(any(CreateMenuRequest.class)))
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

        verify(menuService).createMenu(any(CreateMenuRequest.class));
    }

    @Test
    void updateMenu() throws Exception {
        MenuResponse response = new MenuResponse(
                1L, 1L, "아메리카노", 4500, "가격 수정 테스트", true
        );

        when(menuService.updateMenu(eq(1L), any(PutMenuRequest.class)))
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

        verify(menuService).updateMenu(eq(1L), any(PutMenuRequest.class));
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
                        .param("name", "아메리카노"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("아메리카노"))
                .andExpect(jsonPath("$[0].price").value(4500));

        verify(menuService).searchMenus("아메리카노");
    }

    @Test
    void deleteMenu_notFound() throws Exception {
        doThrow(new NotFoundException("메뉴를 찾을 수 없습니다."))
                .when(menuService).deleteMenu(999L);

        mockMvc.perform(delete("/api/v1/menus/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("메뉴를 찾을 수 없습니다."));

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
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        verify(menuService, never())
                .createMenu(any(CreateMenuRequest.class));
    }

}

