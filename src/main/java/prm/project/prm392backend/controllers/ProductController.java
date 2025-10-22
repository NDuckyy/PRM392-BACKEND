package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.CreateOrUpdateProductRequest;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.Category;
import prm.project.prm392backend.pojos.Product;
import prm.project.prm392backend.pojos.Provider;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.CategoryRepository;
import prm.project.prm392backend.repositories.ProductRepository;
import prm.project.prm392backend.repositories.ProviderRepository;
import prm.project.prm392backend.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private  ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody CreateOrUpdateProductRequest request,
                                                              @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.AUTH_MISSING);
        }
        String token = authHeader.substring(7).trim();

        if (!JwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.AUTH_INVALID);
        }

        Integer userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            throw new AppException(ErrorCode.TOKEN_NO_USERID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request == null || request.getCategoryId() == null) {
            throw new AppException(ErrorCode.CATEGORY_ID_REQUIRED);
        }
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NAME_REQUIRED);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Provider provider = providerRepository.findByUser(user);
        if(provider == null){
            throw new AppException(ErrorCode.PROVIDER_NOT_FOUND);
        }

        Product product = new Product();
        product.setProductName(request.getProductName().trim());
        product.setBriefDescription(request.getBriefDescription());
        product.setFullDescription(request.getFullDescription());
        product.setTechnicalSpecifications(request.getTechnicalSpecifications());
        product.setPrice(request.getPrice());
        product.setImageURL(request.getImageURL());
        product.setCategoryID(category);
        product.setStockQuantity(request.getStockQuantity());
        product.setProvider(provider);

        Product saved = productRepository.save(product);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(saved, "Product created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(products, "Fetched products successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.ok(product, "Fetched product successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Integer id,
            @RequestBody CreateOrUpdateProductRequest request) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request == null || request.getCategoryId() == null) {
            throw new AppException(ErrorCode.CATEGORY_ID_REQUIRED);
        }
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NAME_REQUIRED);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));


        existing.setProductName(request.getProductName().trim());
        existing.setBriefDescription(request.getBriefDescription());
        existing.setFullDescription(request.getFullDescription());
        existing.setTechnicalSpecifications(request.getTechnicalSpecifications());
        existing.setPrice(request.getPrice());
        existing.setImageURL(request.getImageURL());
        existing.setStockQuantity(request.getStockQuantity());
        existing.setCategoryID(category);

        Product updated = productRepository.save(existing);

        return ResponseEntity.ok(ApiResponse.ok(updated, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
        return ResponseEntity.ok(ApiResponse.ok(
                "Deleted product with id = " + id,
                "Product deleted successfully"));
    }
}
