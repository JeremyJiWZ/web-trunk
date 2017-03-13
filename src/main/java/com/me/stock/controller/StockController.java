package com.me.stock.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.me.stock.service.StockService;

@Controller
@RequestMapping("/stock-info")
public class StockController {
	@Resource private StockService stockService;
	@SuppressWarnings("unused")
	static private Logger logger = Logger.getLogger(StockController.class);
	
	@RequestMapping(value={"","index"})
	public ModelAndView showIndex(HttpServletRequest request){
		ModelAndView modelAndView = new ModelAndView("stock/index");
		return modelAndView;
	}
	
	@ResponseBody
	@RequestMapping(value="get-notice", produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String getNotice(HttpServletRequest request, HttpServletResponse response){
		String result = JSON.toJSONString(stockService.getUnreadNotice());
//		logger.info(result);
		return result;
	}
	
}
