package com.finalpro.start.service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.finalpro.start.dao.MemberDAO;
import com.finalpro.start.dto.MemberDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.context.Context;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {
	//메일 전송 객체
	@Autowired
	private JavaMailSender emailSender;
	
	//Thymeleaf 사용을 위한 객체
	@Autowired
	private SpringTemplateEngine templateEngine;
	
	@Autowired
	private MemberDAO memberDAO;
	
	private String authNum;//인증코드 저장 변수
	
	//인증코드 생성 메소드
	public void createCode() {
		log.info("createCode()");
		
		Random random = new Random();
		StringBuffer key = new StringBuffer();
		
		//8자 코드 생성
		for(int i = 0; i < 8; i++) {
			int index = random.nextInt(3);
			
			switch (index) {
				case 0: 
					key.append((char)(random.nextInt(26) + 97));//대문자
					break;
				case 1:
					key.append((char)(random.nextInt(26) + 65));//소문자
					break;
				case 2:
					key.append((char)(random.nextInt(9) + 48));//숫자
			}
		}
		
		authNum = key.toString();
	}
	
	//메일 양식 생성 메소드
	public MimeMessage createEmailForm(String email) 
			throws MessagingException, UnsupportedEncodingException {
		log.info("createEmailForm()");
		
		String setFrom = "ajm0111@naver.com";//mail setting에 설정한 메일 주소
		String title = "인증 코드 전송";//메일 제목
		
		MimeMessage message = emailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, email);// 받는 사람
		message.setSubject(title);//제목 설정
		message.setFrom(setFrom);//보내는 사람
		message.setText(setContext(), "utf-8", "html");
		
		return message;
	}
	
	//Thymeleaf를 이용한 context(HTML, 메일 화면)를 설정하는 메소드
	private String setContext() {
		createCode();//인증 코드 생성
		Context context = new Context();
		context.setVariable("code", authNum);
		return templateEngine.process("mailForm", context);
	}
	
	//메일 전송 메소드
	public String sendEmail(String m_email, HttpSession session) {
	    log.info("sendEmail()");
	    
	    MimeMessage emailForm = null;
	    String res = null;
	    
	    try {
	        // 이메일 주소가 유효한지 확인할 수 있는 추가적인 검증을 수행할 수 있습니다.
	        // 여기서는 생략하겠습니다.

	        emailForm = createEmailForm(m_email);
	        
	        emailSender.send(emailForm);//메일 전송
	        
	        res = "ok";
	        //인증코드 확인을 위해 세션에 저장.
	        session.setAttribute("authNum", authNum);
	        session.setAttribute("email", m_email);
	    } catch (Exception e) {
	        e.printStackTrace();
	        res = "fail";
	    }
	    
	    return res;
	}

	public String codeAuth(String v_code, HttpSession session) {
		log.info("codeAuth()");
		String authNum = (String) session.getAttribute("authNum");
		String res = null;
		
		if(v_code.equals(authNum)) {
			res = "ok";
		} else {
			res = "fail";
		}
		
		session.removeAttribute("authNum");
		
		return res;
	}
}//class end
