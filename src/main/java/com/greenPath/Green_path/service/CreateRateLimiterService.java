package com.greenPath.Green_path.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CreateRateLimiterService {

	private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

	@Value("${app.rate-limit.creates-per-hour:120}")
	private int maxCreatesPerHour;

	public void check(String apiKey) {
		long now = System.currentTimeMillis();
		long windowMs = 3_600_000L;
		Deque<Long> q = hits.computeIfAbsent(apiKey, k -> new ArrayDeque<>());
		synchronized (q) {
			while (!q.isEmpty() && now - q.peekFirst() > windowMs) {
				q.pollFirst();
			}
			if (q.size() >= maxCreatesPerHour) {
				throw new IllegalArgumentException("Too many creates this hour. Try again later or upgrade your plan.");
			}
			q.addLast(now);
		}
	}
}
