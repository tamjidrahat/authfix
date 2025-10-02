package sketchGen.transformer;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketchGen.transformer.generator.ObsoleteExpressionGenerator;

public class ExpressionMutator extends Transformer {

	public ExpressionMutator(TransformInstance subject) {
		super(subject);
		transformerName = "expr";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof ExpressionStmt)
			this.visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			this.visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			this.visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			this.visit((IfStmt) targetStmt, null);
		// else if (targetStmt instanceof Assignment)

		return list;
	}

	public void visit(FieldAccessExpr n, Void arg) {
		// transform
		List<Node> nodes = n.getChildNodes();
		String type = subject.getLibParser().fieldResolveType(nodes);
		EnumDeclaration en = subject.getLibParser().getEnum(type);
		Expression exp = null;
		if (en == null)
			exp = ObsoleteExpressionGenerator.fetchEXP(subject, type);
		else
			exp = ObsoleteExpressionGenerator.fetchENUM(en, type);
		if (exp != null)
			candidates.put(n, exp);
	}

	public void visit(MethodCallExpr call, Void arg) {
		super.visit(call, arg);

		Expression scope = call.getScope().isPresent() ? call.getScope().get() : null;
		NodeList<Expression> param = call.getArguments();

		List<Node> visibleVars = subject.getVisibleVars();
		PriorityQueue<Node> queue = new PriorityQueue<Node>(new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o2.getBegin().get().line - o1.getBegin().get().line;
			}
		});
		queue.addAll(visibleVars);

		while (!queue.isEmpty()) {
			Node var = queue.poll();
			if (var instanceof VariableDeclarator) {
				VariableDeclarator v = (VariableDeclarator) var;
				String type = v.getType().toString();
				for (int i = 0; i < param.size(); i++) {
					Expression paramExpr = param.get(i);
					call.setArgument(i, v.getNameAsExpression());
					System.out.println(subject.getOriginClass().getCompilationUnit());
					writeToFile();
				}

			}
		}


	public void visit(BinaryExpr expr, Void arg) {
		super.visit(expr, arg);
		Expression left = expr.getLeft();
		if (candidates.containsKey(left)) {
			expr.setLeft(candidates.get(left));
			writeToFile();
			expr.setLeft(left);
		}
	}

	public void visit(ExplicitConstructorInvocationStmt thiz, Void arg) {
		super.visit(thiz, arg);
		NodeList<Expression> params = thiz.getArguments();
		for (int i = 0; i < params.size(); i++) {
			Expression p = params.get(i);
			if (candidates.containsKey(p)) {
				thiz.setArgument(i, candidates.get(p));
				writeToFile();
				thiz.setArgument(i, p);
			}
		}
	}

	public void visit(NameExpr n, Void arg) {
		// transform
		String type = subject.getOriginClass().resolveVarType(subject.getBugLocation().getMethod(), n);
		Expression e = ObsoleteExpressionGenerator.fetchEXP(subject, type);
		if (e != null)
			candidates.put(n, e);
	}

	public void visit(ReturnStmt n, Void arg) {
		super.visit(n, arg);
		try {
			Expression rtnExpr = n.getExpression().get();
			if (candidates.containsKey(rtnExpr)) {
				n.setExpression(candidates.get(rtnExpr));
				writeToFile();
				n.setExpression(rtnExpr);
			}
		} catch (Exception e) {
		}
	}

	public void visit(BooleanLiteralExpr expr, Void arg) {
		Expression e = ObsoleteExpressionGenerator.fetchEXP(subject, "boolean");
		if (e != null)
			candidates.put(expr, e);
	}

	public void visit(ConditionalExpr n, Void arg) {
		// transform
		super.visit(n, arg);
		Expression exp = n.getCondition();
		if (candidates.containsKey(exp)) {
			n.setCondition(candidates.get(exp));
			writeToFile();
			n.setCondition(exp);
		}
		exp = n.getElseExpr();
		if (candidates.containsKey(exp)) {
			n.setElseExpr(candidates.get(exp));
			writeToFile();
			n.setElseExpr(exp);
		}
		exp = n.getThenExpr();
		if (candidates.containsKey(exp)) {
			n.setThenExpr(candidates.get(exp));
			writeToFile();
			n.setThenExpr(exp);
		}

	}

	public void visit(VariableDeclarationExpr n, Void arg) {
		super.visit(n, arg);
		if (n.getVariable(0).getInitializer().isPresent()) {
			Expression exp = n.getVariable(0).getInitializer().get();
			if (candidates.containsKey(exp)) {
				VariableDeclarator decl = new VariableDeclarator(n.getVariable(0).getType(), n.getVariable(0).getName(),
						candidates.get(exp));
				n.setVariable(0, decl);
				writeToFile();
			}
		}
	}

}
