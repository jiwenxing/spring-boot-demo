package com.jverson.springboot.controller;

import java.util.Random;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SSEController {

	@RequestMapping(value = "/push", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public @ResponseBody String push(){
		Random r = new Random();
        try {
                Thread.sleep(5000);
        } catch (InterruptedException e) {
                e.printStackTrace();
        }
        return "data:current value：" + r.nextInt() +"\n\n";
	}
	
	@RequestMapping("/sse")
	public String sse(){
		return "ssedemo";
	}
	
}
