package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/forgot")
	public String openEmailFrom() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		
		// generating otp of 4 digit random
		int otp = random.nextInt(99999);
		String subject = "OTP from Residential Contact Manager";
		String  message =""
				+ "<div style='border: 1px solid #e2e2e2; padding:20px' >"
				+ "<h1>"
				+ "OTP is: "
				+ "<b>" + otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else {
			session.setAttribute("message", new Message("Check your email!", "alert-danger"));
			return "forgot_email_form";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(myOtp == otp) {
			User user = this.userRepository.getUserByUseName(email);
			if(user == null){
				// send error
				session.setAttribute("message", new Message("User does not exist with this Email ID", "alert-danger"));
				return "forgot_email_form";
			}
			else {
				// send change password form
				return "password_change_form";
			}
		}
		else {
			session.setAttribute("message", new Message("Please enter the correct OTP", "alert-danger"));
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUseName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=Password changed successfully!";
	}
}
