package com.example.portal.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.example.portal.constant.FileConstant.*;

import com.example.portal.constant.UserImplConstant;
import com.example.portal.domain.User;
import com.example.portal.domain.UserPrincipal;
import com.example.portal.enumeration.Role;
import com.example.portal.exception.domain.EmailExistsException;
import com.example.portal.exception.domain.EmailNotFoundException;
import com.example.portal.exception.domain.UserNameExistsException;
import com.example.portal.exception.domain.UserNotFoundException;
import com.example.portal.repository.UserRepository;
import com.example.portal.service.EmailService;
import com.example.portal.service.LoginAttemptService;
import com.example.portal.service.UserService;
import static com.example.portal.constant.UserImplConstant.*;

@Service
@Transactional	// Is to manage propagation whenever we are dealing with transactions
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService{

	private Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class); // UserServiceImpl.class or getClass()
	private UserRepository userRepository;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private LoginAttemptService loginAttemptService;
	private EmailService emailService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, LoginAttemptService loginAttemptService, EmailService emailService) {
		super();
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.loginAttemptService = loginAttemptService;
		this.emailService = emailService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findUserByUsername(username);
		if (user == null) {
			LOGGER.error(NO_USER_FOUND_BY_USERNAME + username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
		} else {
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepository.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info("Returning found user by username "+ username);
			return userPrincipal;
		}
	}

	@Override
	public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UserNameExistsException, EmailExistsException, MessagingException {
		validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		user.setUserId(generateUserId());
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		user.setPassword(encodedPassword);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setActive(true);
		user.setNotLocked(true);
		user.setJoinDate(new Date());
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getDefaultProfileImageUrl(username));
		
		userRepository.save(user);
		LOGGER.info("New user password: "+ password);
		emailService.sendNewPasswordEmail(firstName, password, email);
		return user;
	}

	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNotLocked, boolean isActive, MultipartFile profileImage) {
		User user = new User();
		try {
			validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
			user.setUserId(generateUserId());
			String password = generatePassword();
			user.setPassword(encodePassword(password));
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setUsername(username);
			user.setEmail(email);
			user.setActive(true);
			user.setNotLocked(true);
			user.setJoinDate(new Date());
			user.setRole(getRoleEnumName(role).name());
			user.setAuthorities(getRoleEnumName(role).getAuthorities());
			user.setProfileImageUrl(getDefaultProfileImageUrl(username));
			userRepository.save(user);
			LOGGER.info("New user password: "+ password);
			saveProfileImage(user, profileImage);
		} catch (UserNotFoundException | UserNameExistsException | EmailExistsException | IOException e) {
			e.printStackTrace();
		}
		return user;
	}

	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
			String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) {
		try {
			User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
			currentUser.setFirstName(newFirstName);
			currentUser.setLastName(newLastName);
			currentUser.setUsername(newUsername);
			currentUser.setEmail(newEmail);
			currentUser.setActive(isActive);
			currentUser.setNotLocked(isNotLocked);
			currentUser.setRole(getRoleEnumName(role).name());
			currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
			userRepository.save(currentUser);
			saveProfileImage(currentUser, profileImage);
			return currentUser;
		} catch (UserNotFoundException | UserNameExistsException | EmailExistsException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	@Override
	public User findUserByUsername(String username) {
		return this.userRepository.findUserByUsername(username);
	}

	@Override
	public User findUserByEmail(String email) {
		return this.userRepository.findUserByEmail(email);
	}

	@Override
	public void deleteUser(long id) {
		userRepository.deleteById(id);
	}

	@Override
	public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
		User user = userRepository.findUserByEmail(email);
		if (user == null) {
			throw new EmailNotFoundException(UserImplConstant.NO_USER_FOUND_BY_EMAIL + email);
		}
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		userRepository.save(user);
		LOGGER.info("New Password generated " + password);
		emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
	}

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UserNameExistsException, EmailExistsException, IOException {
		User user = validateNewUsernameAndEmail(username, null, null);
		saveProfileImage(user, profileImage);
		return user;
	}

	private String getDefaultProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
	}
	
	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
		if (profileImage != null) {
			Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
			if (!Files.exists(userFolder)) {
				Files.createDirectories(userFolder);
				LOGGER.info(DIRECTORY_CREATED + userFolder);
			}
			Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
			Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), StandardCopyOption.REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
			userRepository.save(user);
			LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
		}
	}

	private String setProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION).toUriString();
	}

	private Role getRoleEnumName(String role) {
		return Role.valueOf(role.toUpperCase());
	}

	private String encodePassword(String password) {
		return bCryptPasswordEncoder.encode(password);
	}

	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}

	private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) 
			throws UserNotFoundException, UserNameExistsException, EmailExistsException {
		if (StringUtils.isNoneBlank(currentUsername)) {
			User currentUser = findUserByUsername(currentUsername);
			if (currentUser == null) 
				throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
		
			User userByNewUsername = findUserByUsername(newUsername);
			if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
				throw new UserNameExistsException(USERNAME_ALREADY_EXISTS);
			}
			
			User userByNewEmail = findUserByEmail(newEmail);
			if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
				throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
			}
			return currentUser;
		} else {
			User userByUsername = findUserByUsername(newUsername);
			if (userByUsername != null) throw new UserNameExistsException(USERNAME_ALREADY_EXISTS);
			User userByEmail = findUserByEmail(newEmail);
			if (userByEmail != null) throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
		}
		return null;
	}
	
	private void validateLoginAttempt(User user) {
		if (user.isNotLocked()) {
			if (loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
				user.setNotLocked(false);
			} else {
				user.setNotLocked(true);
			}
		} else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
	}

}
