package prm.project.prm392backend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String email;
    private String phoneNumber;
    private String address;

}
