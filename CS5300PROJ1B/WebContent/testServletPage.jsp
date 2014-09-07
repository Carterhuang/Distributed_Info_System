<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<h1><%=request.getAttribute("replace")%></h1>

		<form method="post" action="MyServletPath">
				<input name="newMessage" />
				<input type="submit" value="Replace"/>
		</form>
			
		<button name="button1" onclick="location.replace('/MyServletPath?action=refresh')">Refresh</button>
		<button name="button2" onclick="location.replace('/MyServletPath?action=logout')">Logout</button><br/>
		<p>Server ID:</p>
		<p><%=request.getAttribute("serverID") %></p>
		<p>Server Primary:</p>
		<p><%=request.getAttribute("serverPR") %></p>
		<p>Server Primary Expire Time(s):</p>
		<p><%=request.getAttribute("expire") %></p>
		<p>Server Primary Discard Time(s):</p>
		<p><%=request.getAttribute("discard") %></p>
		<p>Server Backup:</p>
		<p><%=request.getAttribute("serverBK") %></p>
		<p>Server Backup Expire Time(s):</p>
		<p><%=request.getAttribute("expire") %></p>
		<p>Server Backup Discard Time(s):</p>
		<p><%=request.getAttribute("discard") %></p>
		<p>Server View:</p>
		<p><%=request.getAttribute("view") %></p>
		
		
		</body>
</html>