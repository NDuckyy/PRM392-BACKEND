package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.pojos.Provider;
import prm.project.prm392backend.pojos.StoreLocation;
import prm.project.prm392backend.repositories.ProviderRepository;
import prm.project.prm392backend.repositories.StoreLocationRepository;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final StoreLocationRepository storeLocationRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @GetMapping("/{providerName}")
    public ResponseEntity<?> getLocationByProviderId(@PathVariable String providerName) {
        Provider provider = providerRepository.findByProviderName(providerName);
        StoreLocation storeLocation = storeLocationRepository.findByProvider((provider));
        if(storeLocation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(storeLocation);
    }
}
