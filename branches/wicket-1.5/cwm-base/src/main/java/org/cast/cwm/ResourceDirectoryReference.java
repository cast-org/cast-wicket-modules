package org.cast.cwm;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.File;

/**
 * ResourceReference for a ResourceDirectory.
 * For example, to make all the files in an "images" directory available under the URL path "img", 
 * you can mount this as follows:
 * <blockquote><code>
 *    mountResource("img/${name}", new ResourceDirectoryReference(themeDirectory, "images"));
 * </code></blockquote>
 * @author borisgoldowsky
 *
 */
public class ResourceDirectoryReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	
	private final File resourceDirectory;

	/**
	 * Construct for a given directory.
	 * @param directory
	 */
	public ResourceDirectoryReference(File directory) {
		super(directory.getAbsolutePath());  // Use pathname as ResourceReference key
		resourceDirectory = directory;
	}

	/**
	 * Convenience constructor where a base directory and subdirectory within it are specified.
	 * Useful if you are building several ResourceDirectoryReferences that are siblings to each other.
	 * @param themeDir base directory within which the resources directory is found
	 * @param subdirectory name of the subdirectory
	 */
	public ResourceDirectoryReference (File themeDir, String subdirectory) {
		this(new File(themeDir, subdirectory));
	}
	
	@Override
	public IResource getResource() {
		return new ResourceDirectory(resourceDirectory);

	}

}
