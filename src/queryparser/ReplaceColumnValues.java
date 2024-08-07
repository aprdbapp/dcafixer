package queryparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class ReplaceColumnValues {

	static class ReplaceColumnAndLongValues extends ExpressionDeParser {

		@Override
		public void visit(StringValue stringValue) {
			this.getBuffer().append("?");
		}

//		@Override
//		public void visit(LongValue longValue) {
//			this.getBuffer().append("#");
//		}
	}

	public static String cleanStatement(String sql) throws JSQLParserException {
		StringBuilder buffer = new StringBuilder();
		ExpressionDeParser expr = new ReplaceColumnAndLongValues();

		SelectDeParser selectDeparser = new SelectDeParser(expr, buffer);
		expr.setSelectVisitor(selectDeparser);
		expr.setBuffer(buffer);
		StatementDeParser stmtDeparser = new StatementDeParser(expr, selectDeparser, buffer);
		try {
		Statement stmt = CCJSqlParserUtil.parse(sql);

		stmt.accept(stmtDeparser);
		}catch(Exception e) {
			return null;
				// System.out.println("CCJSqlParserUtil had an issue.");

		}

		return stmtDeparser.getBuffer().toString();
	}

	public static void main(String[] args) throws JSQLParserException {
		System.out.println(cleanStatement("SELECT abc, 5 FROM mytable WHERE 'col'='test'"));
		System.out.println(cleanStatement("UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'"));
		System.out.println(cleanStatement(
				"INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')"));
		System.out.println(cleanStatement("DELETE FROM table1 where col=5 and col2=4"));
		System.out.println(cleanStatement(
				"SELECT Orders.OrderID, Customers.CustomerName, Orders.OrderDate FROM Orders INNER JOIN Customers ON 'colName' = Customers.CustomerID"));
		String x = "d" + " r";
		String y = "ddd" + x;
		String z = "eeee" + x + "EEE" + " FFFF" + y;
		String g = "eeee" + x + "EEE" + z + " FFFF" + y;
	}
}
