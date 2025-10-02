package sketchGen.transformer;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class Transformer extends VoidVisitorAdapter<Void> {
	protected TransformInstance subject;
	protected List<File> list = new ArrayList<File>();
	protected Map<Expression, Expression> candidates = new HashMap<Expression, Expression>();
	protected int index = 0;
	protected String transformerName = "";
	private String source = "";
	private int id = 0;


	public Transformer(TransformInstance subject) {
		this.subject = subject;

		id = subject.getBugLocation().getId();

		File file = new File(source);
		if (!file.exists())
			file.mkdirs();
		String f = subject.getBugLocation().getFilePath();
		source += f.substring(f.lastIndexOf("/")) + "-";
	}


	public abstract List<File> transform();

	protected void writeToFile() {
		String path = transformerName +"-"+ id+"-"+ index++;
		try {
			PrintWriter writer = new PrintWriter(path);
			System.out.println("Generated candidates: "+ path);
			writer.println(subject.getOriginClass().getCompilationUnit());
		
			writer.close();
		} catch (Throwable e) {

			new File(path).delete();
		}

	}
	
	public void setSource(String source) {
		this.source = source;
	}
}
