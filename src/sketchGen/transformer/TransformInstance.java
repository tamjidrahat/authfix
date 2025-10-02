package sketchGen.transformer;

import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.Statement;

import sketchGen.analyzer.analysis.Holes;
import sketchGen.analyzer.analysis.BugLocation;
import sketchGen.analyzer.analysis.BuggyFileParser;
import sketchGen.analyzer.visitor.ClassVisitor;
import sketchGen.analyzer.visitor.LibraryParser;

public class TransformInstance {
	private BugLocation bugLocation;
	private ClassVisitor originClass;
	private List<Node> visibleVars;
	private Statement targetStmt;
	private LibraryParser libParser;
	private Holes.HoleType holeType;

	public TransformInstance(BugLocation loc, BuggyFileParser parser) {
		bugLocation = loc;
		libParser = parser.getLibParser();
		originClass = parser.parseFautyClass(loc.getFilePath());
		visibleVars = originClass.fetchVisibleVars(loc);
		targetStmt = originClass.fetchStatementOnLineNumber(loc.getMethod(), loc.getLocation());
		libParser.setFaultyClass(originClass.getClassNames().iterator().next());
	}
	public TransformInstance(BugLocation loc, BuggyFileParser parser, Holes.HoleType holeType) {
		bugLocation = loc;
		libParser = parser.getLibParser();
		originClass = parser.parseFautyClass(loc.getFilePath());
		visibleVars = originClass.fetchVisibleVars(loc);
		targetStmt = originClass.fetchStatementOnLineNumber(loc.getMethod(), loc.getLocation());
		libParser.setFaultyClass(originClass.getClassNames().iterator().next());
		this.holeType = holeType;
	}

	public BugLocation getBugLocation() {
		return bugLocation;
	}

	public void setBugLocation(BugLocation bugLocation) {
		this.bugLocation = bugLocation;
	}

	public ClassVisitor getOriginClass() {
		return originClass;
	}

	public void setOriginClass(ClassVisitor originClass) {
		this.originClass = originClass;
	}

	public List<Node> getVisibleVars() {
		return visibleVars;
	}

	public void setVisibleVars(List<Node> visibleVars) {
		this.visibleVars = visibleVars;
	}

	public Statement getTargetStmt() {
		return targetStmt;
	}

	public void setTargetStmt(Statement targetStmt) {
		this.targetStmt = targetStmt;
	}

	public LibraryParser getLibParser() {
		return libParser;
	}

	public void setLibParser(LibraryParser libParser) {
		this.libParser = libParser;
	}

	public Holes.HoleType getHoleType() {
		return this.holeType;
	}

	public void setHoleType(Holes.HoleType holetype) {
		this.holeType = holetype;
	}

}
