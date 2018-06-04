package util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	
	public static boolean copyFile(File file, File destinationDirectory) {
		if (file == null || destinationDirectory == null)
			return false;
		if (!file.exists() || !destinationDirectory.exists())
			return false;
		if (file.isDirectory() || destinationDirectory.isFile())
			return false;
		return copyFile(file, destinationDirectory, file.getName());
	}

	public static boolean copyFile(File file, File destinationDirectory,
			String fileName) {
		if (file == null || destinationDirectory == null)
			return false;
		if (!file.exists() || !destinationDirectory.exists())
			return false;
		if (file.isDirectory() || destinationDirectory.isFile())
			return false;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(file);

			destinationDirectory.mkdirs();
			String destinationPath = destinationDirectory.getPath()
					+ File.separator + fileName;
			File destinationFile = new File(destinationPath);
			if (destinationFile.exists())
				destinationFile.delete();
			out = new FileOutputStream(destinationFile);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

	public static boolean copyDirectory(File sourceLocation, File targetLocation) {
		InputStream in = null;
		OutputStream out = null;
		try {
			if (sourceLocation.isDirectory()) {
				if (!targetLocation.exists()) {
					targetLocation.mkdir();
				}

				String[] children = sourceLocation.list();
				for (int i = 0; i < children.length; i++) {
					copyDirectory(new File(sourceLocation, children[i]),
							new File(targetLocation, children[i]));
				}
			} else {

				in = new FileInputStream(sourceLocation);
				out = new FileOutputStream(targetLocation);

				// Copy the bits from instream to outstream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

	public static void deleteDirectory(File dir) {
		if (dir == null || !dir.exists())
			return;
		if (dir.isDirectory()) {
			for (File file : dir.listFiles())
				deleteDirectory(file);
		} else {
			dir.delete();
		}
	}

	/**
	 * @Caution : this method close the stream
	 * @param stream
	 * @param destinationDirectory
	 * @param fileName
	 * @return
	 */
	public static boolean saveFromStream(InputStream stream,
			File destinationDirectory, String fileName) {
		if (stream == null || destinationDirectory == null)
			return false;
		if (!destinationDirectory.exists() || destinationDirectory.isFile())
			return false;

		OutputStream out = null;
		try {
			destinationDirectory.mkdirs();
			String destinationPath = destinationDirectory.getPath()
					+ File.separator + fileName;
			File destinationFile = new File(destinationPath);
			if (destinationFile.exists())
				destinationFile.delete();
			out = new FileOutputStream(destinationFile);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = stream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024*100];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
	}

	public static long directorySize(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += directorySize(file);
		}
		return length;
	}

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
