package com.greenPath.Green_path.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("link_clicks")
public class ClickEvent {

	@Id
	private String id;

	@Indexed
	private String shortCode;

	private Instant clickedAt;

	private String userAgent;

	private String referer;

	/** SHA-256 hex of client IP + server salt for privacy-friendly analytics. */
	private String ipFingerprint;
}
