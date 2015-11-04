<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<p>
In a default setup Package Drone always has an <q>admin user</q> which is not registered but
built in. It has the role <code>ADMIN</code> but not <code>MANAGER</code>. Which allows it
to configure the system, but not play around with artifacts. 
</p>

<p>
The <q>admin token</q> is the password for the admin user. Since it is normally
randomly generated, it is called <q>token</q> instead of <q>password</q>.
</p>

<p>
There are two ways how this password is being generated. The default setup will create a
new token on every startup of Package Drone. It is printed on the console and written to
the file <code>~/.drone-admin-token</code> with limited read permissions.
</p>

<p>
The second way is to provide the token externally, through a system property or
environment variable.
</p>

<p>
It is also possible to disable the <q>admin mode</q> completely or change
the admin user name.
<a href="http://doc.packagedrone.org/book" target="_blank">See the documentation</a> for this.
</p>

<p>
You have to know how your system is configured. Because if you don't know, it probably is not
<em>your</em> system.
</p>