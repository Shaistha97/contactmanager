package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("username:" + userName);
		
		User user =  userRepository.getUserByUseName(userName);
		
		System.out.println("User: " + user);
		
		model.addAttribute("user", user);
		
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//Open "Add Form" handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//Process "Add Contact Form"
	@PostMapping("/process-contact")
	public String processContact(
			@Valid 
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			Model model, 
			HttpSession session) {
		
		String name = principal.getName();
		User user = userRepository.getUserByUseName(name);

		try {
			
			if(file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image Uploaded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			userRepository.save(user);
			System.out.println("Data: " + contact);
			session.setAttribute("message", new Message("Contact Added Successfully !!", "alert-success"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			
		}
		return "normal/add_contact_form";
	}
	
	// Show Contacts Handler
	// per page - 5[n]
	// current page - 0 [page] 
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, HttpSession session, Principal principal) {
		String userName = principal.getName();
		User user = userRepository.getUserByUseName(userName);
		
		Pageable pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts =  this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	// Show a particular contact Detail
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("cId: " + cId) ;
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = userRepository.getUserByUseName(userName);
		
		if(user.getId() == contact.getUser().getId()) 
			model.addAttribute("contact", contact);
	
		return "normal/contact_detail";
	}
	
	// Delete contact Handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session, Principal principal) {
		Contact contact = contactRepository.findById(cId).get();
		User user = userRepository.getUserByUseName(principal.getName());
		user.getContacts().remove(contact);
		userRepository.save(user);
		//contactRepository.delete(contact);
	
		session.setAttribute("message", new Message("Contact Deleted Successfully !!", "alert-success"));
		return "redirect:/user/show-contacts/0";
	}
	
	// open Update Form
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		
		Contact contact = contactRepository.findById(cid).get();
		model.addAttribute("title","Update Contact");
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	// update contact
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Model model, HttpSession session, Principal principal) {
		
		try {
			
			Contact oldContactDetail = contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//re-write the file
				// delete the old one
				/*File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();*/
				// update with new one
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user = userRepository.getUserByUseName(principal.getName());	
			contact.setUser(user);
			contactRepository.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact Name: " + contact.getName());
		System.out.println("Contact Id: " + contact.getcId());
		session.setAttribute("message", new Message("Contact Updated Successfully !!", "alert-success"));
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//Profile Picture
	@GetMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("title","User Profile");
		return "normal/profile";
	}
	
	
	// Open Settings Handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUseName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Password changed successfully!", "alert-success"));
		}
		else {
			session.setAttribute("message", new Message("Old Password is incorrect", "alert-danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
}
