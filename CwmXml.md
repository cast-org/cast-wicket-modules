# Introduction #

The cwm-xml module allows applications to read XML files and use them, or pieces of them, as sources for content to be displayed on pages - or for other purposes.  Currently it only supports DTBook XML, but it is built so that we can add other schemas as necessary.

# Details #

`XmlService` is the entry point for most operations.  It maintains references to all known XML files and transformations, manages the cache, and keeps some configuration information.

`XmlDocument` represents a document.  It is optional to use this as your XML may be stored as fragments in a database or some other scheme; but if you have files on disk or in a DAV store (or otherwise accessible as a wicket Resource) you can call `XmlService`'s `loadXmlDocument()` method to read and parse the document.  You must specify a parser (implementing `IXmlParser`); at the moment the existing parser is `DtbookParser`. The parser constructs from the document a tree of `XmlSection` elements. `XmlDocuments` update themselves when the underlying resource is updated, and are able to notify other objects when this happens - simply pass in an object implementing `IDocumentObserver`.  For example, this allows additional information to be extracted from the document for the application's use, and that information can be updated as necessary (see [cwm-glossary](CwmGlossary.md) for an example).   Documents can be grouped into an `XmlDocumentList` if you need to iterate over a set of them, paginate them consecutively, etc.

`XmlSection` represents a pointer to a particular XML element inside an `XmlDocument` and implements various methods to get information about that element and its content, and to navigate to other elements around it.  They implement the `IXmlPointer` interface which is the general case of a pointer to an XML element.  There is an `XmlSectionModel` that is appropriate for passing around `XmlSection`s without actually keeping XML data in the wicket session (you're not required to use this model; there's also an interface `ICacheableModel` which has just the required features).

Often you need to transform the XML content into some other form in order to use it - most often into HTML for display on a web page.  `IDomTransformer` is the general case of a class that implements some sort of transformation (takes a DOM node and returns another DOM node.  It is allowed to either destructively change the DOM or return a new one).  Specific transformers included in cwm-xml:

  * `XslTransformer` -- transform based on an XSLT stylesheet.
  * `EnsureUniqueWicketIds` -- appends numbers to wicket:id attributes to make them unique
  * `FilterElements` -- extract a piece of the input XML based on an XPath expression
  * `TransformChain` -- concatenates one or more other `IDomTransformers`.

the [cwm-glossary](CwmGlossary.md) module adds another one:

  * `GlossaryTransformer` -- finds and marks up glossary words in running text

all transformers should be self-updating (eg, `XslTransformer` will re-read the underlying XSLT stylesheet when it changes) and can report their last modification date.  Like XML documents, transformers are intended to be registered with `XmlService` so they can be referred to by name and reloaded as necessary.  Transformers can also accept parameters that modify their behavior (eg, the XPath expression to use for `FilterElements`, or parameters to be passed to the XSLT stylesheet for the `XslTransformer`).

`XmlService` is in charge of applying transformations to XML data.  Call the `getTransformed(mXmlPtr, transformName, params)` method with (1) a model that resolves to an `IXmlPointer` object, (2) the registered name of a transform, and (3) optionally, any parameters.  The model used must implement `ICacheableModel`, which means that it can be queried for a cache key and the last modification time of the underlying object.  This allows `XmlService` to cache the output of the transform, and periodically check to see if the cache is still valid. Whenever either the XML or the transformer is updated, the cache will automatically be invalidated and an up to date result returned.

Several wicket components are provided by cwm-xml as well: `XmlComponent` takes a `ICacheableModel<? extends IXmlPointer>` as its model, and uses the content of that XML element as transformed by a given transformer as its markup.  Wicket components within it can be dynamically added by subclassing this component and overriding its `getDynamicComponent()` method.

`ChildListview` is a simple extension of `ListView` that uses the children of a given `XmlSection` as the list.

`NavListView` extends that for use as a navigation structure; it defaults to creating a list of links for each of the `XmlSection`'s children, using the titles from the XML.  This is easily extended to a wide variety of navigational needs.