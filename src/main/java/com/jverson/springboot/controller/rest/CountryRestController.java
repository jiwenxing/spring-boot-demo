package com.jverson.springboot.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.jverson.springboot.model.Country;
import com.jverson.springboot.service.CountryService;

@RestController
@RequestMapping("/api/countries")
public class CountryRestController {

    @Autowired CountryService countryService;
	
	@RequestMapping
	public Object index(Country country) {
		List<Country> countryList = countryService.getAll(country);
		PageInfo<Country> pageInfo = new PageInfo<Country>(countryList);
		return pageInfo;
	}
	
}
