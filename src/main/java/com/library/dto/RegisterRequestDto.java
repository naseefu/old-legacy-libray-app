package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequestDto {
    @NotBlank @Size(min = 3, max = 50) public String username;
    @NotBlank @Email public String email;
    @NotBlank @Size(min = 8) public String password;
    public String fullName;
}
