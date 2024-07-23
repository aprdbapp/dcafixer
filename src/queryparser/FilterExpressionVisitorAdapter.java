package queryparser;

import java.util.ArrayList;
import java.util.List;

//import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;

//@ https://stackoverflow.com/questions/46800058/fully-parsing-where-clause-with-jsqlparser
public class FilterExpressionVisitorAdapter extends ExpressionVisitorAdapter {
	int depth = 0;
	public List<String> LeftColumns = new ArrayList<>();
	public List<String> RightColumns = new ArrayList<>();

	// TODO: add columns names list LEFT values, and Columns values list Right
	// values
	public void processLogicalExpression(BinaryExpression expr, String logic) {
//		String newStr = String.join("", Collections.nCopies(depth, "-"));
//		System.out.println(newStr + logic);
//    	System.out.println(StringUtils.repeat("-", depth) + logic);
//		System.out.println("@@@@ process Logical Expression");
		depth++;
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
		if (depth != 0) {
			depth--;
		}
	}

	public void processlikeExpression(LikeExpression expr, String logic) {
//		System.out.println("@@@@ process like Expression: "+ expr.toString());
//		if(expr.getEscape() != null)
//		System.out.println(expr.getEscape().toString());
//		if(expr.getLeftExpression() != null)
//		System.out.println("\tgetLeftExpression: "+expr.getLeftExpression().toString());
//		if(expr.getRightExpression() != null)
//		System.out.println("\tgetRightExpression: " + expr.getRightExpression().toString());

		String left, right;
		left = expr.getLeftExpression().toString();
		right = expr.getRightExpression().toString();
		if(!left.trim().startsWith("("))
		 {
			LeftColumns.add(left);
		//TODO: process inner expressions
//		if (left.contains(QParser.UI_taint_marker)) {
//			LeftColumns.add(left);
////			System.out.println("@@@@@@ mark as CN" + left);
//		}
		}

		if (right.contains(QParser.UI_taint_marker)) {
			RightColumns.add(right);
//			System.out.println("@@@@@@ mark as CV" + right);
		}
	}

	@Override
	protected void visitBinaryExpression(BinaryExpression expr) {
//		System.out.println("@@@@ visitBinaryExpression");
		if (expr instanceof ComparisonOperator) {
//        	String newStr = String.join("", Collections.nCopies(depth, "-"));
//          System.out.println(StringUtils.repeat("-", depth) +
//        	System.out.println(newStr+
//			System.out.println("left: " + expr.getLeftExpression() +
//					"  op: " + expr.getStringExpression() +
//					"  right: " + expr.getRightExpression());
			String left, right;
			left = expr.getLeftExpression().toString();
			right = expr.getRightExpression().toString();
			LeftColumns.add(left);
//			if (left.contains(QParser.UI_taint_marker)) {
//				LeftColumns.add(left);
////				System.out.println("@@@@@@ mark as CN" + left);
//			}

			if (right.contains(QParser.UI_taint_marker)) {
				// && !right.toLowerCase().startsWith("(select") &&
				// !right.toLowerCase().startsWith("( select")) {
				RightColumns.add(right);
//				System.out.println("@@@@@@ mark as CV" + right);
//				System.out.println("mark as CV");

			}

		}
		super.visitBinaryExpression(expr);
	}

	@Override
	public void visit(AndExpression expr) {
		processLogicalExpression(expr, "AND");

	}

	@Override
	public void visit(OrExpression expr) {
		processLogicalExpression(expr, "OR");
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit(LikeExpression exp) {
		processlikeExpression(exp, "LIKE");
	}

	@Override
	public void visit(Subtraction exp) {
		processLogicalExpression(exp, "-");
	}

	@Override
	public void visit(Addition exp) {
		processLogicalExpression(exp, "+");
	}
//	@Override
//	public void visit(INExpression exp) {
//		processLogicalExpression(exp, "LIKE");
//	}

}
