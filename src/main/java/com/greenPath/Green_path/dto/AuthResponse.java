package com.greenPath.Green_path.dto;

import com.greenPath.Green_path.domain.Plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

	private String apiKey;

	private String email;

	private Plan plan;

	private String subscriptionStatus;

	private String message;
}
