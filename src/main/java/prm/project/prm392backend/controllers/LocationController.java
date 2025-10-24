package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.pojos.StoreLocation;
import prm.project.prm392backend.repositories.StoreLocationRepository;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final StoreLocationRepository storeLocationRepository;

    @GetMapping("/{providerId}")
    public ResponseEntity<?> getLocationByProviderId(@PathVariable Integer providerId) {
        return storeLocationRepository.findByProvider_Id(providerId)
                .map(location -> ResponseEntity.ok(location))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
