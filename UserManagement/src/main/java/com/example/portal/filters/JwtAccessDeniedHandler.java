package com.example.portal.filters;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.portal.constant.SecurityConstant;
import com.example.portal.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

//This Class is used to handle requests when user does not have permission to access specific resource
// by default spring handles it by throwing an error, but we are overriding it to provide better msg
// The user is Unauthorized to do this request --> 401 Status
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		HttpResponse httpResponse = new HttpResponse(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED, 
				HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase(), SecurityConstant.ACCESS_DENIED_MESSAGE);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(outputStream, httpResponse);
		outputStream.flush();		
	}

}
