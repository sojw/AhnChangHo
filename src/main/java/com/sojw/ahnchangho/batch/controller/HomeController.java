package com.sojw.ahnchangho.batch.controller;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sojw.ahnchangho.batch.job.listener.JobDuplicationListener;

@Controller
public class HomeController {
	@RequestMapping("/home")
	public ModelAndView home() {
		ModelAndView mav = new ModelAndView("home");

		mav.addObject("jobNames", jobRegistry.getJobNames());
		mav.addObject("runningJobNames", JobDuplicationListener.RUNNING_JOB_NAMES);

		return mav;
	}

	@Autowired
	private JobRegistry jobRegistry;
}
