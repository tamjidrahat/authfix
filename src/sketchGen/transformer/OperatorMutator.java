package sketchGen.transformer;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketchGen.transformer.generator.ObsoleteOperatorGenerator;

public class OperatorMutator extends Transformer {
	private Set<Operator> rop = new HashSet<>(Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS,
			Operator.GREATER, Operator.GREATER_EQUALS, Operator.LESS, Operator.LESS_EQUALS));

	public OperatorMutator(TransformInstance subject) {
		super(subject);
		transformerName = "op";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof ExpressionStmt)
			visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			this.visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			this.visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			this.visit((IfStmt) targetStmt, null);
		return list;
	}

	public void visit(BinaryExpr expr, Void arg) {
		// relational rop
		super.visit(expr, arg);
		// I dont mutate bop
		// if (!rop.contains(expr.getOperator()))
		// return;
		String type = "int";
		if (expr.getLeft() instanceof NameExpr) {
			NameExpr n = (NameExpr) expr.getLeft();
			type = subject.getOriginClass().resolveVarType(subject.getBugLocation().getMethod(), n);
		}

		Expression rep = ObsoleteOperatorGenerator.fetchOperator(expr, type, "ROP");
		if (rep != null)
			candidates.put(expr, rep);
	
		Expression left = expr.getLeft();
		Expression right = expr.getRight();
		if (candidates.containsKey(left)) {
			expr.setLeft(candidates.get(left));
			writeToFile();
			expr.setLeft(left);
		}
		if (candidates.containsKey(right)) {
			expr.setRight(candidates.get(right));
//			System.out.println(subject.getOriginClass().getCompilationUnit());
			writeToFile();
			expr.setRight(right);
		}
	}

	public void visit(IfStmt ifStmt, Void arg) {
		super.visit(ifStmt, arg);
		Expression cond = ifStmt.getCondition();
		if (candidates.containsKey(cond)) {
			ifStmt.setCondition(candidates.get(cond));
			writeToFile();
			ifStmt.setCondition(cond);
		}
	}

}