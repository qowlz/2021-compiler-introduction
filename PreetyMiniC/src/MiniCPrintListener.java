import org.antlr.v4.runtime.tree.ParseTreeProperty;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MiniCPrintListener extends MiniCBaseListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>();

	// 들여쓰기 깊이
	int depth = 0;

	// 들여쓰기 크기
	final int indentSize = 4;

	// 앞뒤로 피연산자가 오는 연산자인지 확인해주는 함수
	public boolean isBinaryOP(String str)
	{
		if (str.equals("*") || str.equals("/") || str.equals("%") || str.equals("+") || str.equals("-") ||
				str.equals("=") || str.equals("==") || str.equals("<=") || str.equals(">=") || str.equals("!=") ||
				str.equals("<") || str.equals(">") || str.equals("and") || str.equals("or"))
			return true;

		return false;
	}

	// 들여쓰기 문자열을 반환해주는 함수
	public String getIndentStr()
	{
		return depth * indentSize <= 0 ? "" : ".".repeat(depth * indentSize);
	}

	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		String program = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			newTexts.put(ctx, ctx.decl(i).getText());
			program += newTexts.get(ctx.getChild(i));
		}

		File file = new File(String.format("[HW3]201904243.c"));
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(program);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			content += newTexts.get(child) + "\n";
			content += i != ctx.getChildCount() - 1 ? "\n" : ""; // 마지막 decl을 제외하고 decl들 사이에 개행을 넣어줌
		}
		newTexts.put(ctx, content);
	}

	@Override
	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (isBinaryOP(append)) // =이 매칭되는경우 앞 뒤로 공백 넣어주기
				content += String.format(" %s ", append);
			else
				content += append;
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitType_spec(MiniCParser.Type_specContext ctx) {
		// TypeSpec은 IDENT와 짝꿍으로 나오는 경우밖에 없으므로 항상 뒤에 한칸 공백을 넣어줌
		newTexts.put(ctx, ctx.getChild(0).getText() + " ");
	}

	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (append.equals(")")) // ) 뒤엔 compoundStmt가 오니 개행을 넣어줌
				content += append + "\n";
			else
				content += append;
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitParams(MiniCParser.ParamsContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (append.equals(",")) // ,뒤엔 param이 존재하니 항상 공백을 넣어줌
				content += append;
			else
				content += append;
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitParam(MiniCParser.ParamContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			content += newTexts.get(child) != null ? newTexts.get(child) : child.getText();
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitStmt(MiniCParser.StmtContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			content += newTexts.get(child) != null ? newTexts.get(child) : child.getText();
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		var indent = getIndentStr();
		String content = indent;
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			content += newTexts.get(child) != null ? newTexts.get(child) : child.getText();
		}

		newTexts.put(ctx, content);
	}

	// while문에서는 stmt가 하나는 꼭 들어오므로 들여쓰기 depth를 증가시킴
	@Override
	public void enterWhile_stmt(MiniCParser.While_stmtContext ctx) { depth++; }

	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		// enterWhile_stmt에서 증가시켰던 depth를 원래대로 되돌림
		depth--;

		var indent = getIndentStr();
		String content = indent;
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (append.equals("while")) // while과 ( 사이에 공백을 넣어줌
				content +=  append + " ";
			else if (append.equals(")")) // )과 stmt 사이에 개행을 넣어줌
				content += append + "\n";
			else
				content += append;
		}
		newTexts.put(ctx, content);
	}

	@Override
	public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// CompoundStmt의 부모가 될수 있는 경우는 Fundecl을 제외하면 IfStmt, whileStmt만 존재
		// IfStmt, whileStmt는 parent 위치에서 depth를 증가시켜주도록 처리 중
		// 때문에, 위 두 구문의 자식인 경우에는 depth를 증가시키지 않도록 함
		var parent = ctx.getParent();
		if (!(parent instanceof MiniCParser.Fun_declContext)) return;

		// 부모가 FunDecl인 경우에만 depth를 증가
		depth++;
	}

	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// 부모가 FunDecl인 경우에만 증가시켰던 depth를 원래대로 복구
		var parent = ctx.getParent();
		if (parent instanceof MiniCParser.Fun_declContext){
			depth--;
		}

		var innerIndent = getIndentStr(); // 중괄호 내부 stmt들의 들여쓰기
		var braceIndent = innerIndent.length() - indentSize <= 0 ? // 중괄호 들여쓰기
				"" : innerIndent.substring(0, innerIndent.length() - indentSize);
		String content = braceIndent; // 여는 중괄호 앞에 indent 삽입
		boolean hasInnerContent = ctx.getChildCount() > 2; // 중괄호 안에 stmt가 존재하는가
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			var append = newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (hasInnerContent) // 중괄호 안에 stmt가 존재하면
			{
				if (append.equals("{")) // 여는 중괄호 뒤에 개행을 넣어줌
					content +=  append + "\n";
				else if (append.equals("}")) // 닫는 중괄호 앞에 indent 삽입
					content += braceIndent + append;
				else
					content += append + "\n"; // stmt들 사이에는 개행을 넣어줌
			}
			else
				content += append; // 중괄호만 있으면 전부 붙여서 써줌
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		var indent = getIndentStr();
		String content = indent;
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (isBinaryOP(append)) // =가 매칭되면 앞뒤로 공백 삽입
				content += String.format(" %s ", append);
			else
				content +=  append;
		}

		newTexts.put(ctx, content);
	}

	// IfStmt에선 stmt가 최소 한개는 들어오기때문에 depth 증가
	@Override
	public void enterIf_stmt(MiniCParser.If_stmtContext ctx) { depth++; }

	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		// enter에서 증가시켰던 depth 복구
		depth--;

		var indent = getIndentStr();
		String content = indent;
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (append.equals("if")) // if와 ( 사이에 공백 추가
				content += append + " ";
			else if (append.equals(")")) { // )와 stmt 사이에 개행 추가
				content += append + "\n";
			}
			else if (append.equals("else")) { // else 앞에 개행 + indent 삽입, 뒤에는 개행 삽입
				content += '\n' + indent + append + '\n';
			}
			else
				content += append;
		}

		newTexts.put(ctx, content);
	}

	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		var indent = getIndentStr();
		String content = indent;
		boolean hasExpr = ctx.getChildCount() > 2; // expr을 자식으로 가지고있는가
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (append.equals("return")) // return 뒤에 expr이있으면 공백, 없다면 그대로 붙여줌
				content += hasExpr ? append + " " : append;
			else
				content += append;
		}
		newTexts.put(ctx, content);
	}

	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (isBinaryOP(append)) // 연산자가 매칭되면 앞뒤로 공백을 넣어줌
				content += String.format(" %s ", append);
			else
				content += append;
		}
		newTexts.put(ctx, content);
	}

	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {
		String content = "";
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			var child = ctx.getChild(i);
			String append =  newTexts.get(child) != null ? newTexts.get(child) : child.getText();
			if (append.equals(",")) // ,과 expr 사이에 항상 공백을 넣어줌
				content += append + " ";
			else
				content += append;
		}

		newTexts.put(ctx, content);
	}
}