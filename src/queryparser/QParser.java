package queryparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.junit.platform.commons.util.StringUtils;

import com.ibm.wala.ipa.slicer.MethodExitStatement;
import com.ibm.wala.ipa.slicer.Statement;

import flocalization.G;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserDefaultVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserTreeConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import slicer.datatypes.Sig;

//import org.junit.platform.commons.util.StringUtils;
/*TODO: 
 * 1- visit the following examples:
 * https://stackoverflow.com/questions/16768365/how-to-retrieve-table-and-column-names-from-sql-using-jsqlparse
 * 
 * 2- perform query cleaner to replace column values that starts with UI_taint_marker
 * Note: To perform the cleaner, check "ReplaceColumnValues" class, 
 * and change all value you want to change to be surrounded with single quotation 'xxx'   
 * */
public class QParser {
	static  class MappingInfo{
		String call;
		String temp;
		boolean callIsReplace;
		char uit;
		
	}
	final static String UI_taint_marker = "_UI999_";// general -> then it becomes for column value
	final static String TN_taint_marker = "_UITN_";// table name
	final static String CN_taint_marker = "_UICN_";// column name
	final static String LIKE_symbol = "_123412341234_";
	final static String PS_place_holder = "pstmt_dcafixer";
	final static String CV_taint_marker = "_UICV_";
	final static String TN_CN_Whitelist = ".replaceAll(\"[^a-zA-Z0-9-'_']\", \"\")";
	final static String CV_Whitelist = ".replaceAll(\"[^a-zA-Z0-9-'_'-' ']\", \"\")";
	final static String LSbracket_temp ="_LSQB_";
	final static String RSbracket_temp ="_RSQB_";
	// Prepare_formated_query
	public static List<String> SchemaTables = new ArrayList<>();// +++
	public static List<String> UI_Tables = new ArrayList<>();
	public static List<String> UI_Tables_labeled = new ArrayList<>();
	public static List<String> SchemaColumns = new ArrayList<>();
	public static List<String> UI_ColNames = new ArrayList<>();
	public static List<String> UI_ColNames_labeled = new ArrayList<>();
	public static List<String> UI_ColValues = new ArrayList<>();
	public static List<String> UI_ColValues_labeled = new ArrayList<>();
	public static List<String> set_strings = new ArrayList<>();
	public static String PS_query = null;
	public static String WL_query = null;
	public String getWL_query() {
		return WL_query;
	}
	public void setWL_query(String wL_query) {
		WL_query = wL_query;
	}

	public static int prefered_sol = G.NOSOL;// 1 prepared stmt, 2 white listing
	public static String original_query = null;
	public static String new_cq = null;
	public static Sig qs_ql = null;
	public static int c = 0;
	public static boolean allHasReplace = false;
	public static String msg = null;
	public static boolean ParserFail = false;
	//for WL
	public static List<MappingInfo> mappingWL = new ArrayList<>();

	private static CCJSqlParserManager parserManager = new CCJSqlParserManager();
	public boolean parserFailed() {
		return ParserFail;
	}
	public Sig get_qs_ql() {
		return qs_ql;
		
	}
	public String get_msg() {
		return msg;
	}
	public String get_new_cq() {
		return new_cq;
	}
	public int get_prefered_sol() {
		return prefered_sol;
	}
	public boolean get_allHasReplace() {
		return allHasReplace;
	}
	public String get_pstmt_query() {
		return PS_query;
	}
	public List<String>  get_set_strings() {
		return set_strings;
		
	}
//	public ?? get_??() {
//		return ;
//		
//	}
//	public ?? get_??() {
//		return ;
//		
//	}
	public QParser() {
		
	}
	
	public QParser(String query, boolean print, List<String> userinputvars) throws JSQLParserException, IOException {
		parse_query(query, print, userinputvars);
	}
	
	public static void prepare_formated_query(String query, String tail) {

//String.format handling steps:
//("...", %,% ...)

	}

	public static Sig create_query_sig(String q) {
		// TODO: check if s belongs to the TA_list, and get vars list (Read SIG-PAPER I
		// wrote) AND consider functions calls
//		System.out.println("----- In create_query_sig");
		char V, T;
		List<Character> I = new ArrayList<>();
		Sig x = new Sig();
//		x.V = 'Q';
		V = 'Q';
		// === Set T value
		if ((q.split("\\s+"))[0].equalsIgnoreCase("select"))
			T = 's';//x.T = 's';
		else if ((q.split("\\s+"))[0].equalsIgnoreCase("insert"))
			T = 'i';//x.T = 'i';
		else if ((q.split("\\s+"))[0].equalsIgnoreCase("update"))
			T = 'u';//x.T = 'u';
		else if ((q.split("\\s+"))[0].equalsIgnoreCase("delete"))
			T = 'd';//x.T = 'd';
		else {
			T= '_';
			if(!q.contains(UI_taint_marker)// added this to avoid consider some constant calls as [a] 
					&& (q.contains("(")&& q.contains(")") && // ++ the query is a method e.g., scanner.next()
					!G.QueriesList.contains((q.split("\\s+"))[0].toLowerCase())) ) //To handle DDL commands 
				I.add('a');						  //++
		}
		// === Set I values
		// TODO: check taint marks for tables, columns and columns values, create T list
		if (q.contains(CV_taint_marker) || q.contains(TN_taint_marker) || q.contains(CN_taint_marker)
				|| q.contains(UI_taint_marker)) {//

			if (q.contains(CV_taint_marker) &&  UI_ColValues.size()>0 )//|| q.contains(UI_taint_marker)) // UI_taint_marker1??
				I.add('v');//x.I.add('v');
			if ((q.contains(TN_taint_marker) && UI_Tables.size()>0  )|| q.contains(UI_taint_marker))
				I.add('t');//x.I.add('t');
			if (q.contains(CN_taint_marker) && UI_ColNames.size()>0 )
				I.add('c');//x.I.add('c');
			// TODO: check if I have to use a different input for UI_taint_marker1
			// if (q.contains(UI_taint_marker1))
			// x.T.add('c');
		} else {
			if(!I.contains('a'))
				I.add('-');	// constant queries					  //x.I.add('-');
		}
		
		x.setvalues(V, T, I);
//		System.out.print(x.toString());

		return x;
	}

