package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.pojos.Provider;
import prm.project.prm392backend.pojos.StoreLocation;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.ProviderRepository;
import prm.project.prm392backend.repositories.StoreLocationRepository;
import prm.project.prm392backend.repositories.UserRepository;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final StoreLocationRepository storeLocationRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{username}")
    public ResponseEntity<?> getLocationByProviderId(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Provider provider = providerRepository.findByUser(user);
        StoreLocation storeLocation = storeLocationRepository.findByProvider((provider));
        if(storeLocation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(storeLocation);
    }
}
