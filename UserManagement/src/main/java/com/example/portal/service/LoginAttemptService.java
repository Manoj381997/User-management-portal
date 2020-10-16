package com.example.portal.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {
	private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
	private static final int ATTEMPT_INCREMENT = 1;
	private LoadingCache<String, Integer> loginAttemptCache;
	
	/*	Cache will hold users in string and their attempts in integer
	 * 	users		Attempts
	 * ----------------------
	 * 	user-1			3
	 *  user-2			2
	 *  user-3			4
	 *  
	 *  Totally we have given 100 slots for cache
	 */
	public LoginAttemptService() {
		super();
		this.loginAttemptCache = CacheBuilder.newBuilder()
									.expireAfterWrite(15, TimeUnit.MINUTES)
									.maximumSize(100)
									.build(new CacheLoader<String, Integer>() {
											public Integer load(String key) {
												return 0;
											}
									});
	}
	
	
	public void evictUserFromLoginAttemptCache(String username) {
		this.loginAttemptCache.invalidate(username);
	}
	
	public void addUserToLoginAttemptCache(String username) {
		int attempts = 0;
		try {
			attempts = ATTEMPT_INCREMENT + this.loginAttemptCache.get(username);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		loginAttemptCache.put(username, attempts);
	}
	
	public boolean hasExceededMaxAttempts(String username) {
		try {
			return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}
}
