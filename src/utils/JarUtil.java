package utils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JarUtil {

    public static void buildJarFromSrc(String srcFile, String mainClass) {
        String filename = srcFile.substring(0, srcFile.lastIndexOf('.'));

        // Compile the java source code
        compileJavaFile(filename+".java");

        // Create the manifest file
        createManifestFile(filename+".txt", mainClass);

        // Create the .jar file
        createJarFile(filename+".jar", filename+".txt", filename+".class");
    }

    private static void compileJavaFile(String javaFilePath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("Java Compiler not available.");
            System.exit(1);
        }
        int result = compiler.run(null, null, null, javaFilePath);
        if (result != 0) {
            System.err.println("Could not compile " + javaFilePath);
            System.exit(1);
        }
        System.out.println("Compilation successful: " + javaFilePath);
    }

    private static void createManifestFile(String manifestFilePath, String mainClass) {
        try (FileOutputStream fos = new FileOutputStream(manifestFilePath)) {
            String manifestContent = "Manifest-Version: 1.0\nMain-Class: " + mainClass + "\n";
            fos.write(manifestContent.getBytes());
            fos.flush();
            System.out.println("Manifest file created: " + manifestFilePath);
        } catch (IOException e) {
            System.err.println("Error creating manifest file: " + e.getMessage());
            System.exit(1);
        }
    }
    private static void createJarFile(String jarFilePath, String manifestFilePath, String classFilePath) {
        try (FileOutputStream fos = new FileOutputStream(jarFilePath);
             JarOutputStream jos = new JarOutputStream(fos, new java.util.jar.Manifest(new FileInputStream(manifestFilePath)))) {

            addToJar(jos, classFilePath);
            System.out.println("Jar file created: " + jarFilePath);
        } catch (IOException e) {
            System.err.println("Error creating jar file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void addToJar(JarOutputStream jos, String filePath) throws IOException {
        File file = new File(filePath);
        try (InputStream is = new FileInputStream(file)) {
            jos.putNextEntry(new JarEntry(file.getName()));
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
            jos.closeEntry();
        }
    }




}
