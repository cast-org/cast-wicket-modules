/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.glossary;
import java.io.BufferedReader;
import java.io.FileReader;


/**
 *	Read a tab delimited file to create an XML content output to the console that can be used
 *	for a glossary file.
 * 
 * @author lynnmccormack
 *
 */
public class ParseGlossaryFile {

	public static void main(String[] argv) throws Exception{
		
		// add your file here - make sure there are no line feeds in the cells of the file
		BufferedReader fh =  new BufferedReader(new FileReader("/Users/lynnmccormack/Documents/project/BioComplexity/biotest/bio.txt"));

		// set the starting value to number the ids - unlikely to need ids
//		int imgId = 100;
//		int levelId = 200;

		// read the file and parse at each tab
		String s;
		while ((s=fh.readLine())!=null) {
			s = s.replaceAll("&", "&amp;");
			String f[] = s.split("\t");

			// set up your file here if your file has different input columns			
//			String unused0 = f[0].trim();
//			String unused1 = f10].trim();
			String word = f[2].trim();
			
			// add some kind of junk word to the end of your file if you want to exit cleanly
			if (word.equals("xxxxx")) break;
			String alternateWords[] = f[3].split(",");
			String shortDefinition = f[7];

			String scienceDefinition = f[8];
			String scienceContext = f[9];
			String explanation = f[10];
			String everydayDefinition = f[11];
			String seeAlso = f[12];

			String imageFile1 = f[13];
			String imageSource1 = f[15];
			String imageAlt1 = f[16];
			String imageLongDescription1 = f[17];
			String imageCaption1 = f[18];

			String imageFile2 = f[19];
			String imageSource2 = f[21];
			String imageAlt2 = f[22];
			String imageLongDescription2 = f[23];
			String imageCaption2 = f[24];

			// new entry for each word
			// set the id equal to all of the letters and numbers - strip invalid characters
			System.out.println("<level1 id=\""  + word.replaceAll("[^a-zA-Z0-9]", "") +  "\">");
			System.out.println("\t" + "<h1>" + word + "</h1>");

			// determine if there are any alternate words - set up list
			int i;
			boolean foundAltWords = false;
			for (i=0 ; i<alternateWords.length && alternateWords[0].trim().length() != 0 ; i++) {
				String altWord = alternateWords[i];
				if (i==0) {
					System.out.println("");
					System.out.println("\t" + "<list type=\"ul\">");
					foundAltWords = true;
				}
				String cleanedWord = altWord.replace("\"", "").trim();
				System.out.println("\t" + "\t" + "<li>" + cleanedWord + "</li>");
			}
			if (foundAltWords == true) {
				System.out.println("\t" + "</list>");
			}

			// get the short definition - minionly
			if (shortDefinition.indexOf("\"") == 0) 
				shortDefinition = shortDefinition.substring(1, shortDefinition.length()-1);
			if (shortDefinition != null && shortDefinition.trim().length() != 0) {
				System.out.println("");
				System.out.println("\t" + "<level2 class=\"minionly\">");					
//				System.out.println("\t" + "<level2 class=\"minionly\" id=\"" + "l" + levelId++ + "\">");					
				System.out.println("\t" + "\t" + "<p>" + shortDefinition.trim() + "</p>");
				System.out.println("\t" + "</level2>");
			}

			// determine if there are any images
			if (imageAlt1.indexOf("\"") == 0) 
				imageAlt1 = imageAlt1.substring(1, imageAlt1.length()-1);
			if (imageLongDescription1.indexOf("\"") == 0) 
				imageLongDescription1 = imageLongDescription1.substring(1, imageLongDescription1.length()-1);
			if (imageCaption1.indexOf("\"") == 0) 
				imageCaption1 = imageCaption1.substring(1, imageCaption1.length()-1);
			boolean foundImage1 = false;
			if (imageFile1 != null && imageFile1.trim().length() != 0) {
				foundImage1 = true;
				System.out.println("");
				System.out.println("\t" + "<imggroup>");					
				// image details: source, alt, id 
				System.out.println("\t" + "\t" + "<img alt=\"" + imageAlt1 + "\" " + 
//						"id=\"" + "i" + imgId + "\" " +					
						"src=\"images/glossary/" + imageFile1 + "\"" + " />" );
				
				// image long description
				if (imageLongDescription1 != null && imageLongDescription1.trim().length() != 0) {
					System.out.println("");
					System.out.println("\t" + "\t" + "<prodnote " +
//							"imgref=\"" + "i" +  imgId + "\" " + 
							"render=\"optional\">" + imageLongDescription1);
					System.out.println("\t" + "\t" + "</prodnote>" );
				}
				
				// image caption
				if (imageCaption1 != null && imageCaption1.trim().length() != 0) {
					System.out.println("");
					System.out.println("\t" + "\t" + "<caption" +
//							" imgref=\"" + "i" +  imgId + "\" +
							">" + imageCaption1);
					System.out.println("\t" + "\t" + "</caption>" );
					System.out.println("");
				}

				// image source
				if (imageSource1 != null && imageSource1.trim().length() != 0) {
					System.out.println("\t" + "\t" + "<caption" +
//							" imgref=\"" + "i" +  imgId + "\" +
							">" + "Source: " + imageSource1);
					System.out.println("\t" + "\t" + "</caption>" );
				}
				
				System.out.println("\t" + "</imggroup>");
//				imgId++;
			}

			// determine if there are any images
			if (imageAlt2.indexOf("\"") == 0) 
				imageAlt2 = imageAlt2.substring(1, imageAlt2.length()-1);
			if (imageLongDescription2.indexOf("\"") == 0) 
				imageLongDescription2 = imageLongDescription2.substring(1, imageLongDescription2.length()-1);
			if (imageCaption2.indexOf("\"") == 0) 
				imageCaption2 = imageCaption2.substring(1, imageCaption2.length()-1);
			if (imageFile2 != null && imageFile2.trim().length() != 0 && foundImage1) {
				System.out.println("");
				System.out.println("\t" + "<imggroup>");					
				// image details: source, alt, id 
				System.out.println("\t" + "\t" + "<img alt=\"" + imageAlt2 + "\" " + 
//						"id=\"" + "i" + imgId + "\" " +					
						"src=\"images/glossary/" + imageFile2 + "\"" + " />" );
				
				// image long description
				if (imageLongDescription2 != null && imageLongDescription2.trim().length() != 0) {
					System.out.println("");
					System.out.println("\t" + "\t" + "<prodnote " +
//							"imgref=\"" + "i" +  imgId + "\" " + 
							"render=\"optional\">" + imageLongDescription2);
					System.out.println("\t" + "\t" + "</prodnote>" );
				}
				
				// image caption
				if (imageCaption2 != null && imageCaption2.trim().length() != 0) {
					System.out.println("");
					System.out.println("\t" + "\t" + "<caption" +
//							" imgref=\"" + "i" +  imgId + "\" +
							">" + imageCaption2);
					System.out.println("\t" + "\t" + "</caption>" );
					System.out.println("");
				}

				// image source
				if (imageSource2 != null && imageSource2.trim().length() != 0) {
					System.out.println("\t" + "\t" + "<caption" +
//							" imgref=\"" + "i" +  imgId + "\" +
							">" + "Source: " + imageSource2);
					System.out.println("\t" + "\t" + "</caption>" );
				}

				System.out.println("\t" + "</imggroup>");					
//				imgId++;
			}

			// get the other definitions
			if (scienceDefinition.indexOf("\"") == 0) 
				scienceDefinition = scienceDefinition.substring(1, scienceDefinition.length()-1);

			if (scienceContext.indexOf("\"") == 0) 
				scienceContext = scienceContext.substring(1, scienceContext.length()-1);

			if (explanation.indexOf("\"") == 0) 
				explanation = explanation.substring(1, explanation.length()-1);

			if (everydayDefinition.indexOf("\"") == 0) 
				everydayDefinition = everydayDefinition.substring(1, everydayDefinition.length()-1);

			if (scienceDefinition != null && scienceDefinition.trim().length() != 0) {
				System.out.println("");
//				System.out.println("\t" + "<level2 id=\"" + "l" + levelId++ + "\">");					
				System.out.println("\t" + "<level2>");					
				System.out.println("\t" + "\t" + "<h2>Science Definition</h2>");
				System.out.println("\t" + "\t" + "<p>" + scienceDefinition.trim() + "</p>");
				System.out.println("\t" + "</level2>");
			}
			
			if (scienceContext != null && scienceContext.trim().length() != 0) {
				System.out.println("");
//				System.out.println("\t" + "<level2 id=\"" + "l" + levelId++ + "\">");					
				System.out.println("\t" + "<level2>");					
				System.out.println("\t" + "\t" + "<h2>Science Context</h2>");
				System.out.println("\t" + "\t" + "<p>" + scienceContext.trim() + "</p>");
				System.out.println("\t" + "</level2>");
			}
			
			if (explanation != null && explanation.trim().length() != 0) {
				System.out.println("");
//				System.out.println("\t" + "<level2 id=\"" + "l" + levelId++ + "\">");					
				System.out.println("\t" + "<level2>");					
				System.out.println("\t" + "\t" + "<h2>Explanation</h2>");
				System.out.println("\t" + "\t" + "<p>" + explanation.trim() + "</p>");
				System.out.println("\t" + "</level2>");
			}

			if (everydayDefinition != null && everydayDefinition.trim().length() != 0) {
				System.out.println("");
//				System.out.println("\t" + "<level2 id=\"" + "l" + levelId++ + "\">");					
				System.out.println("\t" + "<level2>");					
				System.out.println("\t" + "\t" + "<h2>Everyday Definition</h2>");
				System.out.println("\t" + "\t" + "<p>" + everydayDefinition.trim() + "</p>");
				System.out.println("\t" + "</level2>");
			}

			// determine if there are any See Also words - set up paragraphs (links?)
			boolean foundSeeAlsoWord = false;
			if (seeAlso != null && seeAlso.trim().length() != 0) {
				foundSeeAlsoWord = true;
				System.out.println("");
//				System.out.println("\t" + "<level2 id=\"" + "l" + levelId++ + "\">");					
				System.out.println("\t" + "<level2>");					
				System.out.println("\t" + "\t" + "<h2>See Also</h2>");
				System.out.println("\t" + "\t" + "<p>");
			}
			String seeAlsoWords[] = seeAlso.split(",");
			for (i=0 ; i<seeAlsoWords.length && seeAlsoWords[0].trim().length() != 0 ; i++) {
				String seeAlsoWord = seeAlsoWords[i];
				String cleanedWord = seeAlsoWord.replace("\"", "").trim();
				if (i == 0)
					System.out.print("\t" + "\t");
				System.out.print(cleanedWord); 
				if (i < seeAlsoWords.length-1)		
					System.out.print(", ");
			}
			System.out.println("");
			if (foundSeeAlsoWord == true) {
				System.out.println("\t" + "\t" + "</p>");
				System.out.println("\t" + "</level2>");
			}
			
			
			System.out.println("</level1>");
			{
				System.out.println("");
			}
		}
		fh.close();
	}
}