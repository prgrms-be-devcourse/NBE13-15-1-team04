package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A04 카테고리 수정 · A05 카테고리 삭제 · A15 카테고리별 메뉴 — 예외 응답이 명세 형식(code·message·fieldErrors)인지 확인
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    // ---------- A04 카테고리 수정 ----------

    @Test
    @DisplayName("A04: 카테고리명이 공백뿐이면 400 VALIDATION_ERROR와 fieldErrors.name을 반환한다")
    void 카테고리명이_공백이면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verify(categoryService, never()).update(anyLong(), any(CategoryRequest.class));
    }

    @Test
    @DisplayName("A04: 카테고리명이 앞뒤 공백 제거 후 31자면 400 VALIDATION_ERROR를 반환한다")
    void 카테고리명이_31자면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  1234567890123456789012345678901  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verify(categoryService, never()).update(anyLong(), any(CategoryRequest.class));
    }

    @Test
    @DisplayName("A04: 카테고리 ID가 0이면 400 VALIDATION_ERROR와 fieldErrors.categoryId를 반환한다")
    void 카테고리_수정_ID가_0이면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/categories/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "디카페인"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.categoryId").exists());

        verify(categoryService, never()).update(anyLong(), any(CategoryRequest.class));
    }

    @Test
    @DisplayName("A04: 없는 카테고리를 수정하면 404 CATEGORY_NOT_FOUND를 반환한다")
    void 없는_카테고리를_수정하면_404를_반환한다() throws Exception {
        when(categoryService.update(eq(999L), any(CategoryRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(put("/api/v1/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "디카페인"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A04: 다른 카테고리와 이름이 같으면 409 CATEGORY_NAME_DUPLICATED를 반환한다")
    void 이미_있는_카테고리명으로_수정하면_409를_반환한다() throws Exception {
        when(categoryService.update(eq(2L), any(CategoryRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED));

        mockMvc.perform(put("/api/v1/categories/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Single Origin"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATED"))
                .andExpect(jsonPath("$.message").value("같은 이름의 카테고리가 이미 있습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A04: JSON 문법이 틀리면 400 VALIDATION_ERROR와 빈 fieldErrors를 반환한다")
    void 카테고리_수정_JSON_문법이_틀리면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    // ---------- A05 카테고리 삭제 ----------

    @Test
    @DisplayName("A05: 메뉴가 등록된 카테고리를 삭제하면 409 CATEGORY_IN_USE를 반환한다")
    void 메뉴가_있는_카테고리를_삭제하면_409를_반환한다() throws Exception {
        doThrow(new BusinessException(ErrorCode.CATEGORY_IN_USE))
                .when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"))
                .andExpect(jsonPath("$.message").value("메뉴가 등록된 카테고리는 삭제할 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A05: 없는 카테고리를 삭제하면 404 CATEGORY_NOT_FOUND를 반환한다")
    void 없는_카테고리를_삭제하면_404를_반환한다() throws Exception {
        doThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND))
                .when(categoryService).delete(999L);

        mockMvc.perform(delete("/api/v1/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A05: 카테고리 ID가 숫자가 아니면 400 VALIDATION_ERROR와 fieldErrors.categoryId를 반환한다")
    void 카테고리_삭제_ID가_숫자가_아니면_400을_반환한다() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.categoryId").exists());

        verify(categoryService, never()).delete(anyLong());
    }

    // ---------- A15 카테고리별 메뉴 ----------

    @Test
    @DisplayName("A15: 없는 카테고리의 메뉴를 조회하면 404 CATEGORY_NOT_FOUND를 반환한다")
    void 없는_카테고리의_메뉴를_조회하면_404를_반환한다() throws Exception {
        when(categoryService.findMenus(999L))
                .thenThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(get("/api/v1/categories/999/menus"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("A15: 카테고리 ID가 0이면 400 VALIDATION_ERROR와 fieldErrors.categoryId를 반환한다")
    void 카테고리별_메뉴_ID가_0이면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/categories/0/menus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.categoryId").exists());

        verify(categoryService, never()).findMenus(anyLong());
    }
}
