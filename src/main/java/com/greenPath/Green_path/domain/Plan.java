package com.greenPath.Green_path.domain;

import lombok.Getter;

@Getter
public enum Plan {
	FREE("Free", 10, 40),
	STARTER("Starter", 200, 5_000),
	PRO("Pro", 10_000, 500_000);

	private final String displayName;
	private final int maxActiveLinks;
	private final int maxCreatesPerMonth;

	Plan(String displayName, int maxActiveLinks, int maxCreatesPerMonth) {
		this.displayName = displayName;
		this.maxActiveLinks = maxActiveLinks;
		this.maxCreatesPerMonth = maxCreatesPerMonth;
	}
}
