package sketchGen.transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransformManager {
	private List<Transformer> transformers = new ArrayList<Transformer>();

	
	public TransformManager(TransformInstance subject) {
		transformers.add(new ExpressionMutator(subject));
		transformers.add(new ArithmeticMutator(subject));
		transformers.add(new OperatorMutator(subject));
		transformers.add(new OverloadMutator(subject));
		transformers.add(new ConditionRemover(subject));
		transformers.add(new ConditionTransformer(subject));
		transformers.add(new ConditionAdder(subject));
		transformers.add(new MultiExprMutator(subject));
	}

	public List<File> transform(String source) {
		List<File> files = new ArrayList<File>();
		for (Transformer transformer : transformers) {
			transformer.setSource(source);
			files.addAll(transformer.transform());
		}
		return files;
	}

}
