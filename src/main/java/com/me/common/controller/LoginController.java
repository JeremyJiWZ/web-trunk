package com.me.common.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
	Logger logger = Logger.getLogger(LoginController.class);
	@RequestMapping("/login")
	public ModelAndView login(WebRequest request){
		ModelAndView mv = new ModelAndView("/login");
		logger.info("inside login controller");
		return mv;
	}
}
