# org.eclipse.equinox.jsp.jasper

## META-INF/MANIFEST.MF

```
diff --git a/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF b/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF
index 518e007..cf68456 100644
--- a/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF
+++ b/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF
@@ -10,15 +10,14 @@
  javax.servlet.annotation;version="2.6";resolution:=optional,
  javax.servlet.descriptor;version="2.6";resolution:=optional,
  javax.servlet.http;version="[2.4, 3.2)",
- javax.servlet.jsp;version="[2.0, 2.3)",
+ javax.servlet.jsp;version="[2.0, 2.4)",
  org.apache.jasper.servlet;version="[0, 8)",
  org.osgi.framework;version="1.3.0",
  org.osgi.service.http;version="1.2.0",
  org.osgi.service.packageadmin;version="1.2.0",
  org.osgi.util.tracker;version="1.3.1"
 Export-Package: org.eclipse.equinox.jsp.jasper;version="1.0.0"
-Bundle-RequiredExecutionEnvironment: CDC-1.0/Foundation-1.0,
- J2SE-1.3
+Bundle-RequiredExecutionEnvironment: JavaSE-1.6
 Comment-Header: Both Eclipse-LazyStart and Bundle-ActivationPolicy are specified for compatibility with 3.2
 Eclipse-LazyStart: true
 Bundle-ActivationPolicy: lazy
```

### Allow javax.servlet.jsp to be of version 2.4. 

Changed `javax.servlet.jsp;version="[2.0, 2.3)",`  to `javax.servlet.jsp;version="[2.0, 2.4)",`

Filed [bug 473471](https://bugs.eclipse.org/bugs/show_bug.cgi?id=473471) at the Eclipse bug tracker. 

### Change to Java SE 6

Changed to `Bundle-RequiredExecutionEnvironment: JavaSE-1.6`

# org.apache.jasper.glassfish

Source code: https://repo1.maven.org/maven2/javax/servlet/jsp/javax.servlet.jsp-api/2.3.2-b01/javax.servlet.jsp-api-2.3.2-b01-sources.jar

## META-INF/services/javax.servlet.ServletContainerInitializer

Delete the file.

This tries to load the TldScanner, which is handled differently in this scenario.
But having this file causes strange warnings on the console which confuses people looking at
the log output. 

## src/org/apache/jasper/compiler/AntJavaCompiler.java

Delete the file.

This file implements a possible compiler for Jasper based on Ant. We are sure to use
the Eclipse Java Compiler (JDT) and don't want any dependencies on Ant.

## src/org/apache/jasper/compiler/JDTJavaCompiler.java

### Implement new method

Implement a new method provided by a more recent version of Eclipse JDT.

```
diff --git a/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/JDTJavaCompiler.java b/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/JDTJavaCompiler.java
index 9008e9b..8759ee1 100644
--- a/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/JDTJavaCompiler.java
+++ b/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/JDTJavaCompiler.java
@@ -299,6 +299,10 @@
                 }
                 return result;
             }
+            
+            public boolean ignoreOptionalProblems() {
+                return true;
+            }
         }
 
         final INameEnvironment env = new INameEnvironment() {

```

### Allow using Java up to 1.8

Allow processing Java 1.6, 1.7 and 1.8.

```java
    public void setSourceVM(String sourceVM) {
        if(sourceVM.equals("1.1")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_1);
        } else if(sourceVM.equals("1.2")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_2);
        } else if(sourceVM.equals("1.3")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_3);
        } else if(sourceVM.equals("1.4")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_4);
        } else if(sourceVM.equals("1.5")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_5);
        } else if(sourceVM.equals("1.6")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_6);
        } else if(sourceVM.equals("1.7")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_7);
        } else if(sourceVM.equals("1.8")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_8);
        } else {
            log.warning("Unknown source VM " + sourceVM + " ignored.");
            settings.put(CompilerOptions.OPTION_Source,
                    CompilerOptions.VERSION_1_5);
        }
    }

    public void setTargetVM(String targetVM) {
        if(targetVM.equals("1.1")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_1);
        } else if(targetVM.equals("1.2")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_2);
        } else if(targetVM.equals("1.3")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_3);
        } else if(targetVM.equals("1.4")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_4);
        } else if(targetVM.equals("1.5")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_5);
        } else if(targetVM.equals("1.6")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_6);
        } else if(targetVM.equals("1.7")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_7);
        } else if(targetVM.equals("1.8")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_8);
        } else {
            log.warning("Unknown target VM " + targetVM + " ignored.");
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                    CompilerOptions.VERSION_1_5);
        }
    }
    
```


## src/org/apache/jasper/runtime/PerThreadTagHandlerPool.java

### Correct type

Use correct type and prevent adding additional dependencies.

```
diff --git a/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/PerThreadTagHandlerPool.java b/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/PerThreadTagHandlerPool.java
index d22b157..ccde532 100644
--- a/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/PerThreadTagHandlerPool.java
+++ b/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/PerThreadTagHandlerPool.java
@@ -64,7 +64,7 @@
 
 import javax.servlet.ServletConfig;
 import javax.servlet.jsp.JspException;
-import javax.servlet.jsp.tagext.JspTag;
+import javax.servlet.jsp.tagext.Tag;
 
 import org.apache.jasper.Constants;
 
@@ -84,7 +84,7 @@
     private ThreadLocal<PerThreadData> perThread;
 
     private static class PerThreadData {
-        JspTag handlers[];
+        Tag handlers[];
         int current;
     }
 

```

## META-INF/MANIFEST.MF

### Automatically import TLD classes

The backend system provides all TLD files to each JspServlet. So all defined tag and function
classes must be resolvable by this bundle. We do this by using a DynamicImport to `*`.

```
DynamicImport-Package: *
```

## src/org/apache/jasper/runtime/TldScanner.java

### Remove jstl core uri from system map

Jasper seems to handle the JSTL core tag library special. However this interferes with
using a different implementation.

Comment out:

```java
// systemUris.add("http://java.sun.com/jsp/jstl/core");
```

## src/org/apache/jasper/compiler/Generator.java

### Fix missing parent

A child tag will not get its parent if it was included by a custom tag:

diff --git a/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/Generator.java b/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/Generator.java
index 87a9b26..d14d174 100644
--- a/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/Generator.java
+++ b/org.apache.jasper.glassfish/src/org/apache/jasper/compiler/Generator.java
@@ -2990,8 +2990,15 @@
                 out.println(".setPageContext(_jspx_page_context);");
             }
 
