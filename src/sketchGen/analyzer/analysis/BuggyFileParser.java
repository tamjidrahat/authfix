package sketchGen.analyzer.analysis;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import sketchGen.analyzer.visitor.ClassVisitor;
import sketchGen.analyzer.visitor.LibraryParser;

public class BuggyFileParser {
	private Map<String, ClassVisitor> files = new HashMap<String, ClassVisitor>();
	private LibraryParser libParser = new LibraryParser();
	private String srcDir = "";


	public void setSrcDir(String sourceFolder) {
		this.srcDir = sourceFolder;
		this.libParser.setSource(srcDir);
	}

	public ClassVisitor parseFautyClass(String file) {
		if (files.containsKey(file))
			return files.get(file);
		if (!file.endsWith(".java"))
			return null;
		return analyzeSingleFile(file);
	}

	private ClassVisitor analyzeSingleFile(String file) {
		try {
			CompilationUnit cu = JavaParser.parse(new File(file));
			ClassVisitor classVisitor = new ClassVisitor();

			classVisitor.visit(cu, null);
			libParser.parseDir(new File(file.substring(0,file.lastIndexOf("/"))));

			for (String imp : classVisitor.getImports()) {
				String inimp = srcDir + imp.replace(".", "/");
				if (!new File(inimp).isDirectory())
					inimp += ".java";
				libParser.parseDir(new File(inimp));
			}

			files.put(file, classVisitor);
			return classVisitor;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public LibraryParser getLibParser() {
		return libParser;
	}



}
