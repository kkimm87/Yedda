package com.apps.user.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.apps.user.domain.EmailSender;
import com.apps.user.domain.EmailVO;
import com.apps.user.domain.UserVO;
import com.apps.user.service.UserSvc;
import com.google.gson.Gson;

@Controller
public class UserController {
	
	private Logger log = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	UserSvc userSvc;
	
	@Autowired
	private EmailSender emailSender;

	//메인페이지 호출
	@RequestMapping(value="main.do") 
	public String login(HttpServletRequest request) {
		
		log.debug("0=====================================");
		log.debug("main()");
		log.debug("0=====================================");
		
		return "login";
	}
	//로그인시 유효성
	@RequestMapping(value="do_login.do", method=RequestMethod.POST)
	public ModelAndView do_login(HttpServletRequest request,HttpServletResponse res) throws IOException {
		
		log.debug("1======================================");
		log.debug("do_login.do");
		log.debug("1======================================");
		
		ModelAndView modelAndView = new ModelAndView();
		UserVO inVO = new UserVO();
		UserVO outVO = new UserVO();
		
		inVO.setId(request.getParameter("id"));
		inVO.setPassword(request.getParameter("password"));

		outVO = (UserVO)userSvc.do_login(inVO);
		
		if(outVO != null && outVO.getDelete_flag()==0) {
			
			log.debug(outVO.toString());
					
			HttpSession session = request.getSession(true);
			session.setAttribute("loginUser", outVO);
			session.setAttribute("ID", outVO.getId());
			if(outVO.getAdmin_flag() == 1){
				res.sendRedirect("admin/category.do");
				modelAndView.setViewName("admin");
			}else{
				res.sendRedirect("budget/daily.do");
				modelAndView.setViewName("daily");
			}
		} else {
			
			int idFlag = userSvc.do_check_id(inVO.getId());
			int passwordFlag = userSvc.do_check_passwd(inVO);
			
			if(outVO!=null && outVO.getDelete_flag()==1) {
				modelAndView.addObject("message", "deleteUser");
			}
			
			if(idFlag == 0 || passwordFlag == 0) {
				modelAndView.addObject("message", "passwordFailure");
			}
			
			modelAndView.setViewName("login");
		}
		
		return modelAndView;
	}
	
	@RequestMapping(value="createUser.do")
	public String goCreateUser(HttpServletRequest request) {
		log.debug("0=====================================");
		log.debug("goCreateUser()");
		log.debug("0=====================================");
		
		return "createUser";
	}

	@RequestMapping(value="do_check_id.do")
	@ResponseBody
	public String do_check_id(HttpServletRequest request) {
		
		log.debug("1======================================");
		log.debug("do_check_id");
		log.debug("1======================================");
		
		int flag = 0;
		
		String id = request.getParameter("id");
		
		log.debug("id : "+id);
		
		flag = userSvc.do_check_id(id);
		
		log.debug("flag(control) : "+flag);
		
		return flag+"";
	}
	
	@RequestMapping(value="do_check_email.do")
	@ResponseBody
	public String do_check_email(HttpServletRequest request) {
		
		log.debug("1======================================");
		log.debug("do_check_email");
		log.debug("1======================================");
		
		int flag = 0;
		
		String email = request.getParameter("email");
		
		log.debug("email : "+email);
		
		flag = userSvc.do_check_email(email);
		
		log.debug("flag(control) : "+flag);
		
		return flag+"";
	}
	
	@RequestMapping(value="do_save.do")
	public void do_save(HttpServletRequest request,HttpServletResponse res) throws IOException{
		
		UserVO inVO = new UserVO();
		
		inVO.setId(request.getParameter("id"));
		inVO.setPassword(request.getParameter("password"));
		inVO.setName(request.getParameter("name"));
		inVO.setEmail(request.getParameter("email"));
		int fixed_income = Integer.parseInt(request.getParameter("fixed_income"));
		inVO.setFixed_income(fixed_income);
		int balance = Integer.parseInt(request.getParameter("balance"));
		inVO.setBalance(balance);
		
		log.debug("inVO : "+inVO.toString());
		
		int flag = 0;
		flag = userSvc.do_save(inVO);
		
		log.debug("flag : "+flag);
		
		res.sendRedirect("main.do");
	}
	
	@RequestMapping(value="logout.do")
	public void logout(HttpServletRequest req,HttpServletResponse res, HttpSession session) throws IOException {
		log.debug("0=====================================");
		log.debug("logout()");
		log.debug("0=====================================");
		session.invalidate();
		res.sendRedirect("main.do");
		
	}
	
