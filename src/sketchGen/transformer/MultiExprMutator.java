package sketchGen.transformer;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;

public class MultiExprMutator extends ExpressionMutator {

	public MultiExprMutator(TransformInstance subject) {
		super(subject);
		transformerName = "mem";
	}

	public void visit(ExplicitConstructorInvocationStmt thiz, Void arg) {
		super.visit(thiz, arg);
		NodeList<Expression> params = thiz.getArguments();
		for (int i = 0; i < params.size() - 1; i++) {
			Expression p = params.get(i);
			if (candidates.containsKey(p)) {
				thiz.setArgument(i, candidates.get(p));
				for (int j = i+1; j < params.size(); j++) {
					Expression q = params.get(j);
					if (candidates.containsKey(q)) {
						MethodCallExpr e =(MethodCallExpr) ((CastExpr)((EnclosedExpr)candidates.get(q)).getInner()).getExpression();
						e.setArgument(e.getArguments().size()-1, new IntegerLiteralExpr("1"));

						thiz.setArgument(j, candidates.get(q));
						writeToFile();
						e.setArgument(e.getArguments().size()-1, new IntegerLiteralExpr("0"));
						thiz.setArgument(j, q);
					}
				}
				thiz.setArgument(i, p);
			}
		}
	}
}
