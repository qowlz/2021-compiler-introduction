import org.antlr.v4.runtime.*;

public class TestMiniC {
	public static void main(String[] args) throws Exception {
		MiniCLexer lexer = new MiniCLexer( new ANTLRFileStream("test.c"));
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		MiniCParser parser = new MiniCParser( tokens );
		genAST(parser.program());
	}

	public static void genAST(RuleContext context)
	{
		System.out.println("201904243 Rule " + context.getRuleIndex());
		for (int i = 0; i < context.getChildCount(); i++)
		{
			var elem = context.getChild(i);
			if (elem instanceof RuleContext)
				genAST((RuleContext) elem);
		}
	}
}