	// ID/PW 찾기 페이지들어가기
	@RequestMapping(value="missing.do") 
	public String missing(HttpServletRequest request) {
		
		log.debug("0=====================================");
		log.debug("missing()");
		log.debug("0=====================================");
		
		return "missing";
	}
	
	
	// ID 찾기
	@RequestMapping(value="do_findID.do" , method = RequestMethod.POST) 
	public ModelAndView missing_ID(HttpServletRequest request) {
		
		ModelAndView modelAndView = new ModelAndView();
		UserVO userVO = new UserVO();
		
		userVO.setName(request.getParameter("name"));
		userVO.setEmail(request.getParameter("email"));
		
		String id = userSvc.do_findID(userVO);
		
		if(id != null) {
			modelAndView.addObject("message", "idOK");
			modelAndView.setViewName("missing");
			modelAndView.addObject("id", id);
			
		} else if(null == id || id.equals("")) {
			modelAndView.addObject("message", "idNo");
			modelAndView.setViewName("missing");
		}
		
		return modelAndView;
	}
	
	
	@RequestMapping(value="do_findPW.do" , method = RequestMethod.POST) 
	public ModelAndView missing_PW(HttpServletRequest request)  {
		
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			UserVO userVO = new UserVO();
			
			userVO.setId(request.getParameter("inputID"));
			userVO.setEmail(request.getParameter("inputEmail"));
			
			String pw = userSvc.do_findPW(userVO);
			
			if(pw != null) {
				
				EmailVO emailVO = new EmailVO();
				
				emailVO.setContent("비밀번호는 "+pw+" 입니다.");
	            emailVO.setReceiver(userVO.getEmail());
	            emailVO.setSubject("비밀번호 찾기 메일입니다.");
	            log.debug("emailSender=====================================");
	    		log.debug(emailSender.toString());
	    		log.debug("emailSender=====================================");
	            
	            
				modelAndView.addObject("message", "pwOK");
				modelAndView.addObject("email", userVO.getEmail());
				modelAndView.setViewName("missing");
				
				emailSender.SendEmail(emailVO);
	
				
			} else if(null == pw || pw.equals("")) {
				modelAndView.addObject("message", "pwNo");
				modelAndView.setViewName("missing");
			}
		}catch(Exception e) {
			System.out.println("==============");
			e.printStackTrace();
			System.out.println("=============="+e.getMessage());
		}
		return modelAndView;
	}
	
	
	//마이페이지 호출
	@RequestMapping(value="mypage.do") 
	public ModelAndView identify(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("loginID",session.getAttribute("ID"));
		mav.setViewName("identify");
		log.debug("0=====================================");
		log.debug("mypage()");
		log.debug("0=====================================");
		
		return mav;
	}
	@RequestMapping(value="checkPW.do") 
	@ResponseBody
	public String check_pass(HttpServletRequest request) {
		UserVO inUserVO = new UserVO();
		log.debug("0=====================================");
		log.debug("mypage()");
		log.debug("0=====================================");
		inUserVO.setId(request.getParameter("id"));
		inUserVO.setPassword(request.getParameter("password"));
		int flag = userSvc.do_check_passwd(inUserVO);
		Gson gson=new Gson();
		String retString = gson.toJson(flag);
		log.debug("4===============retString="+retString);
		
		return retString;
	}
	
	
	//회원정보수정화면으로 이동
	@RequestMapping(value="updateUser.do", method= {RequestMethod.POST,RequestMethod.GET}) 
	public ModelAndView updateUser(HttpSession session, HttpServletRequest request) {
		
		UserVO loginUser = (UserVO) session.getAttribute("loginUser");
		
		log.debug("loginUser" + loginUser.toString());
		
		ModelAndView modelAndView =new ModelAndView();
		modelAndView.addObject("loginUser", loginUser);
		modelAndView.setViewName("updateUser");
		
		return modelAndView;
	}
	
	// 회원정보수정
	@RequestMapping(value="do_update.do")
	public ModelAndView do_update(HttpSession session, HttpServletRequest request) {
		
		UserVO sessionVO = (UserVO) session.getAttribute("loginUser");
		UserVO inVO = new UserVO();
		
		inVO.setId(sessionVO.getId());
		inVO.setPassword(request.getParameter("password"));
		inVO.setName(request.getParameter("name"));
		inVO.setEmail(request.getParameter("email"));
		int fixed_income = Integer.parseInt(request.getParameter("fixed_income"));
		inVO.setFixed_income(fixed_income);
		inVO.setBalance(sessionVO.getBalance());
		inVO.setDelete_flag(sessionVO.getDelete_flag());
		inVO.setAdmin_flag(sessionVO.getAdmin_flag());
		
		
		int flag = userSvc.do_update(inVO);
		
		
		ModelAndView modelAndView =new ModelAndView();
		modelAndView.setViewName("login");
		modelAndView.addObject("inVO", inVO);
		
		return modelAndView;
		
	}
	
	//회원탈퇴
	@RequestMapping(value="do_delete.do" ,method= {RequestMethod.POST,RequestMethod.GET})
	public String do_delete(HttpSession session) throws IOException {
		
		UserVO sessionVO = (UserVO) session.getAttribute("loginUser");
		
		int flag = userSvc.do_delete(sessionVO);
		int deleteLog_Flag = userSvc.do_dlog_save(sessionVO.getId());
		//DELETE_LOG 테이블 쓸 수 있게 만들기
		
		return "redirect:logout.do";
	}
}
