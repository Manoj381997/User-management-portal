package com.example.portal.filters;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.example.portal.constant.SecurityConstant.*;
import com.example.portal.utility.JWTTokenProvider;


/* This Filter is going to fire every time there's a new req and its only going to fire once */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter{

	private JWTTokenProvider jwtTokenProvider;
	
	public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
		super();
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/* All we do here is whether Token is valid, User is valid
	Once verified, we can set the user as Authenticated User */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// If we receive a request with Option methods like GET, POST, PUT, DELETE let it pass
		// bcz option is seen before every re, and it is sent to gather info abt server
		if (request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
			response.setStatus(HttpStatus.OK.value());
		} else {
			// Gives a string of Authorization Header
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			
			// If the request header is null or Doesn't contain "Bearer " in it, then we dont work with that
			if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
				filterChain.doFilter(request, response);
				return;
			}
			// Else If the authorization header starts with "Bearer ", then we get the token
			// So here we are removing the "Bearer " part from header since we dont need it, we just need token
			String token = authorizationHeader.substring(TOKEN_PREFIX.length());
			String username = jwtTokenProvider.getSubject(token);
			
			// We need to check whether the token is valid and, also check that the actual context 
			// is not already set (i.e) For that specific request for the user, we dont already have the process done
			// We are just making sure the user is not already in there
			// We are doing this to let Spring know that this is a authenticated user
			if (jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null) {
				List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
				Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				SecurityContextHolder.clearContext();
			}
		}
		
		// FilterChain is used to pass in the request whenever we are done with it
		filterChain.doFilter(request, response);
	}

}
