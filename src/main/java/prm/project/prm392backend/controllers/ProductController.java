package prm.project.prm392backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> createProduct(@RequestBody CreateOrUpdateProductRequest request) {
        Optional<Category> category = categoryRepository.findById(request.getCategoryId());
        if (category.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Category not found with id = " + request.getCategoryId());
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
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found with id = " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody CreateOrUpdateProductRequest request) {
        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found with id = " + id);
        }

        Optional<Category> category = categoryRepository.findById(request.getCategoryId());
        if (category.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Category not found with id = " + request.getCategoryId());
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
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found with id = " + id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Deleted product with id = " + id);
    }
}