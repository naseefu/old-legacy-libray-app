package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;  // javax → jakarta in Boot 3

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequestDto {
    @NotBlank @Size(min = 3, max = 50) public String username;
    @NotBlank @Email public String email;
    @NotBlank @Size(min = 8) public String password;
    public String fullName;
}
