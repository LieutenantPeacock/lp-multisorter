# Lt. Peacock's Multisorter

This library/tool provides the ability to sort various file types.

## Installation
JDK 1.8 is required at a minimum.

To build the project as a single executable JAR:

```
mvn clean package
```

To see instructions on usage:

```
java -jar lp-multisorter.jar
```

## XML
Do you want to compare the differences between different versions of generated XML/WSDL files, but are hindered by the unpredictable order? Lt. Peacock's Multisorter has you covered.

With this tool, you can sort both elements and their attributes by name to facilitate comparing with differencing tools.

### Command Line Usage
```
java -cp lp-multisorter.jar XMLSorter [inputFile] [outputFile]
```
The command above reads an XML file at the location specified by `inputFile` and outputs the sorted result to the location specified by `outputFile`. Elements are sorted lexicographically by node name and then by the `"name"` attribute, and their attributes are sorted lexicographically by name.

If `outputFile` is not specified, output goes to stdout; if `inputFile` is not specified, input is taken from stdin.

Example:

```
java -cp lp-multisorter.jar XMLSorter file.xml file_sorted.xml
```

Example with a Unix pipeline:

```
cat file.xml | java -cp lp-multisorter.jar XMLSorter > file_sorted.xml
```

### Programmatic Usage
Construct a `SortXMLEngine`:

```java
SortXMLEngine engine = new SortXMLEngine();
```

By default, `ElementComparator` is used to order elements, which sorts lexicographically by node name and then by the `"name"` attribute, and `AttributeComparator` is used to order attributes, which sorts lexicographically by attribute name.

To specify a custom order, pass two more arguments to the constructor: a `Comparator` for `ElementVO` objects and a `Comparator` for `ElementAttribute` objects. Use `ElementComparator.MAINTAIN_ORDER` to keep the original order of elements in the file and `AttributeComparator.MAINTAIN_ORDER` to keep the original order of attributes on each element.

```java
SortXMLEngine engine = new SortXMLEngine(elementComparator, attributeComparator);
```

Then, call `sort` with an `InputStream` to read the file from and an `OutputStream` to write the sorted result to.

```java
engine.sort(new FileInputStream("file.xml"), new FileInputStream("file_sorted.xml"));
```