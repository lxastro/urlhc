package xlong.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
	public static void deleteDir(String path) throws Exception{
		Files.walkFileTree(Paths.get(path),
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult postVisitDirectory(Path dir,
							IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file,
							BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
				});
	}
	
	public static void createDir(String path) throws Exception {
		Files.createDirectories(Paths.get(path));
	}
	
	public static String addTralingSlash(String path) {
		if (path.endsWith("/")) {
			return path;
		} else {
			return path + "/";
		}
	}
}