+            // STARTJR: fix missing parent
             // Set parent
-            if (!simpleTag) {
+            if (isTagFile && parent == null) {
+                out.printin(tagHandlerVar);
+                out.print(".setParent(");
+                out.print("new javax.servlet.jsp.tagext.TagAdapter(");
+                out.print("(javax.servlet.jsp.tagext.SimpleTag) this ));");
+            } else if (!simpleTag) {
+            // ENDJR: fix missing parent
                 out.printin(tagHandlerVar);
                 out.print(".setParent(");
                 if (parent != null) {

## src/org/apache/jasper/runtime/JspRuntimeLibrary.java

### Fix `jsp:include` processing

Using `jsp:include` does not bring up the current URI when using relative includes.
It looks like the whole mechanism expects that the servlet is the full servlet JSP page
name without any further path information.

However the JspServlet from Eclipse Equinox does handle multiple JSP pages. And maybe
does something wrong passing this on to the Jasper servlet. In the end the `jsp:include` misses
out the "path info" path of the request and constructs a wrong URL for inclusion.

```
diff --git a/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/JspRuntimeLibrary.java b/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/JspRuntimeLibrary.java
index bd9c000..6cdb627 100644
--- a/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/JspRuntimeLibrary.java
+++ b/org.apache.jasper.glassfish/src/org/apache/jasper/runtime/JspRuntimeLibrary.java
@@ -931,10 +931,16 @@
             }
         }
         else {
+            // STARTJR: fix improper handling of jsp:include
             uri = hrequest.getServletPath();
+            String pathInfo = hrequest.getPathInfo ();
+            if ( pathInfo != null) {
+                uri = uri + pathInfo;
+            }
             if (uri.lastIndexOf('/') >= 0) {
                 uri = uri.substring(0, uri.lastIndexOf('/'));
             }
+            // ENDJR
         }
         return uri + '/' + relativePath;
 

``` 

## src/org/apache/jasper/servlet/JspCServletContext.java

### Implement new method in interface

```
diff --git a/org.apache.jasper.glassfish/src/org/apache/jasper/servlet/JspCServletContext.java b/org.apache.jasper.glassfish/src/org/apache/jasper/servlet/JspCServletContext.java
index ff89225..310a421 100644
--- a/org.apache.jasper.glassfish/src/org/apache/jasper/servlet/JspCServletContext.java
+++ b/org.apache.jasper.glassfish/src/org/apache/jasper/servlet/JspCServletContext.java
@@ -431,7 +431,6 @@
 
     }
 
-
     /**
      * Log the specified message.
      *
@@ -634,6 +633,10 @@
     public ClassLoader getClassLoader() {
         throw new UnsupportedOperationException();
     }
+    
+    public String getVirtualServerName() {
+ 	   throw new UnsupportedOperationException();
+    }
 
     public void declareRoles(String... roleNames) {
         throw new UnsupportedOperationException();

```
