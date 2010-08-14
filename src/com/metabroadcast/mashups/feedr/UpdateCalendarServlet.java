package com.metabroadcast.mashups.feedr;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.metabroadcast.mashups.feedr.core.FeedrTask;
import com.metabroadcast.mashups.feedr.model.Media;

public class UpdateCalendarServlet extends HttpServlet {
	private Logger logger = Logger.getLogger(UpdateCalendarServlet.class.getName());
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		StringBuffer buffer = new StringBuffer();
		FeedrTask feedrTask = new FeedrTask();
		try{
			feedrTask.core();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		resp.getWriter().println(buffer);
	}
}
