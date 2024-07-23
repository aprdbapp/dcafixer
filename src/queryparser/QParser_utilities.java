package queryparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class QParser_utilities {
	public static String prepare_appended_query(String query, boolean print, String LIKE_symbol) {
		// TODO; consider strings that use String.format(...) - think of all possible
		// cases with %, consider %d, %f, %
		// e.g., String.format("name is %s",name);
		// https://www.javatpoint.com/java-string-format.
		// https://docs.oracle.com/javase/tutorial/java/data/numberformat.html

		// ===============================


		String temp1, temp2, clean_query;
		/* cleaning steps */ if (print) {
			System.out.println("$: " + query);
		}
		query = query.trim().replaceAll("\\s+", " ");
		query = query.replace("\u00a0", " ");//=== added on Feb/14
		if(query.startsWith("(") && query.endsWith(")")) {
			query = query.substring(1, query.length() - 1);
		}
		// ??????
		/* cleaning steps */ if (print) {
			System.out.println("$0: " + query);
		}
//		temp1 = query.replace("\n", " ").replace("\\n", " ").trim().replace("\" + \"", " ").replaceAll("\\s+", " ").replace("`", "'");//=============== I commented this on FEb/14 to handle ` and \"
		temp1 = query.replace("\n", " ").replace("\\n", " ").trim().replace("\" + \"", " ").replaceAll("\\s+", " ").replace("`", "\"");// +++++ Added on Feb/14
        // .replace("\" + ", "\'");
			// Keep single and double quotes that are part of the queries

		temp1 = temp1.replace("\\'", "#");// +++++ Commented on Feb/14
//		temp1 = temp1.replace("\\'", "$");// +++++ Added on Feb/14

		temp1 = temp1.replace("'", "#");
		// use "#" as a temp
		/* cleaning steps */ if (print) {
			System.out.println("$1: " + temp1);
		}
		temp1 = temp1.replace("\" + ", "'").replace("\"+ ", "'").replace("\" +", "'").replace("\"+", "'");

		/* cleaning steps */ if (print) {
			System.out.println("$2: " + temp1);
		}
		temp2 = temp1.replace(" + \"", "'").replace(" +\"", "'").replace("+ \"", "'").replace("+\"", "'");
		// ++
		/* cleaning steps */ if (print) {
			System.out.println("$3: " + temp1);
		}
		// Changed the following to do insert into and handle single quotes
//		temp2 = temp2.replace("\";\"", "").replace(";", "").replace("\"", "").trim();
		temp2 = temp2.replace("\";\"", ";").replace("\" ;\"", ";").replace("\"; \"", ";").trim();


		/* cleaning steps */ if (print) {
			System.out.println("$3': " + temp1);
		}
		temp2 = temp2.replace(") ;", ");").replace(" ;", "';");
		// +++ because of update example
		/* cleaning steps */ if (print) {
			System.out.println("$3'': " + temp1);
		}
		temp2 = temp2.replace(";\"", ";").replace("; \"", ";").replace(";;", ";").replace("; ;", ";").trim();
		// +++
		/* cleaning steps */ if (print) {
			System.out.println("$3''': " + temp1);
		}
//		if (temp2.endsWith(";")) {
//			temp2 = temp2.substring(0, temp2.length() - 1);
//		}
		int count = temp2.length() - temp2.replace("'", "").length();
		/* cleaning steps */ if (print)
		 {
			System.out.println("count = " + count);
//		clean_query = temp2;
		}

		if (count % 2 > 0) {

			/* cleaning steps */ if (print) {
				System.out.println("$4: " + temp2);
			}
			if(temp2.trim().endsWith(";")) { // To handle UI at the end
				temp2 = temp2.substring(0,temp2.length()-1);
				temp2 = temp2.trim() + "';";
			}else {
				temp2 = temp2.trim() + "'";
			}
		}
		if (temp2.trim().startsWith("\"")) {
			temp2 = temp2.substring(1);
		}
		temp2 = temp2.replace("#", "\\\"");//=============== I commented this on FEb/14 to handle ` and \"
//		temp2 = temp2.replace("$", "\\\""); // ==== Added on Feb/14
//		temp2 = temp2.replace("#", "\"");// ==== Added on Feb/14
//		clean_query = temp2;
		/* cleaning steps */ if (print) {
			System.out.println("@@@: " + temp2);
		}
		// TODO: clean user input from extra quotations single & double
		// the code bellow removes " around variables
		temp2 = temp2.replace("\\s+", " ");
//		String[] queryParts = temp2.split(" ");// splitting by space caused an issue with e.g.(('DATE1',"Month DD,YYYY"))
		if (print) {
			System.out.print("Handling like with qoutations ");
		}
		// ======== method 1
		if (print) {
			System.out.println(", split by \"");
		}
		String[] queryParts = temp2.split("\"");
		int i = 0;
//		for (String p : queryParts) {
//			if (print) System.out.println("\t***** >" + p);
//		}
		for (String p : queryParts) {
			if (i % 2 == 1) {
				if (print) {
					System.out.println("\t***** >" + p);
				}

				String temp_p = p;
				if (p.contains("'%")) {
					temp_p = temp_p.replace("'%", LIKE_symbol + "'");
					if (print) {
						System.out.println("\t\ttemp_p1=== >" + temp_p);
					}
				}
				if (p.contains("%'")) {
					temp_p = temp_p.replace("%'", "'" + LIKE_symbol);
					if (print) {
						System.out.println("\t\ttemp_p2=== >" + temp_p);
					}
				}

				if (print) {
					System.out.println("\t\ttemp_p3=== >" + temp_p);
				}
				if (!temp_p.equals(p)) { // No change, then don't replace value
					temp2 = temp2.replace("\"" + p + "\"", temp_p);
				}
				if (print) {
					System.out.println("\t\ttemp_p4=== >" + temp2);
				}

			}
			i++;
		}

		// ======== method 2
//		if (print) System.out.println(", split by space ");
//		String[] queryParts = temp2.split(" ");
//		for (String p : queryParts) {
//
//			if (p.contains("'")) {
////				System.out.println("p ==== >" + p);
//				String temp_p = p;
//				if (p.contains("\""))
//					temp_p = temp_p.replace("\"", "");
//
//				if (p.contains("'%\""))
//					temp_p = temp_p.replace("'%", LIKE_symbol + "'");
//				if (p.contains("\"%'"))
//					temp_p = temp_p.replace("%'", "'" + LIKE_symbol);
//
//				temp2 = temp2.replace(p, temp_p);
//			}
//			}
		// ====================
		// Remove extra double qoutations
		int countdq = temp2.length() - temp2.replace("\"", "").length();
		if (countdq % 2 > 0) {
			if (temp2.trim().endsWith("\"")) {
				temp2 = temp2.substring(0, temp2.length() - 1);
			} else if (temp2.trim().startsWith("\"")) {
				temp2 = temp2;
			} else if(temp2.trim().endsWith("\";")) { // handle the case when the query ends with (";)
				temp2 = temp2.substring(0, temp2.length() - 2);
			}
			 if(temp2.trim().endsWith("\" ;")) { // handle the case when the query ends with (" ;)
				temp2 = temp2.substring(0, temp2.length() - 3);
			}
		}
		clean_query = temp2;


		clean_query = clean_query.replace("where", " where ").replace("WHERE", " WHERE ").replace("Where", " Where ");
		clean_query = clean_query.replace("from", " from ").replace("From", " From ").replace("FROM", " FROM ");
		//Selected
		String Selected_temp = "$SSS$";
		clean_query = clean_query.replace("Selected", Selected_temp);
		clean_query = clean_query.replace("select", " select ").replace("Select", " Select ").replace("SELECT", " SELECT ");
		clean_query = clean_query.replace(Selected_temp, "Selected");
		clean_query = clean_query.replace("delete", " delete ").replace("Delete", " Delete ").replace("DELETE", " DELETE ");
//		clean_query = clean_query.replace("update", " update ").replace("Update", " Update ").replace("UPDATE", " UPDATE ");
//		clean_query = clean_query.replace("insert", " insert ").replace("Insert", " Insert ").replace("INSERT", " INSERT ");
		clean_query = clean_query.replace("create", " create ").replace("Create", " Create ").replace("CREATE", " CREATE ");
		clean_query = clean_query.replace("alter", " alter ").replace("Alter", " Alter ").replace("ALTER", " ALTER ");
		clean_query = clean_query.replace("drop", " drop ").replace("Drop", " Drop ").replace("DROP", " DROP ");
		clean_query = clean_query.replace("truncate", " truncate ").replace("Truncate", " Truncate ").replace("TRUNCATE", " TRUNCATE ");
		clean_query = clean_query.trim().replaceAll("\\s+", " ");
		/* cleaning steps */ if (print) {
			System.out.println("$5: " + clean_query);
		}
		return clean_query;

	}


	public static String classified_query(String markedQuery) throws IOException {
		String newQuery = "";
		String s = null;
		Process p = Runtime.getRuntime()
				.exec("python3 python3 ~/Fixer/tmp/parse_sql.py " + "\"" + markedQuery.trim() + "\"");
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((s = in.readLine()) != null) {
			System.out.print(s);
		}

		return newQuery;
	}

	public static int countMatches(String str, String findStr) {
		int lastIndex = 0;
		int count = 0;
		while (lastIndex != -1) {

			lastIndex = str.indexOf(findStr, lastIndex);

			if (lastIndex != -1) {
				count++;
				lastIndex += findStr.length();
			}
		}
//		System.out.println(count);
		return count;
	}


}
