package sketchGen.transformer;

import java.io.File;
import java.util.*;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketchGen.analyzer.analysis.Holes;
import sketchGen.transformer.generator.ObsoleteExpressionGenerator;

public class ConditionTransformer extends Transformer {
	private Set<Operator> rop = new HashSet<>(
			Arrays.asList(BinaryExpr.Operator.EQUALS,
			BinaryExpr.Operator.NOT_EQUALS,
			BinaryExpr.Operator.GREATER,
			BinaryExpr.Operator.GREATER_EQUALS,
			BinaryExpr.Operator.LESS,
			BinaryExpr.Operator.LESS_EQUALS));

	private Set<Operator> lop = new HashSet<>(
			Arrays.asList(Operator.OR,
					Operator.AND));


	public ConditionTransformer(TransformInstance subject) {
		super(subject);
		transformerName = "cond";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof ExpressionStmt)
			visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			visit((IfStmt) targetStmt, null);
		return list;
	}

	// not being used currently
	public void visit(NameExpr n, Void arg) {
		// transform
		String type = subject.getOriginClass().resolveVarType(subject.getBugLocation().getMethod(), n);
		Expression e = ObsoleteExpressionGenerator.fetchEXP(subject, type);
		if (e != null)
			candidates.put(n, e);
	}

	public void visit(IfStmt stmt, Void arg) {
		super.visit(stmt, arg);
		//fetchCOND(stmt);
	}

	public void visit(BinaryExpr expr, Void arg) {
		super.visit(expr, arg);
		genBinaryExpr(expr);
	}

	private void genBinaryExpr(BinaryExpr binExpr) {
		Operator originOp = binExpr.getOperator();
		//relational operator
		if(subject.getHoleType() == Holes.HoleType.RopHole && rop.contains(originOp)) {
			for(BinaryExpr.Operator op: rop) {
				//skip the original operator
				if(op != originOp) {
					binExpr.setOperator(op);
					writeToFile();
				}
			}
		}
		// logical operator
		else if(subject.getHoleType() == Holes.HoleType.LopHole && lop.contains(originOp)) {
			if(originOp == Operator.AND) {
				binExpr.setOperator(Operator.OR);
				writeToFile();
			} else if(originOp == Operator.OR) {
				binExpr.setOperator(Operator.AND);
				writeToFile();
			}
		}
	}

}
