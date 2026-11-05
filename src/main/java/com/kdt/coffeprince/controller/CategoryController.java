package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.dto.CategoryUpdateRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping                                   // A01
    public List<CategoryResponse> list() {
        return categoryService.findAll();
    }

    @GetMapping("/{categoryId}")                  // A02
    public CategoryResponse get(@PathVariable @Positive Long categoryId) {
        return categoryService.find(categoryId);
    }

    @GetMapping("/{categoryId}/menus")            // A15
    public List<MenuResponse> menus(@PathVariable @Positive Long categoryId) {
        return categoryService.findMenus(categoryId);
    }

    @PostMapping                                  // A03
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{categoryId}")                  // A04
    public CategoryResponse update(@PathVariable @Positive Long categoryId,
                                   @Valid @RequestBody CategoryUpdateRequest request) {
        return categoryService.update(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")               // A05
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long categoryId) {
        categoryService.delete(categoryId);
    }
}
