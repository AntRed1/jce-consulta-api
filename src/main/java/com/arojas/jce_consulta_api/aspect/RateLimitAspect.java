/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.aspect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.arojas.jce_consulta_api.exception.query.CedulaQueryExceptions;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Aspecto para implementar rate limiting
 */

@Aspect
@Component
@Slf4j
public class RateLimitAspect {

	private final ConcurrentHashMap<String, UserRequestTracker> userTrackers = new ConcurrentHashMap<>();

	@Around("@annotation(rateLimit)")
	public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return joinPoint.proceed();
		}

		String userEmail = authentication.getName();
		UserRequestTracker tracker = userTrackers.computeIfAbsent(userEmail,
				k -> new UserRequestTracker(rateLimit.requests(), rateLimit.timeWindowSeconds()));

		if (!tracker.allowRequest()) {
			log.warn("Rate limit exceeded for user: {}", userEmail);
			throw CedulaQueryExceptions.limitExceeded("últimos " + rateLimit.timeWindowSeconds() + " segundos",
					rateLimit.requests());
		}

		return joinPoint.proceed();
	}

	private static class UserRequestTracker {
		private final int maxRequests;
		private final long timeWindowMs;
		private final AtomicInteger requestCount = new AtomicInteger(0);
		private volatile long windowStart = System.currentTimeMillis();

		public UserRequestTracker(int maxRequests, int timeWindowSeconds) {
			this.maxRequests = maxRequests;
			this.timeWindowMs = timeWindowSeconds * 1000L;
		}

		public boolean allowRequest() {
			long now = System.currentTimeMillis();

			// Reset window if expired
			if (now - windowStart > timeWindowMs) {
				synchronized (this) {
					if (now - windowStart > timeWindowMs) {
						windowStart = now;
						requestCount.set(0);
					}
				}
			}

			return requestCount.incrementAndGet() <= maxRequests;
		}
	}
}