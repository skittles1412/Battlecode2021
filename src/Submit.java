import java.io.*;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Submit {
	private static BufferedWriter writer;
	private static String folder;
	private static HashSet<String> processedFiles;
	private static ArrayDeque<ToProcessFile> toProcessFiles;
	//working directory
	//run in folder containing src
	private static final File ROOT = new File(System.getProperty("user.dir")), SOURCE = getChild(ROOT, "src");
	private static final Pattern IMPORT = Pattern.compile("\\h*import\\h*(?:static\\h*)?([^;]+);");
	private static final Pattern PRINT = Pattern.compile("\\h*System\\.out\\.println");
	static class ToProcessFile {
		public File file;
		public String path;
		public ToProcessFile(File file, String path) {
			this.file = file;
			this.path = path;
		}
		@Override
		public boolean equals(Object o) {
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ToProcessFile that = (ToProcessFile) o;
			return path.equals(that.path);
		}
		@Override
		public int hashCode() {
			return Objects.hash(path);
		}
		@Override
		public String toString() {
			return "ToProcessFile{"+
					"path='"+path+'\''+
					'}';
		}
	}
	//write logic to process a file here
	//the new file should get written to writer
	private static void processFile(File file) throws IOException {
		for(String line: Files.readAllLines(file.toPath())) {
			Matcher matcher = IMPORT.matcher(line);
			if(matcher.find()) {
				String importedClass = matcher.group(1);
				if(!importedClass.startsWith("battlecode")&&!importedClass.startsWith("java")&&!importedClass.startsWith(folder)) {
					line = line.substring(0, matcher.start(1))+folder+".external_imports."+importedClass+line.substring(matcher.end(1));
					String importPath = importedClass.replace('.', '/');
					if(importPath.endsWith("/*")) {
						importPath = importPath.substring(0, importPath.length()-1);
						File imported = getChild(SOURCE, importPath);
						if(imported.exists()) {
							for(File child: imported.listFiles()) {
								if(child.isFile()) {
									String path = folder+"/external_imports/"+importPath+child.getName();
									if(!processedFiles.contains(path)) {
										processedFiles.add(path);
										toProcessFiles.add(new ToProcessFile(child, path));
									}
								}
							}
						}else {
							throw new IllegalArgumentException("The import "+importedClass+" couldn't be found under src");
						}
					}else {
						File imported = getChild(SOURCE, importPath+=".java");
						importPath = folder+"/external_imports/"+importPath;
						if(!processedFiles.contains(importPath)) {
							processedFiles.add(importPath);
							toProcessFiles.add(new ToProcessFile(imported, importPath));
						}
					}
				}
			}else if(PRINT.matcher(line).find()) {
				line = "//"+line;
			}
			println(line);
		}
	}
	//prints a line into the output stream
	private static void println(String s) throws IOException {
		writer.write(s);
		writer.newLine();
	}
	//get the child of parent called name
	private static File getChild(File parent, String name) {
		return new File(parent.getAbsolutePath()+"/"+name);
	}
	public static void main(String[] args) throws IOException {
		if(args.length==0) {
			System.out.println("Please enter the folder to submit");
			folder = new Scanner(System.in).next();
		}else {
			folder = args[0];
		}

		File submission = getChild(SOURCE, folder);
		File outputFile = getChild(ROOT, "submission.zip");

		System.out.println("Extracting "+submission.getAbsolutePath()+" to "+outputFile.getAbsolutePath()+":");
		System.out.println("Processing files");

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
		writer = new BufferedWriter(new OutputStreamWriter(out));

		processedFiles = new HashSet<>();
		toProcessFiles = new ArrayDeque<>();
		toProcessFiles.add(new ToProcessFile(submission, folder));

		while(!toProcessFiles.isEmpty()) {
			ToProcessFile toProcessFile = toProcessFiles.removeFirst();
			File file = toProcessFile.file;
			String path = toProcessFile.path;
			System.out.println("Processing "+path);
			if(file.isDirectory()) {
				for(File child: file.listFiles()) {
					toProcessFiles.addFirst(new ToProcessFile(child, path+"/"+child.getName()));
				}
			}else {
				out.putNextEntry(new ZipEntry(path));
				processFile(file);
				writer.flush();
			}
		}
		out.close();
		System.out.println("Finished!");
	}
}
