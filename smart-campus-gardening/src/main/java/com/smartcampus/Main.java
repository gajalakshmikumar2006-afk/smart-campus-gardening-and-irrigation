package com.smartcampus;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * Starts the whole website with an embedded web server - no separate Tomcat install needed.
 * Run this class (Eclipse: right-click -> Run As -> Java Application), then open http://localhost:8080/
 * The working directory must be the project root (the folder containing pom.xml).
 */
public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) port = Integer.parseInt(envPort.trim());

        File webapp = new File("src/main/webapp");
        if (!webapp.isDirectory()) {
            System.err.println("Run this from the project folder (the one containing pom.xml). Not found: " + webapp.getAbsolutePath());
            System.exit(1);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(new File("target/embedded-tomcat").getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addWebapp("", webapp.getAbsolutePath());
        // Let the server find the compiled servlets/filter (@WebServlet, @WebFilter) and db.properties.
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("\n==============================================");
        System.out.println(" Smart Campus Gardening is running:");
        System.out.println("   http://localhost:" + port + "/");
        System.out.println(" Login: admin@campus.edu / Admin@123");
        System.out.println("==============================================\n");
        tomcat.getServer().await();
    }
}
