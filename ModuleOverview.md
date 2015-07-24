This diagram shows the dependencies between the various modules and selected dependencies on third-party libraries.

![http://cast-wicket-modules.googlecode.com/svn/wiki/images/cwm-modules.png](http://cast-wicket-modules.googlecode.com/svn/wiki/images/cwm-modules.png)

The blue boxes are CAST modules that are not included in this project.
  * ISI and its Example Application are part of the [UDL Curriculum Toolkit](http://code.google.com/p/udl-curriculum-toolkit), which makes use of all of these modules in a complete webapp.
  * The drawing tool and cwm wrapper for it are available in [the cwm-drawtool Google Code project](http://code.google.com/p/cwm-drawtool).
  * The audio applet will be uploaded once it is a little more complete and stable.

The orange boxes are the modules that are included in this project:
  * [cwm-xml](CwmXml.md): use transformed XML content on web pages
  * [cwm-glossary](CwmGlossary.md): use an XML file as a glossary
  * [cwm-dav](CwmDav.md): use content stored on a WebDAV server
  * [cwm-mediaplayer](CwmMediaplayer.md): use the JW Player for video and audio
  * [cwm-data](CwmData.md): common database classes and components to manage them
  * [cwm-tag](CwmTag.md): support for tagging, built on top of the cwm-data structures
  * [cwm-base](CwmBase.md): lightweight dependency with a couple of interfaces to avoid everything having to depend on cwm-data.
  * [cwm-audioapplet](CwmAudioapplet.md): Java/Javascript audio recorder for web pages
  * [cwm-components](CwmComponents.md): miscellaneous useful wicket components

The white boxes are third party dependencies (not exhaustively listed, just the most significant ones).