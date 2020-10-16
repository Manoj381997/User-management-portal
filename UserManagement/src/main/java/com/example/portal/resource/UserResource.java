package com.example.portal.resource;

import com.example.portal.constant.FileConstant;
import com.example.portal.domain.HttpResponse;
import com.example.portal.domain.User;
import com.example.portal.domain.UserPrincipal;
import com.example.portal.exception.domain.EmailExistsException;
import com.example.portal.exception.domain.EmailNotFoundException;
import com.example.portal.exception.domain.ExceptionHandling;
import com.example.portal.exception.domain.UserNameExistsException;
import com.example.portal.exception.domain.UserNotFoundException;
import com.example.portal.service.UserService;
import com.example.portal.utility.JWTTokenProvider;
import static com.example.portal.constant.SecurityConstant.JWT_TOKEN_HEADER;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = {"/user"})
public class UserResource extends ExceptionHandling{

	private static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
	private static final String EMAIL_SENT = "An email with a new password was sent to ";
	
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JWTTokenProvider jwtTokenProvider;
    
	@GetMapping("/home")
    public String showUser(){
        return "Application works";
    }
    
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) throws UserNotFoundException, UserNameExistsException, EmailExistsException, MessagingException{
    	User newUser = this.userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK);
    }
    
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody User user) {
    	
    	authenticate(user.getUsername(), user.getPassword());
    	
    	User loginUser = userService.findUserByUsername(user.getUsername());
    	UserPrincipal userPrincipal = new UserPrincipal(loginUser);
    	HttpHeaders jwtHeader = getJwtHeaders(userPrincipal);
		return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }
    
    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
    									   @RequestParam("username") String username, @RequestParam("email") String email, 
    									   @RequestParam("role") String role, @RequestParam("isActive") String isActive, 
    									   @RequestParam("isNotLocked") String isNotLocked, 
    									   @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
		User newUser = userService.addNewUser(firstName, lastName, username, email, role, 
											Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
    	
    	return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestParam("currentUsername") String currentUsername, @RequestParam("firstName") String firstName, 
    									   @RequestParam("lastName") String lastName, @RequestParam("username") String username, 
    									   @RequestParam("email") String email, @RequestParam("role") String role,
    									   @RequestParam("isActive") String isActive, @RequestParam("isNotLocked") String isNotLocked, 
    									   @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
		User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username, email, role, 
											Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
    	
    	return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    
    @GetMapping("/find/{username}")
    public ResponseEntity<User> findUser(@PathVariable("username") String username) {
    	User user = userService.findUserByUsername(username);
    	return new ResponseEntity<>(user, HttpStatus.OK);
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<User>> list() {
    	List<User> users = userService.getUsers();
    	return new ResponseEntity<>(users, HttpStatus.OK);
    }
    
    @GetMapping("/reset-password/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {
    	userService.resetPassword(email);
    	return response(HttpStatus.OK, EMAIL_SENT + email);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")	// This is the reason why we have used "@EnableGlobalMethodSecurity" in Security config class
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id) {
    	userService.deleteUser(id);
    	return response(HttpStatus.NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }
    
    @PostMapping("/update-profile-image")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username, @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, UserNameExistsException, EmailExistsException, MessagingException, IOException{
    	User user = this.userService.updateProfileImage(username, profileImage);
		return new ResponseEntity<>(user, HttpStatus.OK);
    }
    
    @GetMapping(path = "/image/{username}/{fileName}", produces = {MediaType.IMAGE_JPEG_VALUE})
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER + username + FileConstant.FORWARD_SLASH + fileName));
		// "user.home" + "/supportportal/user/john/john.jpg"
    }
    
    @GetMapping(path = "/image/profile/{username}", produces = {MediaType.IMAGE_JPEG_VALUE})
    public byte[] getDefaultProfileImage(@PathVariable("username") String username) throws IOException {
    	URL url =  new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + username);
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	
    	try (InputStream inputStream = url.openStream()) {
    		int bytesRead;
    		byte[] chunk = new byte[1024];
    		while((bytesRead = inputStream.read(chunk)) > 0) {
    			byteArrayOutputStream.write(chunk, 0, bytesRead);
    		}
    	}
		return byteArrayOutputStream.toByteArray();
    }
    
	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
		return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), 
				message.toUpperCase()), httpStatus);
	}

	private HttpHeaders getJwtHeaders(UserPrincipal userPrincipal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
		return headers;
	}

	/* If wrong password or wrong username or account is locked or disabled , this mthd will throw an exception */ 
	private void authenticate(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}
}
