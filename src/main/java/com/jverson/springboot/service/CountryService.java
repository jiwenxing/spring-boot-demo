package com.jverson.springboot.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.jverson.springboot.mapper.CountryMapper;
import com.jverson.springboot.model.Country;

import tk.mybatis.mapper.entity.Example;

@Service
public class CountryService {

	@Autowired CountryMapper countryMapper;
	
	public List<Country> getAll(Country country){
		if (country.getPage()!=null && country.getRows()!=null) {
			PageHelper.startPage(country.getPage(), country.getRows());
		}
		Example example = new Example(Country.class);
		Example.Criteria criteria = example.createCriteria();
		if (StringUtil.isNotEmpty(country.getCountryname())) {
			criteria.andLike("countryname", "%"+country.getCountryname());
		}
		if (StringUtil.isNotEmpty(country.getCountrycode())) {
			criteria.andLike("countrycode", "%"+country.getCountrycode());
		}
        return countryMapper.selectByExample(example);
	}
	
}
