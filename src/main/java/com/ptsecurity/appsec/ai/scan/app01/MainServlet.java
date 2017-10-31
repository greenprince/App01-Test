package com.ptsecurity.appsec.ai.scan.app01;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final static Logger g_objLog = Logger.getLogger(MainServlet.class);
       
    /**
     * @see Servlet#init(ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
    }
	
    private Connection getDB() {
        Connection l_objRes = null;
        try {
            /**
             * Get initial context that has references to all configurations and
             * resources defined for this web application.
             */
            Context l_objInitCtx = new InitialContext();
            /**
             * Get Context object for all environment naming (JNDI), such as
             * resources configured for this web application.
             */
            Context l_objEnvCtx = (Context) l_objInitCtx.lookup("java:comp/env");
            // jdbc/App01/DB is a name of the Resource we want to access (see definition in META-INF/context.xml).
            // Get the data source for the DB to request a connection.
            DataSource l_objDS = (DataSource)l_objEnvCtx.lookup("jdbc/App01/DB");
            // Request a Connection from the pool of connection threads.
            l_objRes = l_objDS.getConnection();
        } catch (Exception e) {
            g_objLog.error(e);
        }
        return l_objRes;
    }

    protected void warning(HttpServletResponse response, String theWarning) throws IOException {
        response.getWriter().append("<div class=\"bs-callout bs-callout-warning\">");
        response.getWriter().append("<h4>Warning</h4>");
        response.getWriter().append(theWarning);
        response.getWriter().append("</div>");
    }

    protected void info(HttpServletResponse response, String theInfo) throws IOException {
        response.getWriter().append("<div class=\"bs-callout bs-callout-info\">");
        response.getWriter().append("<h4>Info</h4>");
        response.getWriter().append(theInfo);
        response.getWriter().append("</div>");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest theRq, HttpServletResponse response) throws ServletException, IOException {
        String l_strName = theRq.getParameter("NAME");
        if ((null == l_strName) || (l_strName.trim().isEmpty())) {
            this.warning(response, "NAME parameter missing");
            return;
        }
        Connection l_objDB = this.getDB();
        if (null == l_objDB) {
            this.warning(response, "DB connect failed");
            return;
        }
        g_objLog.debug("Database searched for " + l_strName);
        StringBuffer l_objRes = new StringBuffer();

        this.info(response, "Search for " + l_strName);
        try {
            String l_strQuery = "SELECT CONCAT (cname2, ' ', cname1) AS cname FROM pt.employee WHERE UPPER(cname2) = UPPER('" + l_strName + "')";
            ResultSet l_objRS;
            l_objRS = l_objDB.createStatement().executeQuery(l_strQuery);

            l_objRes.append("<table class=\"table\"><tr><th>Employee</th></tr>");
            while (l_objRS.next())
                l_objRes.append("<tr><td>").append(l_objRS.getString("cname")).append("</td></tr>");
            l_objRes.append("</table>");
        } catch (Exception e) {
            g_objLog.error(e);
        }
        response.getWriter().append(l_objRes.toString());
        response.setHeader("X-XSS-Protection", "0");
    }
}
