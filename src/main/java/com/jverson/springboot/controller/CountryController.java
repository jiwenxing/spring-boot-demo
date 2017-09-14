package com.jverson.springboot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageInfo;
import com.jverson.springboot.model.Country;
import com.jverson.springboot.service.CountryService;

@Controller
@RequestMapping("/countries")
public class CountryController {

	@Autowired CountryService countryService;
	
	@RequestMapping
	public String index(Model model, Country country) {
		List<Country> countryList = countryService.getAll(country);
		model.addAttribute("pageInfo", new PageInfo<Country>(countryList));
		model.addAttribute("queryParam", country);
		model.addAttribute("page", country.getPage());
		model.addAttribute("rows", country.getRows());
		return "index";
	}
	
}