	public static String classify_userinput(String cq) throws JSQLParserException {
//		System.out.println("----- In classify_userinput");

		Expression whereClause = null;
		List<String> tableList = new ArrayList<>();
		List<SelectItem> ColumnsList = new ArrayList<>();
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		new_cq = null;
		new_cq = cq;
		try {
//		=============================== SELECT ===============================
		// SELECT ... FROM ... JOIN ... WHERE ... ORDER BY...
//			System.out.println("#############...1 ");
		if (cq.trim().toLowerCase().startsWith("select")) {
//		if ((cq.split("\\s+"))[0].equalsIgnoreCase("select")) {
//		if (statement instanceof Select) {
			Select selectStatement = (Select) CCJSqlParserUtil.parse(cq);

			//System.out.println("Tables: " + tableList);
			PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();

			// ----------- SELECT ... -----------
			ColumnsList = plainSelect.getSelectItems();
//			plainSelect.getASTNode()
			// Using ast nodes to get column names
//			System.out.println("Columns in select part:");
			for (SelectItem s : ColumnsList) {
				s.getASTNode();
				SimpleNode node = (SimpleNode) s.getASTNode();// CCJSqlParserUtil.parseAST(s);
				node.jjtAccept(new CCJSqlParserDefaultVisitor() {
					@Override
					public Object visit(SimpleNode node, Object data) {
//							//Note: Recursive here to handle nested select didn't work
						if (node.getId() == CCJSqlParserTreeConstants.JJTCOLUMN) {
							String col = node.jjtGetValue().toString();
							if (col.contains(UI_taint_marker)) {
								String new_col = col.replace(UI_taint_marker, CN_taint_marker);
								new_cq = new_cq.replace(col, new_col);
								UI_ColNames_labeled.add(new_col);
								UI_ColNames.add(col.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
							} else {
								SchemaColumns.add(col.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
//								// TODO: Figure out why the code bellow causes more failed tests!!!
//								System.out.println(col);
//								if (col.contains(".")) {
//									String[] onlyCols = col.split(".");
//									AllColumns.add(onlyCols[onlyCols.length-1]);
//								} else {
//									AllColumns.add(col);
//								}//==================
							}
							//System.out.println("Col: " + node.toString());
//							System.out.println("Col: " + node.jjtGetValue());
							return super.visit(node, data);

						} else {
							return super.visit(node, data);
						}
					}
				}, null);
			}

			// ----------- FROM ... -----------
			tableList = tablesNamesFinder.getTableList(selectStatement);
			// ====== JOIN ...
			if (plainSelect.getJoins() != null) {
				for (Join join : plainSelect.getJoins()) {// TODO: test with more than one join
					if (join.getOnExpression() != null)
						parseJoinONToFilter(join.getOnExpression().toString());
				}
			}

			// ----------- WHERE ... -----------
			whereClause = plainSelect.getWhere();
			// ----------- ORDER BY ... -----------
			// TODO: handle inner queries in order by
			if (plainSelect.getOrderByElements() != null) {
				for (OrderByElement obe : plainSelect.getOrderByElements()) {
					String col = obe.toString();
					System.out.println("Col2: " +col);
					if (col.contains(UI_taint_marker)) {
						String new_col = col.replace(UI_taint_marker, CN_taint_marker);
						new_cq = new_cq.replace(col, new_col);
						UI_ColNames_labeled.add(new_col);
						UI_ColNames.add(col.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
					} else {
						SchemaColumns.add(col.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
					}
				}
			}
			// TODO: INTERSECT, GROUPBY, MINUS
			if (plainSelect.getGroupBy() != null) {

				if (plainSelect.getGroupBy().getGroupingSets() != null) {
					List<GroupByElement> gbl = plainSelect.getGroupBy().getGroupingSets();
					for (GroupByElement gbe : gbl) {
						String col = gbe.toString();
						System.out.println("Col3: " +col);
						if (col.contains(UI_taint_marker)) {
							String new_col = col.replace(UI_taint_marker, CN_taint_marker);
							new_cq = new_cq.replace(col, new_col);
							UI_ColNames_labeled.add(new_col);
							UI_ColNames.add(col.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
						} else {
							SchemaColumns.add(col.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
						}
					}
				}
			}

//	=============================== INSERT ===============================
		} else if (cq.trim().toLowerCase().startsWith("insert")) {

			Insert insertStatement = (Insert) CCJSqlParserUtil.parse(cq);
			if (insertStatement.getSelect() != null) {
				// TODO: handle INSERT INTO .. SELECT ...
				// I need to use recursive
				String temp_query = new_cq;
				String new_select = classify_userinput(insertStatement.getSelect().toString());
				Select InsertSelectStatement = (Select) CCJSqlParserUtil.parse(new_select);
				insertStatement.setSelect(InsertSelectStatement);
				new_cq = insertStatement.toString();
//				System.out.println(new_cq);
			}

			tableList = tablesNamesFinder.getTableList(insertStatement);
//			System.out.println("tableList: " + tableList);
//			System.out.println("getColumns: " + insertStatement.getColumns());
			if (insertStatement.getColumns() != null) {
				for (Column c : insertStatement.getColumns()) {
					String col = c.getColumnName();
//				System.out.println("col: " + col);
					if (col.contains(UI_taint_marker)) {
						String new_col = col.replace(UI_taint_marker, CN_taint_marker);
						new_cq = new_cq.replace(col, new_col);
						UI_ColNames_labeled.add(new_col);
						UI_ColNames.add(col.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
					} else {
						SchemaColumns.add(col.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
					}
				}
			}

			if (insertStatement.getItemsList() != null) {
				if (insertStatement.getItemsList() instanceof ExpressionList) {

					ExpressionList list = (ExpressionList) insertStatement.getItemsList();
					for (Expression ex : list.getExpressions()) {
						String CV = ex.toString();
						if (CV.contains(UI_taint_marker)) {
//							Columns_values.add(CV.replace(UI_taint_marker1, ""));
//							CValues_labeled.add(CV);
							String new_CV = CV.replace(UI_taint_marker, CV_taint_marker).replace(LSbracket_temp,"[").replace(RSbracket_temp, "]");
							UI_ColValues.add(CV.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
							new_cq = new_cq.replace(CV, new_CV);
							UI_ColValues_labeled.add(new_CV);
						}
//						System.out.println(ex);
					}
//					System.out.println(list.getExpressions().get(1).toString());
				}
			}

//			=============================== UPDATE ===============================
		} else if (cq.trim().toLowerCase().startsWith("update")) {

			Update updateStatement = (Update) parserManager.parse(new StringReader(cq));
//			System.out.println("UPDATE:");
//			Update updateStatement = (Update) CCJSqlParserUtil.parse(cq);

			tableList = tablesNamesFinder.getTableList(updateStatement);

			// Columns in SET
//			System.out.println("updateStatement: " + updateStatement.getColumns());
			for (Column c : updateStatement.getColumns()) {
				String col = c.getColumnName();
//				System.out.println("col: " + col);
				if (col.contains(UI_taint_marker)) {
					String new_col = col.replace(UI_taint_marker, CN_taint_marker);
					new_cq = new_cq.replace(col, new_col);
					UI_ColNames_labeled.add(new_col);
					UI_ColNames.add(col.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
				} else {
					SchemaColumns.add(col.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
				}

			}
			// Column values in SET
//			System.out.println("getExpressions: " + updateStatement.getExpressions());
			for (Expression ex : updateStatement.getExpressions()) {
				String CV = ex.toString();
				if (CV.contains(UI_taint_marker)) {
//					Columns_values.add(CV.replace(UI_taint_marker1, ""));
//					CValues_labeled.add(CV);
					String new_CV = CV.replace(UI_taint_marker, CV_taint_marker).replace(LSbracket_temp,"[").replace(RSbracket_temp, "]");
					UI_ColValues.add(CV.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
					new_cq = new_cq.replace(CV, new_CV);
					UI_ColValues_labeled.add(new_CV);
				}
			}

			whereClause = updateStatement.getWhere();

//			=============================== DELETE ===============================
		} else if (cq.trim().toLowerCase().startsWith("delete")) {
//			System.out.println("############# .... D");
			Delete deleteStatement = (Delete) CCJSqlParserUtil.parse(cq);
//			Delete delete = (Delete) parserManager.parse(new StringReader(cq));
			tableList = tablesNamesFinder.getTableList(deleteStatement);
			System.out.println("############# TL "+tableList.toString());
			whereClause = deleteStatement.getWhere();

		} else {
			// Handling DDL queries
			
			// WL is the solution
//			create_whitelist_sol(lq);
//			System.out.println("############# create_whitelist_sol_ddl, " + cq);
//			create_whitelist_sol_ddl(cq);
		}

//		System.out.println("Tables in From part:" + tableList);
		for (String table : tableList) {
			if (table.startsWith(UI_taint_marker)) {
				String temp = table;
				UI_Tables.add(temp.replace(UI_taint_marker, ""));
				UI_Tables_labeled.add(temp.replace(UI_taint_marker, TN_taint_marker));
				new_cq = new_cq.replace(table, table.replace(UI_taint_marker, TN_taint_marker));
			} else {
				SchemaTables.add(table);
			}
		}

		if (whereClause != null) {
//			System.out.println("Where part:" + whereClause);
			parseWhereClauseToFilter(whereClause.toString());
		}
		// TODO: Check if the new_cq still have UI_taint_marker1
//		try {
		if (new_cq.contains(UI_taint_marker)) {
			SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(new_cq);

			node.jjtAccept(new CCJSqlParserDefaultVisitor() {
				@Override
				public Object visit(SimpleNode node, Object data) {

					if (node.jjtGetValue() != null) {
						String ui_node = node.jjtGetValue().toString();
//						System.out.print("\n -- " + node.toString() + " : " + ui_node Where part:);
						if (node.getId() == CCJSqlParserTreeConstants.JJTCOLUMN && ui_node.contains(UI_taint_marker)) {
							String new_CV = ui_node.replace(UI_taint_marker, CV_taint_marker).replace(LSbracket_temp,"[").replace(RSbracket_temp, "]");
							UI_ColValues.add(ui_node.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
							new_cq = new_cq.replace(ui_node, new_CV);
							UI_ColValues_labeled.add(new_CV);

						}
					}
					return super.visit(node, data);
				}
			}, null);
		}
	} catch (Exception e) {
		// System.out.println("CCJSqlParserUtil had an issue.");
		ParserFail = true;
	}
		String new_query = new_cq;
		return new_query;

	}

	public static void handle_inner_query(String inner_query, String CV) {
//		System.out.println(" ===== " + CV);
//	 	String inner_query = CV.trim();
		if (inner_query.startsWith("(")) {
			inner_query = inner_query.substring(1);
		}
		if (inner_query.endsWith(")")) {
			inner_query = inner_query.substring(0, inner_query.length() - 1);
		}
//		node.jjtGetFirstToken().toString()
		// TODO: recursive to handle nested select
		String original_query = new_cq;
		String new_select = "";
		try {
			new_select = classify_userinput(inner_query);
//			System.out.println("new_select: " + new_select);
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(new_cq);
		new_cq = original_query.replace(CV, CV.replace(inner_query, new_select));

	}

	// Ref:
	// https://stackoverflow.com/questions/46800058/fully-parsing-where-clause-with-jsqlparser
	public static void parseWhereClauseToFilter(String whereClause) {

		try {
			Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause);
			FilterExpressionVisitorAdapter adapter = new FilterExpressionVisitorAdapter();
			expr.accept(adapter);

			// We assume in WHERE clause left is Column name and right is Column value

//			System.out.println("++++ Left Cols Names: " + adapter.LeftColumns);
			for (String CN : adapter.LeftColumns) {
				if (CN.contains(UI_taint_marker)) {
					// Update the query with the new label
					String new_CN = CN.replace(UI_taint_marker, CN_taint_marker);
					UI_ColNames_labeled.add(new_CN);
					new_cq = new_cq.replace(CN, new_CN);
					UI_ColNames.add(CN.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));

				} else {
					SchemaColumns.add(CN.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
				}

			}
//			System.out.println("++++ Right Col Values: " + adapter.RightColumns);

			for (String CV : adapter.RightColumns) {
//				System.out.println("RightColumns: " + CV);
//				if (CV.trim().toLowerCase().startsWith("like")) {
//					//%or%
//					String pattern = CV.substring("like".length()).trim();
//					if(pattern.startsWith("%"+UI_taint_marker1) || pattern.endsWith(UI_taint_marker1+"%") ) {
//							// CV.contains(UI_taint_marker1)
//							Columns_values.add(CV.replace(UI_taint_marker1, "").replace("%", ""));
//							CValues_labeled.add(CV);
//					}
//					
//				}else
				//TODO: handle fixed RHS. It could be column name
				if (CV.trim().startsWith(UI_taint_marker)) { 
					// CV.contains(UI_taint_marker1)
					String new_CV = CV.replace(UI_taint_marker, CV_taint_marker).replace(LSbracket_temp,"[").replace(RSbracket_temp, "]");
					UI_ColValues.add(CV.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
					new_cq = new_cq.replace(CV, new_CV);
					UI_ColValues_labeled.add(new_CV);
				
				} else {
					// Handeling select (in where clause) after = || ANY || ALL

					String inner_query = CV.trim();

					String[] starts = { "any(select", "any (select", "any( select", "any ( select", "all(select",
							"all (select", "all( select", "all ( select", "(select", "( select", "select" };
					// System.out.println(myStr.indexOf("planet"));

					for (String start : starts) {
						if (inner_query.toLowerCase().startsWith(start)) {
//						System.out.println("---- inner_query.indexOf \"" +start +"\" : " +inner_query.toLowerCase().split(start)[0]);
//						inner_query.re//.replace(0, start.length()-1, "");
							String str = start.replace("select", "");
							inner_query = inner_query.substring(str.length());
//							if (inner_query.endsWith(")")) {
//								inner_query = inner_query.substring(0, inner_query.length() - 1);
//							}
//							System.out.println("---- inner_query = " + inner_query);
							handle_inner_query(inner_query, CV);
							break;
						}
					}
				}


			}

		} catch (JSQLParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void parseJoinONToFilter(String whereClause) {

		try {
			Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause);
			FilterExpressionVisitorAdapter adapter = new FilterExpressionVisitorAdapter();
			expr.accept(adapter);

			// We assume in JOIN both sides left and right are Columns names
//			System.out.println("++++ Left Cols Names: " + adapter.LeftColumns);
			for (String CN : adapter.LeftColumns) {
				if (CN.contains(UI_taint_marker)) {
					// Update the query with the new label
					String new_CN = CN.replace(UI_taint_marker, CN_taint_marker);
					UI_ColNames_labeled.add(new_CN);
					new_cq = new_cq.replace(CN, new_CN);
					UI_ColNames.add(CN.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));

				} else {
					SchemaColumns.add(CN.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
				}

			}

//			System.out.println("++++ Right Col Values: " + adapter.RightColumns);
			for (String CN : adapter.RightColumns) {
				if (CN.contains(UI_taint_marker)) {
					// Update the query with the new label
					String new_CN = CN.replace(UI_taint_marker, CN_taint_marker);
					UI_ColNames_labeled.add(new_CN);
					new_cq = new_cq.replace(CN, new_CN);
					UI_ColNames.add(CN.replace(UI_taint_marker, "").replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));

				} else {
					SchemaColumns.add(CN.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]"));
				}

			}

		} catch (JSQLParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	public static void create_whitelist_sol_ddl(String lqddl) {
		//UI_taint_marker
		//System.out.println("############# create_whitelist_sol_ddl, " + lqddl);
		String lq2 = lqddl;
		String [] parts = lqddl.split(UI_taint_marker);
		for (int i =0 ; i< parts.length;i++) {
			if (i%2 ==1) {
				lq2 = lq2.replace(UI_taint_marker+parts[i].trim()+UI_taint_marker, "\" + String.valueOf(" +parts[i].trim()+ ")" + CV_Whitelist);
			}
		}
		WL_query = "\"" + lq2;
		WL_query = WL_query.replace("\\\"", "\\#");
		int count = WL_query.length() - WL_query.replace("\"", "").length();
		if (count % 2 > 0)
			WL_query = WL_query + "\"";

		WL_query = WL_query.replace("\\#", "\\\"");
		// WL_query = WL_query.replace("#", "\"");//====> didn't work
//				System.out.println("@@@@@@@@ WL_query: "+WL_query);
		for (MappingInfo m : mappingWL) {
//					System.out.println("@@@@@@@@  m.temp: "+m.temp+" m.call: " + m.call);
			if (WL_query.contains(m.temp.trim()))
				WL_query = WL_query.replace(m.temp.trim(), m.call);

		}
		WL_query = WL_query.replace("\" \"", "''");// +++++ Added on Feb/14
		//System.out.println("############# create_whitelist_sol_ddl, " + WL_query);
	}
	public static void create_whitelist_sol(String lq) {
		
//System.out.println("create_whitelist_sol 1");

		// Replace Tables names with WL code
		for (String table : UI_Tables_labeled) {
			if (lq.endsWith(table)) {
				// No need to use String.valueOf because Table name is always a string
				lq = lq.replace(table, ("\" + " + table.replace(TN_taint_marker, "") + TN_CN_Whitelist));
			} else {
				lq = lq.replace(table, ("\" + " + table.replace(TN_taint_marker, "") + TN_CN_Whitelist + " + \""));
			}
		}
		// Replace Columns names with WL code
		for (String cname : UI_ColNames_labeled) {
			// No need to use String.valueOf because Column name is always a string
			if (lq.endsWith(cname))
				lq = lq.replace(cname, ("\" + " + cname.replace(CN_taint_marker, "") + TN_CN_Whitelist));
			else
				lq = lq.replace(cname, ("\" + " + cname.replace(CN_taint_marker, "") + TN_CN_Whitelist + " + \""));
		}
		// Replace Columns values with WL code, we use "String.valueOf" in case other
		// data type is used.

		for (String cvalue : UI_ColValues_labeled) {
//			System.out.println("w/ like : " + cvalue);
			String Slike = "", Elike = "", Replace = "";
			if (cvalue.contains(LIKE_symbol)) {
				if (cvalue.replace(CV_taint_marker, "").startsWith(LIKE_symbol)
						&& cvalue.replace(CV_taint_marker, "").endsWith(LIKE_symbol)) {
					Slike = "'%";
					Elike = " + \"%'\"";
				} else if (cvalue.replace(CV_taint_marker, "").endsWith(LIKE_symbol)) {
					Slike = "'";
					Elike = " + \"%'\"";
				} else if (cvalue.replace(CV_taint_marker, "").startsWith(LIKE_symbol)) {// *
					Slike = "'%";
					Elike = " + \"'\"";
				}

				Replace = Slike + "\" + String.valueOf(" + cvalue.replace(CV_taint_marker, "").replace(LIKE_symbol, "")
						+ ")" + CV_Whitelist + Elike;
			} else {
				Replace = "\" + String.valueOf(" + cvalue.replace(CV_taint_marker, "") + ")" + CV_Whitelist;
			}

//			System.out.println("w/o like : " + cvalue);
			if (lq.endsWith(cvalue))
				lq = lq.replace(cvalue, Replace);
//						"\" + String.valueOf(" + cvalue.replace(UI_taint_marker1, "") + ")" + CV_Whitelist);
			else
				lq = lq.replace(cvalue, Replace + " + \"");
//						"\" + String.valueOf(" + cvalue.replace(UI_taint_marker1, "") + ")" + CV_Whitelist + " + \"");
		}
		
//		if(ParserFail) {
		if(UI_taint_marker.contains(UI_taint_marker) || ParserFail) {
//			System.out.println("create_whitelist_sol - ParserFail"); 
			//System.out.println("ParserFail!!!");
//			Replace = "\" + String.valueOf(" + cvalue.replace(CV_taint_marker, "") + ")" + CV_Whitelist;
			String parts[] = lq.split(UI_taint_marker);//("\\s+");
			int i =0;
			for(String p: parts) {
//				System.out.println("------ "+p);
				if (i % 2 == 1) {
//					if(p.startsWith(UI_taint_marker)) {
					String tmp = UI_taint_marker + p+ UI_taint_marker;
					//System.out.println("------>> "+tmp);
						String new_p= "\" + String.valueOf(" + p + ")" + TN_CN_Whitelist;
						//System.out.println("------ "+p);
						lq = lq.replace(tmp, new_p) ;
//					}
				}
				i++;
			}
			
		}
		//System.out.println("##### "+lq);
		WL_query = "\"" + lq;
		WL_query = WL_query.replace("\\\"", "\\#");
		int count = WL_query.length() - WL_query.replace("\"", "").length();
		if (count % 2 > 0)
			WL_query = WL_query + "\"";

		WL_query = WL_query.replace("\\#", "\\\"");//=============== I commented this on FEb/14 to handle ` and \" 
		//WL_query = WL_query.replace("#", "\"");//====> didn't work
//		System.out.println("@@@@@@@@ WL_query: "+WL_query);
		for(MappingInfo m:mappingWL ) {
//			System.out.println("@@@@@@@@  m.temp: "+m.temp+" m.call: " + m.call);
			if(WL_query.contains(m.temp.trim()))
				WL_query = WL_query.replace(m.temp.trim(), m.call);
			
		}
//		System.out.println("@@@@@@@@ WL_query: "+WL_query);
		WL_query = WL_query.replace("\" \"", "''");// +++++ Added on Feb/14
	}

	public static boolean all_query_is_user_input(String query, List<String> uiVar) {
		String[] parts = query.split(".");
		if(parts.length>1) {
			if((parts[parts.length-1].equals("nextLine()") || parts[parts.length-1].equals("next()") 
					||parts[parts.length-1].equals("nextFloat()") ||parts[parts.length-1].equals("nextDouble()") 
					||parts[parts.length-1].equals("nextInt()") ||parts[parts.length-1].equals("nextLong()") ) 
//					|| uiVar.contains(parts[0]) || uiVar.contains(parts[parts.length-2]) 
					){
				return true;
		}else 
		if(uiVar!=null){
				if(uiVar.contains(parts[0]) || uiVar.contains(parts[parts.length-2]))
					return true;
			}
		}
		return false;
	}
	public static void clear_all_values(String query) {
		SchemaTables.clear();
		UI_Tables.clear();
		UI_Tables_labeled.clear();
		SchemaColumns.clear();
		UI_ColNames.clear();
		UI_ColNames_labeled.clear();
		UI_ColValues.clear();
		UI_ColValues_labeled.clear();
		set_strings.clear();
		PS_query = "";
		WL_query = "";
		prefered_sol = 0;
		original_query = query;
		new_cq = "";
		qs_ql = new Sig();// +++
		mappingWL.clear();
		c =0;
		allHasReplace =false;
		msg = "";
		ParserFail = false;
	}
	//TODO: uiVar has to contain all UI vars and function argumnets!
	public static void parse_query(String query, boolean print, List<String> uiVar) throws JSQLParserException, IOException {
//		if (print) System.out.println("----- In parse_query");
		clear_all_values(query);
		
		// ====================================
		// ----------------- Start: 9/27th/22 I commented next part 
//		if(all_query_is_user_input(query, uiVar)) {
		if(uiVar!= null) {
		if(uiVar.size()>0) {
			prefered_sol = G.NOSOL;
			msg = G.MSG_UsePS_all;
			List<Character> I = new ArrayList<>();
			I.add('a');
			qs_ql.setvalues('Q', '_', I);
			return;
		}}
		
		// ----------------- end: 9/27th/22 I commented prev. part
		/* fixing steps */ if (print)
			System.out.println("1# Query before cleaning:\n" + query);
		String cq_ = QParser_utilities.prepare_appended_query(query, print, LIKE_symbol);// always return clean query
														// with ' around user input
//		/* fixing steps */ if (print)
//			System.out.println("cq_:"+cq_);
		String[] parts = cq_.split("'");
		int i = 0, p=0;
		for (String part : parts) {
			if (p % 2 == 1) {
				//System.out.println("1'#%%%%%%%%" + part);
				String tn_cn_wl,cv_wl;
				// ----------------- Start: 9/27th/22 I commented next 2 lines
//				tn_cn_wl ="colName.replaceAll(\"[^a-zA-Z0-9-\"_\"]\", \"\")";
//				cv_wl ="colval.replaceAll(\"[^a-zA-Z0-9-\"_\"-\" \"]\", \"\")";
				// ----------------- end: 9/27th/22 I commented prev. 2 lines
				tn_cn_wl =".replaceAll(\"[^a-zA-Z0-9-\"_\"]\", \"\")";
				cv_wl =".replaceAll(\"[^a-zA-Z0-9-\"_\"-\" \"]\", \"\")";

//				TN_CN_Whitelist
//				if (part.contains(tn_cn_wl) 
//						|| part.contains(cv_wl)) { // ---- commented on 9/27th/22
				if (part.contains(tn_cn_wl) 
						|| part.contains(cv_wl) ||part.contains(TN_CN_Whitelist) 
						|| part.contains(CV_Whitelist)) {
					i++;
					MappingInfo m = new MappingInfo();
					m.call = part;
					// ---- commented on 9/27th/22 // 
					//m.call = part.replace(tn_cn_wl, TN_CN_Whitelist).replace(cv_wl, CV_Whitelist);
					m.temp = "Temp_" + System.currentTimeMillis() + c++;
					m.callIsReplace = true;
					mappingWL.add(m);
					cq_ = cq_.replace(part, m.temp);

				} else if ((part.contains("(") && part.contains(")")) || part.contains(".")) {// Handling other calls
					MappingInfo m = new MappingInfo();
					m.call = part;
					m.temp = "T_" + System.currentTimeMillis() + c++;
					m.callIsReplace = false;
					mappingWL.add(m);
					cq_ = cq_.replace(part, m.temp);
				}
			}
			p++;
		}
		if(i == parts.length/2) {
			allHasReplace = true;
		}
		
		//+++++++++++++++++++++++++++++++  Start 10/11th
		if(cq_.contains("\\\"'"))
			cq_ = cq_.replace("\\\"\'", "'");
		if(cq_.contains("'\\\""))
			cq_ = cq_.replace("\'\\\"", "'");
		//+++++++++++++++++++++++++++++++ End 10/11th

				
		/* fixing steps */ if (print)
			System.out.println("1'# Handeling calls:\n" + cq_);
		cq_ = cq_.replace("\\\"", "\"");// ++++  Added on Feb/14
		String cq_marked = cq_.replace("\'", UI_taint_marker);
		cq_marked = cq_marked.replace("[", LSbracket_temp).replace("]", RSbracket_temp);
		
		
		/* fixing steps */ if (print)
			System.out.println("2# Query prepared to label UI:\n" + cq_);
		/* fixing steps */ if (print)
			System.out.println("2'# Labeled Query prepared for cleaning:\n" + cq_marked);

//		TODO: use this function to label mark tables and columns
//				QParser_utilities.classified_query(cq_marked);

		String lq = classify_userinput(cq_marked);
		lq = lq.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]");
//		String lq = classify_userinput(query);// ??? test insert to 
		/* fixing steps */ if (print)
			System.out.println("3# Labeled Query based on type of UI:\n" + lq);

//		Sig qs_ql = create_query_sig(lq);
		
		qs_ql = create_query_sig(lq);
		/* fixing steps */ if (print)
			System.out.println("4# Query Sig from qs_ql:" + qs_ql.toString());
//		/* fixing steps */ if(print) System.out.print("// *** Passed {" + qs_ql.toString() + "}");
		/* fixing steps */ if (print)
			System.out.println("" + cq_);

		cq_ = cq_.replace(LSbracket_temp,"[").replace(RSbracket_temp, "]");
//		if (qs_ql.toString().contains("v") && !qs_ql.toString().contains("t") && !qs_ql.toString().contains("c")) {
		 if (!qs_ql.get_I().contains('t') && !qs_ql.get_I().contains('c')&& qs_ql.get_I().contains('v') ) {
			
			String clean_q = ReplaceColumnValues.cleanStatement(cq_);
			
			if(clean_q ==null) {
//				qs_ql.setvalues('F', '_', null);// ---- commented on 9/27th/22
				ParserFail = true;
				prefered_sol = G.SOLWL;
//				String temp = cq_.toLowerCase().trim();
				if ((cq_.split("\\s+"))[0].equalsIgnoreCase("CREATE")
						|| (cq_.split("\\s+"))[0].equalsIgnoreCase("ALTER")
						|| (cq_.split("\\s+"))[0].equalsIgnoreCase("TRUNCATE")
						|| (cq_.split("\\s+"))[0].equalsIgnoreCase("DROP"))
					msg = G.MSG_WLSol +" It is a DDL command."; 

				else
					msg = "Prefered solution is PS, but in this case, parser failed to build clean query for PS.";
//				return ;
			}else {
				prefered_sol = G.SOLPS;
				msg = G.MSG_PSSol;
			}
			if(!ParserFail){// ----- Added it on 9/27th/22	
			PS_query = "\"" + clean_q + "\"";
			PS_query = PS_query.replace("\" \"", "''"); // +++++ Added on Feb/14
//			output_query = "\"" + ReplaceColumnValues.cleanStatement(lq) + "\"";//???????? change back to the previus one
			// *fixing steps*/System.out.println("5# Query cleaned to be used with prepared
			// stmt: " + output_query);
			// Create the setObject value statement for the prepared statement
			// Use Columns_values & lq
			// ================= [
			//  Order the names of the columns values
			TreeMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
			for (String cvalue : UI_ColValues) {// cvalue
				// ===== Handle when the variable is used more than one time!
				int lastIndex = 0;
				if (QParser_utilities.countMatches(cq_, "'" + cvalue + "'") > 1) {
					if (print)
						System.out.println("countMatches > 1");

					while (lastIndex != -1) {

						lastIndex = cq_.indexOf("'" + cvalue + "'", lastIndex);

						if (lastIndex != -1) {
							sortedMap.put(lastIndex, cvalue);
							if (print)
								System.out.println("+++ key:" + cvalue + ", Value: " + lastIndex);
							lastIndex += ("'" + cvalue + "'").length();
						}
					}
				} else {
					lastIndex = cq_.indexOf("'" + cvalue + "'");
					if (lastIndex != -1)
						sortedMap.put(lastIndex, cvalue);
				}
			}
			int index = 1;
			for (Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
				if (print)
					System.out.println("Key: " + entry.getKey() + " , Value: " + entry.getValue());
				String cvalue = entry.getValue();
				String Slike = "", Elike = "";
				if (cvalue.startsWith(LIKE_symbol))
					Slike = "\"%\" + ";
				if (cvalue.endsWith(LIKE_symbol))
					Elike = " + \"%\"";
				set_strings.add(PS_place_holder + ".setObject(" + index + ", " + Slike + cvalue.replace(LIKE_symbol, "")
						+ Elike + ");");
				index++;
			}
//			
			for (int x = 0; x < set_strings.size(); x++) {
				String setStr = set_strings.get(x);
				for (MappingInfo m : mappingWL) {
					//System.out.println("@@@@@@@@ setStr: "+setStr +", m.temp: "+m.temp+" m.call: " + m.call);
					if (setStr.contains(m.temp)) {
						set_strings.set(x, setStr.replace(m.temp, m.call));
						break;
					}
				}
			}}
			// =================]
//			//=========== the method below to build setObjects worked fine but It's not guaranteed that the set values are sorted  
//			int index = 1;
//			for (String cvalue : Columns_values) {
//				String Slike = "", Elike = "";
//				if (cvalue.startsWith(LIKE_symbol))
//					Slike = "\"%\" + ";
//				if (cvalue.endsWith(LIKE_symbol))
//					Elike = " + \"%\"";
//				set_strings.add(PS_place_holder + ".setObject(" + index + ", " + Slike + cvalue.replace(LIKE_symbol, "")
//						+ Elike + ");");
//				index++;
//			}
//			//===========
			
			create_whitelist_sol(lq);

		} else if ((qs_ql.get_I().contains('t') || qs_ql.get_I().contains('c'))&& !qs_ql.get_I().contains('v')) {

			prefered_sol = G.SOLWL;
			
			
			if(allHasReplace) {
				prefered_sol = G.SEC;
				msg = G.MSG_WLWarning;
			} 
			else {
				if ((cq_.split("\\s+"))[0].equalsIgnoreCase("CREATE")
						|| (cq_.split("\\s+"))[0].equalsIgnoreCase("ALTER")
						|| (cq_.split("\\s+"))[0].equalsIgnoreCase("TRUNCATE")
						|| (cq_.split("\\s+"))[0].equalsIgnoreCase("DROP")) {
					create_whitelist_sol_ddl(lq);
					
					msg = G.MSG_WLSol + " It is a DDL command.";
				} else {
					msg = G.MSG_WLSol;
					create_whitelist_sol(lq);
				}
			}
			
			
//			create_whitelist_sol(lq);
		} else if ((qs_ql.get_I().contains('t') || qs_ql.get_I().contains('c'))&& qs_ql.get_I().contains('v')) {
			prefered_sol = G.NOSOL;
			if(allHasReplace) {
				msg = G.MSG_WLWarning+" However, it's not safe becaue column value and table/column name are user input. WL with column values is not practical.";
				if(print)
				System.out.print(msg);
			}else {
				
				msg = G.MSG_NoSol+" Column value and table/column name are user input";
				if(print)
					System.out.print(msg);
			}
		}
		 //System.out.print("HERE!!!");
		for (int x = 0; x < mappingWL.size(); x++) {
			MappingInfo mp = mappingWL.get(x);
//			for (String coln : UI_ColNames) {
			for (int y = 0; y < UI_ColNames.size(); y++) {
//				System.out.println("@@@@@@@@ setStr: "+setStr +", m.temp: "+m.temp+" m.call: " + m.call);
				String Col = UI_ColNames.get(y);
				String colEl = mp.temp; 
				if (colEl.equals(Col)) {
					MappingInfo mp_temp = mp;
					mp_temp.uit = 'c';
					mappingWL.set(x, mp_temp);
					UI_ColNames.set(y, Col.replace(mp.temp, mp.call));
					break;
				}
			}
		}
		
		for (int x = 0; x < mappingWL.size(); x++) {
			MappingInfo mp = mappingWL.get(x);
//			for (String coln : UI_ColNames) {
			for (int y = 0; y < UI_ColValues.size(); y++) {
//				System.out.println("@@@@@@@@ setStr: "+setStr +", m.temp: "+m.temp+" m.call: " + m.call);
				String ColVal = UI_ColValues.get(y);
				String colValEl = mp.temp;
				if (colValEl.equals(ColVal)) {
					MappingInfo mp_temp = mp;
					mp_temp.uit = 'v';
					mappingWL.set(x, mp_temp);
					UI_ColValues.set(y, ColVal.replace(mp.temp, mp.call));
					break;
				}
			}
		}
		
		for (int x = 0; x < mappingWL.size(); x++) {
			MappingInfo mp = mappingWL.get(x);
//			for (String coln : UI_ColNames) {
			for (int y = 0; y < UI_Tables.size(); y++) {
//				System.out.println("@@@@@@@@ setStr: "+setStr +", m.temp: "+m.temp+" m.call: " + m.call);
				String Tab = UI_Tables.get(y);
				String tabEl = mp.temp;
				if (tabEl.equals(Tab)) {
					MappingInfo mp_temp = mp;
					mp_temp.uit = 't';
					mappingWL.set(x, mp_temp);
					UI_Tables.set(y, Tab.replace(mp.temp, mp.call));
					break;
				}
			}
		}
		
		//UI_Tables
		if (print)
			print_parsing_result();
	}

	public static void print_parsing_result() {
//		System.out.println("Original query: " + original_query);
		System.out.println("Sig: " + qs_ql + ", Sol: " + prefered_sol);
		System.out.println("PS: " + PS_query);
		System.out.println("WL: " + WL_query);
//		System.out.println("=====================================");
		System.out.println(">>>>>  Original query " + original_query);
		System.out.println(">>>>>  Query Sig: " + qs_ql);
		System.out.println(">>>>>  Columns_names: " + UI_ColNames.toString());
		System.out.println(">>>>>  L Columns_names: " + UI_ColNames_labeled.toString());
		System.out.println(">>>>>  Var Tables: " + UI_Tables.toString());
		System.out.println(">>>>>  L Var Tables : " + UI_Tables_labeled.toString());
		System.out.println(">>>>>  Columns_values: " + UI_ColValues.toString());
		System.out.println(">>>>>  L Columns_values: " + UI_ColValues_labeled.toString());
		System.out.println(">>>>>  Set strings: " + set_strings.toString());
		System.out.println(
				"set_strings size:" + set_strings.size() + " , " + "#Columns_values: " + UI_ColValues.size());
		System.out.println(">>>>>  PS_query: " + PS_query);
		System.out.println(">>>>>  WL_query2: " + WL_query);
		System.out.println(">>>>>  Prefered sol: " + prefered_sol);
		if(msg != null)
			System.out.println(">>>>>  Message: " + msg);
		System.out.println("=====================================");
	}

	public static void main(String[] args) throws JSQLParserException, IOException {

		String Query_ = "SELECT * FROM LOCATIONS WHERE COUNTRY_ID = ANY( SELECT COUNTRY_ID FROM COUNTRIES WHERE COUNTRY_NAME LIKE _UI999_id1_UI999_);";

		Query_ = "SELECT * FROM locations WHERE COUNTRY_ID = ANY( 100,200,300,400)";
		Query_ = "SELECT * FROM locations WHERE id IN ( 100,200,300,400)";
		Query_ = "SELECT * From A Where id = ( Select id from B where bid = 100 )";
		Query_ = "SELECT * FROM a WHERE id = ANY( SELECT id FROM b );";
		Query_ = "SELECT * FROM LOCATIONS WHERE COUNTRY_ID = ANY( SELECT COUNTRY_ID FROM COUNTRIES WHERE COUNTRY_NAME LIKE '\" + id1 + \"%');"; // didn't
																																				// parse
		Query_ = " Select id from B where bid <> \"+ id + \";";
		Query_ = "Select * From offer where location=\" + offerLocation + \" order by customerId";
		// ==========================
		// Didn't pass
//		Query_ ="DELETE FROM EMPLOYEES WHERE FIRST_NAME=(SELECT FIRST_NAME FROM EMPLOYEES WHERE HIRE_DATE =TO_DATE(\" + DATE1 + \",'Month DD,YYYY') AND HIRE_DATE=TO_DATE(\" + DATE2 + \", '1998','Month DD,YYYY'))";
		// ==========================
		// Didn't pass -- extra )
		Query_ = "SELECT D.DEPARTMENT_ID, MAX(E.SALARY) FROM DEPARTMENTS D, EMPLOYEES E WHERE E.DEPARTMENT_ID = D.DEPARTMENT_ID AND E.LAST_NAME NOT LIKE _UI999_id1_1234__UI999_ GROUP BY D.DEPARTMENT_ID HAVING AVG(E.SALARY)) <> _UI999_id2_UI999_;\n";
		Query_ = "SELECT D.DEPARTMENT_ID, MAX(E.SALARY) FROM DEPARTMENTS D, EMPLOYEES E WHERE E.DEPARTMENT_ID = D.DEPARTMENT_ID AND E.LAST_NAME NOT LIKE '\" + id1 + \"%' GROUP BY D.DEPARTMENT_ID HAVING AVG(E.SALARY)) <> \" + id2 + \";";
		// passed after removing the extra )
		Query_ = "SELECT D.DEPARTMENT_ID, MAX(E.SALARY) FROM DEPARTMENTS D, EMPLOYEES E WHERE E.DEPARTMENT_ID = D.DEPARTMENT_ID AND E.LAST_NAME NOT LIKE _UI999_id1_1234__UI999_ GROUP BY D.DEPARTMENT_ID HAVING AVG(E.SALARY) <> _UI999_id2_UI999_;\n";
		Query_ = "SELECT D.DEPARTMENT_ID, MAX(E.SALARY) FROM DEPARTMENTS D, EMPLOYEES E WHERE E.DEPARTMENT_ID = D.DEPARTMENT_ID AND E.LAST_NAME NOT LIKE '%\" + id1 + \"' GROUP BY D.DEPARTMENT_ID HAVING AVG(E.SALARY) <> \" + id2 + \";";
		Query_ = "SELECT D.DEPARTMENT_ID, MAX(E.SALARY) FROM DEPARTMENTS D, EMPLOYEES E WHERE E.DEPARTMENT_ID = D.DEPARTMENT_ID AND E.LAST_NAME NOT LIKE '%\" + id1 + \"' GROUP BY D.DEPARTMENT_ID HAVING AVG(E.SALARY) <> \" + id2 + \";";
		// ==========================
//		Query_ = "DELETE FROM EMPLOYEES WHERE FIRST_NAME=(SELECT FIRST_NAME FROM EMPLOYEES WHERE HIRE_DATE =TO_DATE(\" + DATE1 + \",'Month DD,YYYY') AND HIRE_DATE=TO_DATE(\" + DATE2 + \", 1998','Month DD,YYYY'))";
//		Query_ = "DELETE FROM EMPLOYEES WHERE FIRST_NAME=(SELECT FIRST_NAME FROM EMPLOYEES WHERE HIRE_DATE =TO_DATE(\" + DATE1 + \",'Month DD,YYYY') AND HIRE_DATE=TO_DATE(\" + DATE2 + \", '1998','Month DD,YYYY'))";
		Query_ = "SELECT (FIRST_NAME || '  ' || LAST_NAME) NAME, TO_CHAR(HIRE_DATE,'ddth Month,YYYY') H_DATE FROM EMPLOYEES where (date-365)=\" + days";

		Query_ = " Select id from B where bid <> \"+ id1 + \" AND bid > \"+ id2 + \";";
		Query_ = " Select id from B where bid <> \"+ id + \" AND bid > \"+ id + \";";
		
		Query_ = Query_.replace("\u00a0", " ");
		
//		Query_ ="UPDATE EMPLOYEES SET DEPARTMENT_ID =\"+ id1()+\" WHERE EMPLOYEE_ID =\" +id2.get();";
//		Query_ = "\"Select * from ACCOUNTS where \" + colName9.replaceAll(\"[^a-zA-Z0-9-'_']\", \"\") + \"  = \"\n"
//				+ "					+ colval7.replaceAll(\"[^a-zA-Z0-9-'_'-' ']\", \"\") + \";\"";
//		Query_="\"UPDATE users \" + \"SET password = \"+passwordBean.getPassword1()+\", change_user_name = javauser, change_date = \"+GenericUtilities.getCurrentTimeStamp() + \"WHERE id = \"+userId;";
//		Query_="\"UPDATE users \" + \"SET password = \" + passwordBean.getPassword1() + \", change_user_name = javauser , change_date = \" + GenericUtilities.getCurrentTimeStamp() + \"WHERE id = \" + userId";
//		Query_="\"SELECT JOB_ID, MAX(DISTINCT SALARY), MIN(DISTINCT SALARY), \" + \"SUM(DISTINCT SALARY), COUNT(DISTINCT DEPARTMENT_ID) \" + \"FROM EMPLOYEES \" + \"WHERE DEPARTMENT_ID < \" + id + \"GROUP BY JOB_ID \" + \"ORDER BY JOB_ID DESC\"";
//		Query_="\"SELECT 'DROP TABLE ' || owner || '.\\\"' || table_name || '\\\" CASCADE CONSTRAINTS' \" +\n\"FROM all_tables \" +\n\"WHERE owner = sys_context('USERENV', 'SESSION_USER')\" +\n\"      AND tablespace_name NOT IN ('SYSAUX')\" +\n\"      AND global_stats = 'NO'\" +\n\"      AND table_name NOT LIKE 'DEF$\\\\_%' ESCAPE '\\\\'\" +\n\" UNION ALL \" +\n\"SELECT 'DROP SEQUENCE ' || sequence_owner || '.' || sequence_name FROM all_sequences WHERE sequence_owner = sys_context('USERENV', 'SESSION_USER') and sequence_name not like 'ISEQ$$%'\"";
//		Query_="\"SELECT 'DROP TABLE ' || owner || '.\\\"' || table_name || '\\\" CASCADE CONSTRAINTS' \" +\n\"FROM all_tables \" +\n\"WHERE owner = '\" + schemaName + \"'\" +\n\"      AND tablespace_name NOT IN ('SYSAUX')\" +			\n\"      AND global_stats = 'NO'\" +\n\"      AND table_name NOT LIKE 'DEF$\\\\_%' ESCAPE '\\\\'\" +\n\" UNION ALL \" +\n\"SELECT 'DROP SEQUENCE ' || sequence_owner || '.' || sequence_name FROM all_sequences WHERE sequence_owner = '\" + schemaName + \"'\"";
//		Query_="\"ALTER DATABASE \" + dbName + \" SET SINGLE_USER WITH ROLLBACK IMMEDIATE\"";
//		Query_="\"USE \" + dbName";
//		Query_="\"ALTER TABLE \" + table + \" ADD \" + cols[i] + \" \" + newColumnDefaultType";
//		Query_="   query = \"select p.name as patient_name, p.surname as patient_surname, a.t as date\\n\" + \"from appointments a, doctors d, patients p\\n\" + \"where a.patientamka=p.patientamka and a.doctoramka=d.doctoramka\\n\" + \"and a.t>(select date(max(t))from appointments) - integer '30'\\n\" + \"--and a.t>current_date-30\\n\" + \"and d.doctoramka='\" + amka + \"'\"\n"
//				+ "";
//		
//		Query_= "\"SELECT username, user_id FROM logins where x = \"ttt\" AND username=\" + \"'\" + username + \"'\"";
//		Query_= "\"SELECT username, user_id FROM logins where x = 'ttt' AND username=\" + \"'\" + username + \"'\"";
//		Query_="\"update produces set price =\" + iprice.getText() + \" where item_code =\" + id + \" and id = \" + vid";
//		Query_="\"SELECT * FROM table \" + buildWhere(id)";
//		   // To test: "select * from keywordsr where keywordname like '%" + search_text + "%'"
//		Query_= "scanner.next()";
//		Query_ = "select count (*) from x;";
//		Query_="SELECT count(*) as c FROM pg_tables WHERE schemaname='public';";
//		Query_= "\"insert into gz_second_house(`property`,`area`,`section`,`address`,`layout`,`price`,`average_price`,`contact`,`identity`,`phone`,`website`,`atime`) values(\" + \"'\" + resultItems.get(\"property\") + \"',\" + \"'\" + resultItems.get(\"area\") + \"',\" + \"'\" + resultItems.get(\"section\") + \"',\" + \"'\" + resultItems.get(\"address\") + \"',\" + \"'\" + resultItems.get(\"layout\") + \"',\" + \"'\" + resultItems.get(\"price\") + \"',\" + \"'\" + resultItems.get(\"average_price\") + \"',\" + \"'\" + resultItems.get(\"contact\") + \"',\" + \"'\" + resultItems.get(\"identity\") + \"',\" + \"'\" + resultItems.get(\"phone\") + \"',\" + \"'\" + resultItems.get(\"website\") + \"',\" + \"'\" + resultItems.get(\"atime\") + \"'\" + \")\"";	
//		Query_="\"insert into gz_second_house(`property`,`area`,`section`,`address`,`layout`,`price`,`average_price`,`contact`,`identity`,`phone`,`website`,`atime`) values(\" + \"'\" + resultItems.getProperty() + \"',\" + \"'\" + resultItems.getArea() + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + resultItems.getWebsite() + \"',\" + \"'\" + sdf.format(new Date()) + \"'\" + \")\"";
//		Query_="\"UPDATE Diagnosis\\n\" + \"Set 	patientId = \" + item.getPatientId() + \"\\n\" + \",		details = \\\"\" + item.getDetails() + \"\\\"\" + \"\\n\" + \",		comments = \\\"\" + item.getComments() + \"\\\"\" + \"\\n\" + \" ,		date = \\\"\" + item.getDiagnosisId() + \"\\\"\" + \"\\n\" + \"Where diagnosisId = \" + item.getDiagnosisId()";
//		Query_="\"UPDATE Guest SET \" + \"guestType= '\" + guest.getGuestType() + \"', \" + \"travelAgency=  '0' \" + \"WHERE personId= '\" + guest.getId() + \"'\"\n";
		Query_="\"insert into gz_second_house(`property`,`area`,`section`,`address`,`layout`,`price`,`average_price`,`contact`,`identity`,`phone`,`website`,`atime`) values(\" + \"'\" + resultItems.getProperty() + \"',\" + \"'\" + resultItems.getArea() + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + \"\" + \"',\" + \"'\" + resultItems.getWebsite() + \"',\" + \"'\" + sdf.format(new Date()) + \"'\" + \")\"";
		Query_="\"UPDATE User\" + \"Set 	name = \\\"\" + item.getName() + \"\\\"\" + \",		email = \\\"\" + item.getEmail() + \"\\\"\" + \"	,	phone = \\\"\" + item.getPhone() + \"\\\"\" + \"		,role = \\\"\" + item.getRole() + \"\\\"\" + \"Where userId = \" + item.getUserId()";
		Query_="\"INSERT INTO Team(teamId, leaderId, numberOfParticipants) VALUES ('\" + nextTeamId + \"','\" + guestObj.getId() + \"','\" + team.getNumberOfParticipants() + \"')"; 
		Query_=		"INSERT INTO TeamParticipants(teamId, participantId) VALUES ('\" + nextTeamId + \"','\" + guestObj.getId() + \"')\"";
		Query_="\"SELECT COUNT(*) AS activityLineInstances3 FROM ActivityLine \" + \" WHERE activityId= '\" + activityId + \"' \" + \"AND date= '\" + date + \"' \" + \"AND startHour= '\" + startHour + \"' \" + \"AND facilityId= '\" + facilityId + \"' \" + \"AND status<>'Canceled' \"";
		Query_="\"SELECT COUNT(*) AS activityLineInstances3 FROM ActivityLine \" + \" WHERE bookingId= '\" + bookingId + \"' \" + \"AND date= '\" + date + \"' \" + \"AND startHour= '\" + startHour + \"' \" + \"AND startHour= '\" + startHour + \"' \" + \"AND status<>'Canceled' \"";
		Query_="\"INSERT INTO update_node_table VALUES(\" + \"'\" + domain + \"')\"";
		Query_="\"DELETE FROM update_node_table WHERE domain='\" + domain + \"'\"";
		Query_="\"select * from practica7.SOCI, practica7.PERSONA WHERE practica7.SOCI.ID = practica7.PERSONA.ID AND practica7.PERSONA.Llinatge1 LIKE '%\" + llinatge + \"%';\"";
		Query_="\"SELECT * FROM virtual_machine_host_table WHERE hostname='\" + rs.getString(\"host\") + \"'\"";			
		Query_="\"select * from practica7.SOCI, practica7.PERSONA WHERE practica7.SOCI.ID = practica7.PERSONA.ID AND practica7.PERSONA.Llinatge1 LIKE '%\" + llinatge + \"%';\"";
		Query_="\"insert into gz_second_house(`property`,`area`,`section`,`address`,`layout`,`price`,`average_price`,`contact`,`identity`,`phone`,`website`,`atime`) values(\" + \"'\" + resultItems.get(\"property\") + \"',\" + \"'\" + resultItems.get(\"area\") + \"',\" + \"'\" + resultItems.get(\"section\") + \"',\" + \"'\" + resultItems.get(\"address\") + \"',\" + \"'\" + resultItems.get(\"layout\") + \"',\" + \"'\" + resultItems.get(\"price\") + \"',\" + \"'\" + resultItems.get(\"average_price\") + \"',\" + \"'\" + resultItems.get(\"contact\") + \"',\" + \"'\" + resultItems.get(\"identity\") + \"',\" + \"'\" + resultItems.get(\"phone\") + \"',\" + \"'\" + resultItems.get(\"website\") + \"',\" + \"'\" + resultItems.get(\"atime\") + \"'\" + \")\"";
		Query_="\"INSERT INTO job_file_table VALUES(\" + \"'\" + f.getName() + \"',\" + \"'\" + job.getName() + \"',\" + \"'\" + f.getAbsolutePath() + \"',\" + \"\" + job.getFileType(job.getFileIndex(f.getName())) + \",\" + \"\" + job.getFileStatus(job.getFileIndex(f.getName())) + \")\"";
			Query_="Integer.toString(bid) + \";\"";
			Query_="\"select * from customers where cus_id  = '\" +answer+\"'\";"; //Parser failed to clean or parse!!!!
		Query_="\"DROP TABLE\" + tName";
		Query_="\"select name,type from items where item_code = \" + item_code[i]";
//		Query_="\"select name,type from items where  \" + item_code[i] + \" = 'item_code'\"";
		///=================== Cases to consider
		Query_= "\"ALTER TABLE \" + table + \" ADD \" + cols[i] + \" \" + newColumnDefaultType";//should be Sol: 2, wrong query-WL
			
			Query_="\"SET sql_mode = concat(@@sql_mode,',NO_BACKSLASH_ESCAPES')\"";//constant
			QParser.parse_query(Query_, true,null);
//		String clean_q2 = ReplaceColumnValues.cleanStatement("select * from practica7.SOCI, practica7.PERSONA WHERE practica7.SOCI.ID = practica7.PERSONA.ID AND practica7.PERSONA.Llinatge1 LIKE '%XXXX%';");
		String clean_q2 = "ReplaceColumnValues.cleanStatement(\"delete * from customers where cus = 'answer';\");"; 
		System.out.println("---------------------------> "+clean_q2);
//		System.out.print(Query_);
		// =====================================================================


	}

}
