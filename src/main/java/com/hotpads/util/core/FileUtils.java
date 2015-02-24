package com.hotpads.util.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtils{
    private static final Logger logger = LoggerFactory.getLogger( FileUtils.class );
	private static AtomicInteger nextId = new AtomicInteger();
	private static volatile String tempPath = null;
	// Temporary files are consumed on a rotational basis
	private static final int MAX_CURRENT_FILE = 256;
	/**
	 * Demonstrate use.
	 */
	public static void main(String... aArguments) throws FileNotFoundException{
		File tempDir = new File(aArguments[0]);
		List<File> files = FileUtils.listFilesInDirectory(tempDir);

		// print out all file names, and display the order of File.compareTo
		for(File f : files){
			System.out.println(f);
		}
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found; the List is sorted using File.compareTo.
	 * 
	 * @param aStartingDir is a valid directory, which can be read.
	 */
	public static List<File> listFilesInDirectory(File aStartingDir) throws FileNotFoundException{
		validateDirectory(aStartingDir);
		List<File> result = new ArrayList<File>();

		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for(File file : filesDirs){
			result.add(file); // always add, even if directory
			if(!file.isFile()){
				// must be a directory
				// recursive call!
				List<File> deeperList = listFilesInDirectory(file);
				result.addAll(deeperList);
			}

		}
		Collections.sort(result);
		return result;
	}

	public static List<File> filterFiles(FilenameFilter filter, Collection<File> files){
		if(files == null || files.size() < 0) return new ArrayList<File>(0);
		List<File> r = new ArrayList<File>(files.size());
		for(File f : files){
			if(!filter.accept(f.getParentFile(), f.getName())) continue;
			r.add(f);
		}
		return r;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be read.
	 */
	private static void validateDirectory(File aDirectory) throws FileNotFoundException{
		if(aDirectory == null){ throw new IllegalArgumentException("Directory should not be null."); }
		if(!aDirectory.exists()){ throw new FileNotFoundException("Directory does not exist: " + aDirectory); }
		if(!aDirectory.isDirectory()){ throw new IllegalArgumentException("Is not a directory: " + aDirectory); }
		if(!aDirectory.canRead()){ throw new IllegalArgumentException("Directory cannot be read: " + aDirectory); }
	}

	public static String appendSlashToDirectoryPathIfMissing(final String dir){
		if(StringTool.isEmpty(dir)){ return "/"; }
		if(dir.endsWith("/")){ return dir; }
		return dir + "/";
	}

	public static boolean createFileParents(String path){
		return createFileParents(new File(path));
	}

	public static boolean createFileParents(File aFile){
		if(aFile.exists()) return true;
		File parent = new File(aFile.getParent());
		if(parent.exists()) return true;
		try{
			parent.mkdirs();
		}catch(Exception e){
			return false;
		}
		return true;
	}

	public static Calendar getLastUpdated(File aFile){
		long lastModMillis = aFile.lastModified();
		Calendar lastMod = Calendar.getInstance();
		lastMod.setTimeInMillis(lastModMillis);
		return lastMod;
	}

	public static void delete(String path){
		if(StringTool.isEmpty(path) || "/".equals(path)){ throw new IllegalArgumentException(
				"cannot delete empty or root path"); }
		File file = new File(path);
		file.delete();
	}

	public static void deleteRecursive( File file ) {
		if(file==null){
			throw new IllegalArgumentException("cannot delete null file");
		}
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for ( File child : children ) {
				deleteRecursive( child );
			}
		}
		file.delete();
	}
	
	public static void delete(File file){
		if(file==null){
			throw new IllegalArgumentException("cannot delete null file");
		}
		file.delete();
	}
	
	public static boolean hasAStaticFileExtension(String path){
		if(path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".html") || path.endsWith(".pdf")
				|| path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".swf")){ return true; }
		return false;
	}

	public static boolean contentEquals(File file1, File file2) throws IOException{
		boolean file1Exists = file1.exists();
		if(file1Exists != file2.exists()){ return false; }
		if(!file1Exists){
			// two not existing files are equal
			return true;
		}
		if(file1.isDirectory() || file2.isDirectory()){
			// don't want to compare directory contents
			throw new IOException("Can't compare directories, only files");
		}
		if(file1.length() != file2.length()){
			// lengths differ, cannot be equal
			return false;
		}
		if(file1.getCanonicalFile().equals(file2.getCanonicalFile())){
			// same file
			return true;
		}
		InputStream input1 = null;
		InputStream input2 = null;
		try{
			input1 = new FileInputStream(file1);
			input2 = new FileInputStream(file2);

			return contentEquals(input1, input2);
		}finally{
			input1.close();
			input2.close();
			// IOUtils.closeQuietly(input1);
			// IOUtils.closeQuietly(input2);
		}
	}

	public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException{
		if(!(input1 instanceof BufferedInputStream)){
			input1 = new BufferedInputStream(input1);
		}
		if(!(input2 instanceof BufferedInputStream)){
			input2 = new BufferedInputStream(input2);
		}

		int ch1 = input1.read();
		while(-1 != ch1){
			int ch2 = input2.read();
			if(ch1 != ch2){
				return false; 
			}
			ch1 = input1.read();
		}

		int ch2 = input2.read();
		return (ch2 == -1);
	}
	
	public static void createRandomFile(String filePath, long fileSize) throws IOException{
		RandomAccessFile file = new RandomAccessFile(filePath, "rw");
		file.setLength(fileSize);
		file.close();
	}
	
	private static String nextTempId() {
		int id = nextId.incrementAndGet();
		while ( id >= MAX_CURRENT_FILE ) {
			nextId.set( 0 );
			id = nextId.incrementAndGet();
		}
		return "" + id;
	}
	
	public static String getTemporaryFileDirectoryName() throws IOException {
		if ( tempPath != null ) return tempPath;
		File f = File.createTempFile("whocares", ".tmp");
		tempPath = f.getParent();
		f.delete();
		return tempPath;
	}
	
	public static File createTemporaryFile( String prefix, byte[] bytes ) throws IOException {
		String name = prefix + nextTempId() + ".tmp";
		File f = new File( getTemporaryFileDirectoryName() + File.separatorChar + name  );
		FileOutputStream fos = new FileOutputStream( f );
		f.deleteOnExit();
		fos.write( bytes );
		fos.close();
		return f;  
	}

	public static File createWorkFile( String prefix ) throws IOException {
		String tempDir = getTemporaryFileDirectoryName();
		File fTempDir = new File( tempDir );
		File f = File.createTempFile(prefix, ".tmp", fTempDir );
		return f;  
	}
	
	public static void makeDirectoriesToPath( String filePath ) throws IOException, IllegalArgumentException {
		if ( StringUtils.isEmpty( filePath ) ) {
			throw new IllegalArgumentException( "File path must not be empty or null");
		}
		String[] paths = filePath.split( "/" );
		StringBuilder parentPath = new StringBuilder( "/" );
		
		for (int i = 0; i + 1 < paths.length; i++ ) {
			String subdir = paths[i];
			if ( StringUtils.isEmpty(subdir) ) {
				continue;
			}
			parentPath.append( subdir ).append( "/" );
			File dir = new File( parentPath.toString() );
			if ( dir.exists() ) {
				if ( dir.isDirectory() ) {
					continue;
				}
				throw new IllegalArgumentException( "Cannot create directory for " + filePath 
						+ " because " + parentPath.toString() + " is a standard file (not a directory)" );
			} else {
				if ( !dir.mkdir() ) {;
					throw new IllegalArgumentException( "Failed to directory " + parentPath.toString() 
							+ " for " + filePath );
				}
			}
		}
	}
	
	public static File copyFile(File source, String destPath) throws IOException{
		File dest = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try{
			makeDirectoriesToPath( destPath );
			dest = new File(destPath);
			if (dest.exists()) {
				dest.delete();
			}
			dest.createNewFile();
			inChannel = new FileInputStream(source).getChannel();
			outChannel = new FileOutputStream(dest).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch ( Exception e ) {
			logger.error( "Failed to copy file to " + destPath, e );
		}finally{
			if(inChannel != null) inChannel.close();
			if(outChannel != null) outChannel.close();
		}
		return dest;
	}

	
	public static File getResourcePathFile( String resourcePath ) throws IOException {
		URL u = FileUtils.class.getResource(resourcePath);
		String path = u.getFile();
		return new File( path );
	}
	
	public static String loadResourcePathFile( String resourcePath ) throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			inputStream = FileUtils.class.getResourceAsStream( resourcePath );
			if (inputStream == null) {
				File file = new File( resourcePath );
				if ( file.exists() ) {
					inputStream = new FileInputStream( file );
				}
			}
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			char[] buffer = new char[ 1000 ];
			while (bufferedReader.ready()) {
				int len = bufferedReader.read(buffer, 0, 1000);
				if ( len < 0 ) break;
				sb.append(buffer, 0, len);
			}
		} catch (IOException e) {
			logger.error( "Failed to load resource object " + resourcePath, e );
		} finally {
			close( inputStream, inputStreamReader, bufferedReader );
		}
		return sb.toString();
	}
	
	public static URL locateFile( String resourcePath ) throws IOException {
		return FileUtils.class.getResource( resourcePath );
	}

	
	public static void close( Closeable... objs ) {
		for ( Closeable o : objs ) {
			if ( o == null ) { continue; }
			try {
				o.close();
			} catch ( Exception e ) {
				logger.error( "Failed to close object " + o.getClass().getName(), e );
			}
		}
	}
	
	public static class FileUtilsTest{
		private static String FILE_PATH = "/tmp/t1.txt";
		private static long FILE_SIZE = 100000;
		@Test
		public void testCreateRandomFile() throws IOException{
			FileUtils.delete(FILE_PATH);
			createRandomFile(FILE_PATH, FILE_SIZE);
			File f = new File(FILE_PATH);
			Assert.assertTrue(f.exists());
			Assert.assertTrue(f.length()==FILE_SIZE);
			FileUtils.delete(FILE_PATH);
		}
		
		@Test
		public void testCreateTemporaryFileTest() throws IOException {
			String tmpdir = getTemporaryFileDirectoryName();
			System.out.println( "Temporary file directory is " + tmpdir );
			File file = createTemporaryFile("Test", "The boy on the burning deck again".getBytes() );
			System.out.println( "Temporary file path is " + file.getAbsolutePath() );
			file.delete();
		}

		@Test
		public void testCreateWorkFileAndDeleteTest() throws IOException {
			File workFile = createWorkFile( "work1" );
			checkBonaFide(workFile);
			delete(workFile);
			Assert.assertTrue( !workFile.exists() );
		}

		@Test
		public void testLoadPropertiesClassResource() throws IOException {
			Properties props = PropertiesTool.fromFile(  "/test.properties" );
			Assert.assertTrue("props should not be null", props != null );
		}

		public void testLoadPropertiesAbsoluteResource() throws IOException {
			Properties props = PropertiesTool.fromFile(  "/hotpads/config/databases.properties" );
			Assert.assertTrue("props should not be null", props != null );
		}

		private void checkBonaFide( File f ) {
			Assert.assertTrue( f != null );
			Assert.assertTrue( f.exists() );
			Assert.assertTrue( f.canWrite() );
		}
		
		@Test
		public void testCreateWorkFileCopyAndDeleteTest() throws IOException {
			String subDirPath = "/tmp/testSubDir1/";
			String destPath =  subDirPath + "testCopyFile.txt";
			File temp = createTemporaryFile("work2", "Some random data in the file please".getBytes());
			checkBonaFide(temp);
			File dest = copyFile(temp, destPath);
			checkBonaFide(dest);
			delete(temp);
			File subDir = new File( subDirPath );
			checkBonaFide(dest);
			deleteRecursive(subDir);
			Assert.assertTrue( !temp.exists() );
			Assert.assertTrue( !dest.exists() );
			Assert.assertTrue( !subDir.exists() );
		}

	}
}