package com.greenPath.Green_path.dto;

import java.util.List;

import com.greenPath.Green_path.domain.Plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanCatalogResponse {

	private List<PlanRow> plans;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlanRow {
		private Plan id;
		private String name;
		private int maxActiveLinks;
		private int maxCreatesPerMonth;
		private String suggestedMonthlyPriceUsd;
	}
}
