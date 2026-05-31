package com.greenPath.Green_path.dto;

import com.greenPath.Green_path.domain.LinkApprovalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLinkResponse {

	private String id;

	private String shortCode;

	private String shortUrl;

	private String managementSecret;

	private String qrUrl;

	private String statsUrl;

	private LinkApprovalStatus linkApprovalStatus;

	private boolean platformRoutingEnabled;
}
