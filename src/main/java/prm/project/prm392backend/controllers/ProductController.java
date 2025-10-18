package prm.project.prm392backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.CreateOrUpdateProductRequest;
import prm.project.prm392backend.pojos.Category;
import prm.project.prm392backend.pojos.Product;
import prm.project.prm392backend.repositories.CategoryRepository;
import prm.project.prm392backend.repositories.ProductRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody CreateOrUpdateProductRequest request) {
        ApiResponse<Product> res = new ApiResponse<>();

        // Validate cơ bản
        if (request == null || request.getCategoryId() == null) {
            res.setCode(400);
            res.setMessage("categoryId is required");
            res.setData(null);
            return res;
        }
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            res.setCode(400);
            res.setMessage("productName is required");
            res.setData(null);
            return res;
        }

        Optional<Category> category = categoryRepository.findById(request.getCategoryId());
        if (category.isEmpty()) {
            res.setCode(400);
            res.setMessage("Category not found with id = " + request.getCategoryId());
            res.setData(null);
            return res;
        }

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setBriefDescription(request.getBriefDescription());
        product.setFullDescription(request.getFullDescription());
        product.setTechnicalSpecifications(request.getTechnicalSpecifications());
        product.setPrice(request.getPrice());
        product.setImageURL(request.getImageURL());
        product.setCategoryID(category.get());

        Product saved = productRepository.save(product);

        res.setCode(201);
        res.setMessage("Product created successfully");
        res.setData(saved);
        return res;
    }

    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        ApiResponse<List<Product>> res = new ApiResponse<>();
        List<Product> products = productRepository.findAll();

        res.setCode(200);
        res.setMessage("Fetched products successfully");
        res.setData(products);
        return res;
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getProductById(@PathVariable Integer id) {
        ApiResponse<Product> res = new ApiResponse<>();
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            res.setCode(200);
            res.setMessage("Fetched product successfully");
            res.setData(product.get());
        } else {
            res.setCode(404);
            res.setMessage("Product not found with id = " + id);
            res.setData(null);
        }
        return res;
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Integer id,
                                              @RequestBody CreateOrUpdateProductRequest request) {
        ApiResponse<Product> res = new ApiResponse<>();

        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            res.setCode(404);
            res.setMessage("Product not found with id = " + id);
            res.setData(null);
            return res;
        }

        if (request == null || request.getCategoryId() == null) {
            res.setCode(400);
            res.setMessage("categoryId is required");
            res.setData(null);
            return res;
        }
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            res.setCode(400);
            res.setMessage("productName is required");
            res.setData(null);
            return res;
        }

        Optional<Category> category = categoryRepository.findById(request.getCategoryId());
        if (category.isEmpty()) {
            res.setCode(400);
            res.setMessage("Category not found with id = " + request.getCategoryId());
            res.setData(null);
            return res;
        }

        Product product = existing.get();
        product.setProductName(request.getProductName());
        product.setBriefDescription(request.getBriefDescription());
        product.setFullDescription(request.getFullDescription());
        product.setTechnicalSpecifications(request.getTechnicalSpecifications());
        product.setPrice(request.getPrice());
        product.setImageURL(request.getImageURL());
        product.setCategoryID(category.get());

        Product updated = productRepository.save(product);

        res.setCode(200);
        res.setMessage("Product updated successfully");
        res.setData(updated);
        return res;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable Integer id) {
        ApiResponse<String> res = new ApiResponse<>();

        if (!productRepository.existsById(id)) {
            res.setCode(404);
            res.setMessage("Product not found with id = " + id);
            res.setData(null);
            return res;
        }

        productRepository.deleteById(id);

        res.setCode(200);
        res.setMessage("Product deleted successfully");
        res.setData("Deleted product with id = " + id);
        return res;
    }
}
