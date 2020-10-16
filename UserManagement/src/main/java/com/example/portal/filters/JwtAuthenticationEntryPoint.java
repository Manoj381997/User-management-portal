package com.example.portal.filters;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import com.example.portal.constant.SecurityConstant;
import com.example.portal.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/* This Filter is to process requests which fails Authentication 
 By default Spring takes care of it by responding with a default msg, but we are overriding and making it
 more pretty by sending a nicer response to the user*/
// If the user is not authenticated and they try to access the appln, this filter class gets triggered
// By default, spring sends an error msg, but we are overriding it, to make it clearer to the user
@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint{
	
	/**
	 * Always returns a 403 error code to the client.
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		HttpResponse httpResponse = new HttpResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN, 
				HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(), SecurityConstant.FORBIDDEN_MESSAGE);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
//		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
	}
}
