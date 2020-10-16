package com.example.portal.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.portal.constant.SecurityConstant;
import com.example.portal.filters.JwtAccessDeniedHandler;
import com.example.portal.filters.JwtAuthenticationEntryPoint;
import com.example.portal.filters.JwtAuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)	// Method level Security is enabled
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{
	
	private JwtAuthorizationFilter jwtAuthorizationFilter;
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private JwtAccessDeniedHandler jwtAccessDeniedHandler;
	private UserDetailsService userDetailsService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
		
	@Autowired
	public SecurityConfiguration(JwtAuthorizationFilter jwtAuthorizationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler,
			@Qualifier("UserDetailsService") UserDetailsService userDetailsService,
			BCryptPasswordEncoder bCryptPasswordEncoder) {
		super();
		this.jwtAuthorizationFilter = jwtAuthorizationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
		this.userDetailsService = userDetailsService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService).passwordEncoder(this.bCryptPasswordEncoder); 
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// CORS -> Cross Origin Resource Sharing (we don't want any other domain to connect to our api)
		http.csrf().disable().cors()
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)	// Since we use JWT, no need to maintain sessions
			.and()
			.authorizeRequests()
			.antMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
			.anyRequest().authenticated()	// Any other req apart from public URL's has to be authenticated
			.and()
			.exceptionHandling().accessDeniedHandler(this.jwtAccessDeniedHandler)
			.authenticationEntryPoint(this.jwtAuthenticationEntryPoint)
			.and()
			.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		// TODO Auto-generated method stub
		return super.authenticationManagerBean();
	}	

}
