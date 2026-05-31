package com.greenPath.Green_path.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 8, max = 72)
	private String password;

	@Size(max = 120)
	private String companyName;
}
