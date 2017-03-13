package com.me.jeremy.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.me.jeremy.service.JeremyService;

@Controller
public class JeremyController {
	@Resource private JeremyService jeremyService;
	
	@RequestMapping("Jeremy")
	public ModelAndView showIndex(WebRequest request){
		String url = "/Jeremy";
		jeremyService.increaseViewCount(url);
		ModelAndView mv = new ModelAndView("jeremy/index");
		mv.addObject("message", "你好，欢迎来到纪文忠的主页");
		mv.addObject("viewCount", jeremyService.getViewCount(url));
		mv.addObject("time", new Date());
		return mv;
	}
}
