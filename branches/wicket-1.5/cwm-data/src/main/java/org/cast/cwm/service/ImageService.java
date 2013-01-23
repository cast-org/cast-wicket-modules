/*
 * Copyright 2011-2013 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.apache.sanselan.Sanselan;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.BinaryFileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
/**
 * TODO: Compare for quality with https://cwiki.apache.org/WICKET/uploaddownload.html as it was suggested by Igor
 * 
 * @author jbrookover
 *
 */
public class ImageService {

	private static final Logger log = LoggerFactory.getLogger(ImageService.class);
	
	@Inject
	private ICwmService cwmService;

	protected static Map<ScaledImageKey, ScaledImage> scaledImageCache;
	protected static final int MAX_SCALED_ENTRIES = 1000;
	protected static Color FILL_COLOR = new Color(248, 248, 248); // #F8F8F8
	protected static Color EDGE_COLOR = new Color(217, 217, 217); // #D9D9D9
	
	static {
		scaledImageCache = Collections.synchronizedMap(new LinkedHashMap<ScaledImageKey, ScaledImage>(500, (float) 0.75, true) {

			private static final long serialVersionUID = 1L;
			
			@Override
			protected boolean removeEldestEntry(Map.Entry<ScaledImageKey, ScaledImage> eldest) {
				return size() > MAX_SCALED_ENTRIES;
			}
		});
		
	}

	protected static ImageService instance = new ImageService();

	public static ImageService get() {
		return instance;
	}

	public ImageService() {
		Injector.get().inject(this);
	}
	
