package com.example.portal.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import com.example.portal.domain.User;
import com.example.portal.exception.domain.EmailExistsException;
import com.example.portal.exception.domain.EmailNotFoundException;
import com.example.portal.exception.domain.UserNameExistsException;
import com.example.portal.exception.domain.UserNotFoundException;

public interface UserService {
	
	User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UserNameExistsException, EmailExistsException, MessagingException;
	
	List<User> getUsers();
	
	User findUserByUsername(String username);
	
	User findUserByEmail(String email);
	
	User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage);
	
	User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage);
	
	void deleteUser(long id);
	
	void resetPassword(String email) throws EmailNotFoundException, MessagingException;
	
	User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UserNameExistsException, EmailExistsException, IOException;
	
}
