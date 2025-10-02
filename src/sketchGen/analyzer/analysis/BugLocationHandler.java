package sketchGen.analyzer.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sketchGen.transformer.TransformManager;
import sketchGen.transformer.TransformInstance;

public class BugLocationHandler {
	protected BuggyFileParser parser = new BuggyFileParser();

	
	public void processBugLocations(String bugLocations, String srcDir, String outDir) {
		String[] locations = bugLocations.split(",");
		List<BugLocation> locList = new ArrayList<>();
		int bugId = 1;

		for(String loc: locations) {
			String[] tk = loc.split(":");
			locList.add(new BugLocation(srcDir + tk[0].replace(".", "/") + ".java", "", Integer.parseInt(tk[1]), bugId++));
		}

		parser.setSrcDir(srcDir);
		
		for (BugLocation loc : locList) {
			TransformInstance instance = new TransformInstance(loc, parser, Holes.HoleType.RopHole);
			List<File> instrumented = new TransformManager(instance).transform(outDir);
		}
	}
}
