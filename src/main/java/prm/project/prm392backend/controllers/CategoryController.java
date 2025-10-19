package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.CreateCategoryRequest;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.Category;
import prm.project.prm392backend.repositories.CategoryRepository;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody CreateCategoryRequest request) {
        String name = (request == null ? null : request.getName());
        if (name == null || name.trim().isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_NAME_REQUIRED);
        }
        String normalized = name.trim();

        // 409 Conflict nếu trùng tên
        if (categoryRepository.existsByCategoryNameIgnoreCase(normalized)) {
            throw new AppException(ErrorCode.CATEGORY_CONFLICT);
        }

        Category category = new Category();
        category.setCategoryName(normalized);
        Category saved = categoryRepository.save(category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(saved, "Category created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(categories, "Fetched categories successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.ok(category, "Fetched category successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Integer id,
            @RequestBody CreateCategoryRequest newCategory) {

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        String name = (newCategory == null ? null : newCategory.getName());
        if (name == null || name.trim().isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_NAME_REQUIRED);
        }
        String normalized = name.trim();

        // Nếu đổi tên sang tên đã tồn tại của category khác → 409
        boolean nameTaken = categoryRepository.existsByCategoryNameIgnoreCase(normalized)
                && !normalized.equalsIgnoreCase(existing.getCategoryName());
        if (nameTaken) {
            throw new AppException(ErrorCode.CATEGORY_CONFLICT);
        }

        existing.setCategoryName(normalized);
        Category updated = categoryRepository.save(existing);

        return ResponseEntity.ok(ApiResponse.ok(updated, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
        return ResponseEntity.ok(ApiResponse.ok("Deleted category with id = " + id, "Category deleted successfully"));
    }
}
