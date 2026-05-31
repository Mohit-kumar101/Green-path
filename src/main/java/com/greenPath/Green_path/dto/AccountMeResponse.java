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
public class AccountMeResponse {

	private String email;

	private Plan plan;

	private String subscriptionStatus;

	private String companyName;

	private long activeLinks;

	private int linksCreatedThisPeriod;

	private int maxCreatesPerMonth;

	private int maxActiveLinks;
}
