package prm.project.prm392backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.CreateCategoryRequest;
import prm.project.prm392backend.pojos.Category;
import prm.project.prm392backend.repositories.CategoryRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    public ApiResponse<Category> createCategory(@RequestBody CreateCategoryRequest request) {
        ApiResponse<Category> res = new ApiResponse<>();

        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            res.setCode(400);
            res.setMessage("Category name is required");
            res.setData(null);
            return res;
        }

        Category category = new Category();
        category.setCategoryName(request.getName().trim());
        Category savedCategory = categoryRepository.save(category);

        res.setCode(201); // Created
        res.setMessage("Category created successfully");
        res.setData(savedCategory);
        return res;
    }

    @GetMapping
    public ApiResponse<List<Category>> getAllCategories() {
        ApiResponse<List<Category>> res = new ApiResponse<>();
        List<Category> categories = categoryRepository.findAll();

        res.setCode(200);
        res.setMessage("Fetched categories successfully");
        res.setData(categories);
        return res;
    }

    @GetMapping("/{id}")
    public ApiResponse<Category> getCategoryById(@PathVariable Integer id) {
        ApiResponse<Category> res = new ApiResponse<>();
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            res.setCode(200);
            res.setMessage("Fetched category successfully");
            res.setData(category.get());
        } else {
            res.setCode(404);
            res.setMessage("Category not found with id = " + id);
            res.setData(null);
        }
        return res;
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Integer id,
                                                @RequestBody CreateCategoryRequest newCategory) {
        ApiResponse<Category> res = new ApiResponse<>();

        Optional<Category> existing = categoryRepository.findById(id);
        if (existing.isEmpty()) {
            res.setCode(404);
            res.setMessage("Category not found with id = " + id);
            res.setData(null);
            return res;
        }

        if (newCategory == null || newCategory.getName() == null || newCategory.getName().trim().isEmpty()) {
            res.setCode(400);
            res.setMessage("Category name is required");
            res.setData(null);
            return res;
        }

        Category category = existing.get();
        category.setCategoryName(newCategory.getName().trim());
        Category updated = categoryRepository.save(category);

        res.setCode(200);
        res.setMessage("Category updated successfully");
        res.setData(updated);
        return res;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Integer id) {
        ApiResponse<String> res = new ApiResponse<>();

        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            res.setCode(200);
            res.setMessage("Category deleted successfully");
            res.setData("Deleted category with id = " + id);
        } else {
            res.setCode(404);
            res.setMessage("Category not found with id = " + id);
            res.setData(null);
        }
        return res;
    }
}
