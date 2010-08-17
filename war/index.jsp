<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="com.google.appengine.api.users.UserService"%>
<%@page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@page import="com.google.appengine.api.users.User"%>
<%@page import="com.metabroadcast.mashups.feedr.model.AuthenticatedUser" %>
<%@page import="com.metabroadcast.mashups.feedr.model.TaskExecution" %>
<%@page import="com.metabroadcast.mashups.feedr.manager.EntityManager" %>
<%@page import="com.metabroadcast.mashups.feedr.google.calendar.CalendarUtil" %>
<%@page import="com.google.gdata.client.http.AuthSubUtil" %>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.logging.Logger"%><html>

<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>calendar feedr</title>
<style>
body {
	font: normal 14px/ 18px helvetica, arial, hirakakupro-w3, osaka,
		"ms pgothic", sans-serif;
	padding: 0;
	margin: 0;
	height: 100%;
}

.container {
	margin-left: auto;
	margin-right: auto;
	width: 800px;
	text-align: center;
}

.forms {
	text-align: center;
}

h1 {
	font-size: 100px;
	padding: 10px;
	margin-bottom: 0px;
}

h3 {
	margin-left: 200px;
}

.jid {
	font-size: 60px;
	width: 600px;
}

.instructions {
	font-style: italic;
}

.footer {
	font-size: 12px;
	background: #000;
	color: #eee;
	text-align: center;
}

.footer-container {
	position: absolute;
	width: 100%;
	bottom: 0px;
}

.caveat {
	text-align: right;
	font-size: 10px;
	padding-right: 10px;
}

.footer a {
	text-decoration: none;
	color: #fff;
}

a {
	text-decoration: none;
	color: #000;
}
</style>

</head>

<body>
<div class="container">
<h1>calendar feedr</h1>

<h3>never miss another show.</h3>

<%
		try{
			
			// get logged-in user 
			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
			EntityManager manager = new EntityManager();
			Logger logger = Logger.getLogger(this.getServletName());
			
			// if not logged-in redirect to logging screen
			if (user == null) {
		 		response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
			}
			logger.info("recognised user:"+user.getEmail());
		
			// user already authenticate with service? 
			AuthenticatedUser authUser = manager.getAuthenticatedUser(user);
			if (authUser != null && authUser.getToken() != null) {
				TaskExecution taskExecutionedOn = manager.getLatestTaskExecution(authUser.getUser().getEmail());
				StringBuffer excutionString = new StringBuffer();
				excutionString.append("Hi ").append(user.getNickname()).append("!");
				if (taskExecutionedOn == null){
					excutionString.append(" Your calendar have been queued for excution. In a while, check your calendar with name: ").append(CalendarUtil.CALENDAR_FEEDR_TITLE);
				} else {
					SimpleDateFormat format = new SimpleDateFormat("dd MM yyyy HH:mm");
					String xOn = format.format(taskExecutionedOn.getExecutedOn());
					excutionString.append("Your calendar has been updated last time on: ").append(xOn).append(" ");
				}
				logger.info("Auth calendar id:"+authUser.getCalendarUid());
				%>
				<p class="instructions" ><%=excutionString.toString() %></p>
				<p class="instructions" >Hope you enjoy service. If you feel like <a href="/deauthuser?uniqId=<%=authUser.getCalendarUid() %>">click here to</a> off from service.</p>			
				<%
			
			} else {
				// if not authenticated, get him to auth screen
				logger.info("Can't find auth user.");
				String nextUrl = "http://calendarfeedr.appspot.com/authsub";
				String scope = "http://www.google.com/calendar/feeds/";
				boolean secure = false;
				boolean ssion = true;
				String authSubUrl = AuthSubUtil.getRequestUrl(nextUrl, scope,
					secure, ssion);
				%>
				<p class="instructions" >Connect our media stream with your Google Calendar account.
				<a href="<%=authSubUrl %>">Click here to authenticate to Service</a>
<%
			}
			// sanaty check!
		} catch(Exception e){
			e.printStackTrace();
		}

		 %>
</div>

<div class="footer-container">
<div class="footer">
         Made by <a href="http://twitter.com/bojanbabic">@BojanBabic</a>. Made with <a href="http://code.google.com/appengine">GAE</a> and <a href="http://docs.atlasapi.org//">Atlas API</a> Technology. <a href="http://github.com/bojanbabic/Calendar-Feedr">You can check out the source here</a><br />
</div>
</div>

</body>
</html>
