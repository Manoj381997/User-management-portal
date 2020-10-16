package com.example.portal.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432_000_000;     // 5 days expressed in milliseconds
    public static final String TOKEN_PREFIX = "Bearer ";        // Bearer means whoever gives me this token I don't have to verify further
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";  // convention, two words are separated by '-'
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String OWNER = "Manoj Kumar G";
    public static final String OWNER_ADMINISTRATION = "User Management Portal";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS"; // If the request method does not have any action(get,post,..), then we shud not do anything
    public static final String[] PUBLIC_URLS = { "/user/home", "/user/login", "/user/register", "/user/resetpassword/**", "/user/image/**" };
//    public static final String[] PUBLIC_URLS = { "**" };
}
