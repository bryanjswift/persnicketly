package com.persnicketly.web;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import velocity.VelocityView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple servlet to render any requested .vm file
 * @author bryanjswift
 */
@Singleton
public class VelocityServlet extends HttpServlet {
    /** Logger for VelocityServlet */
    private static final Logger log = LoggerFactory.getLogger(VelocityServlet.class);

    /**
     * Get the log to use
     * @return Logger instance for this class
     */
    protected Logger logger() {
        return log;
    }

    /**
     * Get the context Map to be passed to the VelocityView
     * @param request - information about the servlet request
     * @return String to Object map of data to put into the VelicityView
     */
    protected Map<String, Object> getContext(HttpServletRequest request) {
        return new HashMap<String, Object>();
    }

    /**
     * The content type to send to the response
     * @return value for the content-type header of the response
     */
    protected String getContentType() {
        return MediaType.TEXT_HTML;
    }

    /**
     * Render requested .vm file
     * @param req - information about the requested resource
     * @param resp - where to write the response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getServletPath();
        logger().debug("Requested path is {} for referrer {}", path, req.getHeader("Referer"));
        final VelocityView view = new VelocityView(path);
        resp.setContentType(getContentType());
        view.render(getContext(req), resp);
    }
}