	/**
	 * Resize an image.  All resize functions fall to this.  
	 * 
	 * @param original
	 * @param maxW
	 * @param maxH
	 * @param exact if exact, will force the image to be the provided size; gaps filled in with gray
	 * @return
	 */
	public BufferedImage resizeToBufferedImage(BufferedImage original, Integer maxW, Integer maxH, boolean exact) {

		if (original == null)
			return null;

		boolean resizeRequired = false;
		
		int w = original.getWidth();
		int h = original.getHeight();
		if (maxW != null && w > maxW) {
			float factor = (float) maxW / w;
			w = (int) (w * (factor));
			h = (int) (h * (factor));
			resizeRequired = true;
		}
		if (maxH != null && h > maxH) {
			float factor = (float) maxH / h;
			w = (int) (w * (factor));
			h = (int) (h * (factor));
			resizeRequired = true;
		}

		if (!resizeRequired && !exact)
			return original;

		BufferedImage resized = new BufferedImage(exact ? maxW : w, exact ? maxH : h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resized.createGraphics();
		
		// Fill with Light Gray if we might have gaps
		if (exact) {
			g.setColor(FILL_COLOR);
			g.fillRect(0, 0, resized.getWidth() - 1, resized.getHeight() - 1);
			g.setColor(EDGE_COLOR);
			g.drawRect(0, 0, resized.getWidth() - 1, resized.getHeight() - 1);
		}

		g.drawImage(original, exact ? Math.abs(maxW - w) / 2 : 0, exact ? Math.abs(maxH - h) / 2 : 0, w, h, FILL_COLOR, null);
		g.dispose();

		return resized;

	}

	public BufferedImage resizeToBufferedImage(BufferedImage original, Integer maxW, Integer maxH) {
		return resizeToBufferedImage(original, maxW, maxH, false);
	}

	public BufferedImage resizeToBufferedImage(byte[] bytes, Integer maxW, Integer maxH) {
		return resizeToBufferedImage(bytes, maxW, maxH, false);
	}

	public BufferedImage resizeToBufferedImage(byte[] bytes, Integer maxW, Integer maxH, boolean exact) {
		
		BufferedImage original = null;

		try {
			original = ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (IOException e) {
			original = null;
			e.printStackTrace();
		}
		
		return resizeToBufferedImage(original, maxW, maxH, exact);
	}
	
	/**
	 * 
	 * @param bytes
	 * @param mimeType the mimeType of the returned byte[]
	 * @param maxW
	 * @param maxH
	 * @return
	 */
	public byte[] resizeToByteArray(byte[] bytes, String mimeType, Integer maxW, Integer maxH) {
		return resizeToByteArray(bytes, mimeType, maxW, maxH, false);
	}
	
	/**
	 * 
	 * @param bytes
	 * @param mimeType the mimeType of the returned byte[]
	 * @param maxW
	 * @param maxH
	 * @param exact
	 * @return
	 */
	public byte[] resizeToByteArray(byte[] bytes, String mimeType, Integer maxW, Integer maxH, boolean exact) {
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String type = mimeType.substring(mimeType.lastIndexOf("/") + 1);
		
		if (type.equals("pjpeg"))
			type = "jpeg"; // Internet Explorer is stupid

		BufferedImage image = resizeToBufferedImage(bytes, maxW, maxH, exact);
		
		try {
			ImageIO.write(image, type, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return out.toByteArray();
	}
	
	/**
	 * Get a thumbnail of a certain size for a given key.  Many applications will want
	 * to override this to create the thumbnail if it is not found in the HashMap.
	 * 
	 * @param key
	 * @param w
	 * @param h
	 * @return
	 */
	public ScaledImage getScaledImage(ScaledImageKey key) {
		ScaledImage thumb = scaledImageCache.get(key);
		
		if (thumb != null)
			return thumb;
		
		if (key.getKey() instanceof Long) {
			Long datastoreId = (Long) key.getKey();
			BinaryFileData data = cwmService.getById(BinaryFileData.class, datastoreId).getObject();
			thumb = new ScaledImage(resizeToBufferedImage(data.getData(), key.getWidth(), key.getHeight()));
			try {
				thumb.setType(Sanselan.getImageInfo(data.getData()).getFormat().extension.toLowerCase());
			} catch (Exception e) {
				log.warn("Failed to determine image type: {}", datastoreId);
			}
			
		} else if (key.getKey() instanceof String) {
			String url = (String) key.getKey();
			if(url.startsWith("http://")) {
				try {
					BufferedImage externalImage = ImageIO.read(new URL(url));
					thumb = new ScaledImage(resizeToBufferedImage(externalImage, key.getWidth(), key.getHeight()));
				} catch (Exception e) {
					throw new WicketRuntimeException(e);
				}
			}
		}
		
		scaledImageCache.put(key, thumb);
		return thumb;
	}
	
	public Image getScaledImageComponent(String wicketId, Long datastoreId, Integer maxWidth, Integer maxHeight) {		
		Image imageComponent = new ScaledImageComponent(wicketId, getResourceReference(datastoreId, maxWidth, maxHeight));
		return imageComponent;
		
	}
	
	public Image getScaledImageComponent(String wicketId, String url, Integer maxWidth, Integer maxHeight) {
		Image imageComponent = new ScaledImageComponent(wicketId, getResourceReference(url, maxWidth, maxHeight));
		return imageComponent;
	}

	
	public ScaledImageResourceReference getResourceReference(String url, Integer maxWidth, Integer maxHeight) {
		String name = url.replaceAll("/", "_");
		return getResourceReference(name, url, maxWidth, maxHeight);
		
	}
	
	public ScaledImageResourceReference getResourceReference(Long datastoreId, Integer maxWidth, Integer maxHeight) {
		BinaryFileData data = cwmService.getById(BinaryFileData.class, datastoreId).getObject();
		String name = datastoreId.toString() + "_" + (data == null ? "unknown.png" : data.getName());
		return getResourceReference(name, datastoreId, maxWidth, maxHeight);
		
	}
	
	protected ScaledImageResourceReference getResourceReference(String fileName, Serializable key, Integer maxWidth, Integer maxHeight) {
		StringBuffer name = new StringBuffer();
		if (maxWidth != null)
			name.append("maxW_" + maxWidth.toString() + "_");
		if (maxHeight != null)
			name.append("maxH_" + maxHeight.toString() + "_");
		name.append(fileName);
		return new ScaledImageResourceReference(name.toString(), new ScaledImageKey(key, maxWidth, maxHeight));
	}
	
	@Getter
	@Setter
	@EqualsAndHashCode
	public static class ScaledImageKey implements Serializable{
		
		private static final long serialVersionUID = 1L;
		
		private Serializable key;
		private Integer width;
		private Integer height;
		
		public ScaledImageKey(Serializable key, Integer w, Integer h) {
			this.key = key; // URL, Datastore ID, Other
			this.width = w;
			this.height = h;
		}
	}
	
	public static class ScaledImageResource extends DynamicImageResource {

		private static final long serialVersionUID = 1L;
		private ScaledImageKey lookupKey;
		
		public ScaledImageResource(ScaledImageKey lookupKey) {
			this.lookupKey = lookupKey;
			setCacheable(true);
		}

		@Override
		protected byte[] getImageData() {
			ScaledImage image = ImageService.get().getScaledImage(lookupKey);
			setFormat(image.getType() == null ? "jpg" : image.getType());
			setLastModifiedTime(image.getLastModified());
			return toImageData(image.getImage());
		}
		
		public BufferedImage getBufferedImage() {
			return ImageService.get().getScaledImage(lookupKey).getImage();
		}
	}
	
	public static class ScaledImageResourceReference extends ResourceReference {

		private static final long serialVersionUID = 1L;
		private ScaledImageKey lookupKey;

		public ScaledImageResourceReference(String name, ScaledImageKey lookup) {
			super(name);
			this.lookupKey = lookup;
		}
		
		@Override
		protected Resource newResource() {
			return new ScaledImageResource(lookupKey);
		}	
	}

	public static class ScaledImageComponent extends Image {
		
		private static final long serialVersionUID = 1L;

		public ScaledImageComponent(String id, ScaledImageResourceReference resourceReference) {
			super(id, resourceReference);
		}
		
		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			super.onComponentTag(tag);
			ScaledImageResource resource = (ScaledImageResource) getImageResource();
			
			if (resource.getBufferedImage() != null) {
				tag.put("width", resource.getBufferedImage().getWidth());
				tag.put("height", resource.getBufferedImage().getHeight());
			}
		}
	}
	
	@Getter
	@Setter
	public static class ScaledImage {
		
		private String type; // "png" or "jpg"
		private Time lastModified;
		private BufferedImage image;
		
		public ScaledImage(BufferedImage image) {
			this.image = image;
			this.lastModified = Time.now();
		}
	}
	

}
