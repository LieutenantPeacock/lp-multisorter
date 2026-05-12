package com.ltpeacock.sorter.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

import com.ltpeacock.sorter.ContentSorter;

/**
 * <p>Engine for sorting YAML by key.</p>
 * Sorting is performed on SnakeYAML's node tree rather than the loaded object graph, which preserves
 * comments, scalar styles and tags so that the output differs from the input only in key order.
 * @author LieutenantPeacock
 *
 */
public class SortYAMLEngine implements ContentSorter {
	private final Comparator<Node> keyComparator;
	private boolean recursive = true;
	private int indent = 2;

	/**
	 * Constructs a {@code SortYAMLEngine} that sorts the keys in lexicographical order.
	 */
	public SortYAMLEngine() {
		this(Comparator.comparing(SortYAMLEngine::keyText));
	}

	/**
	 * Constructs a {@code SortYAMLEngine} that sorts the keys using the {@code keyComparator}.
	 * The comparator receives the key {@link Node} of each mapping entry, which allows ordering
	 * by scalar value, tag or any other node property.
	 * @param keyComparator The {@link Comparator} for sorting mapping keys.
	 */
	public SortYAMLEngine(final Comparator<Node> keyComparator) {
		this.keyComparator = keyComparator;
	}

	/**
	 * Sorts the YAML from an {@link InputStream} and prints the result to the given {@link OutputStream}.
	 * Each document in the stream (documents are separated by {@code ---}) is sorted independently.
	 * @param is The InputStream to read the YAML stream from.
	 * @param os The OutputStream to write the sorted YAML to.
	 * @throws IOException If there is an error in reading/writing data.
	 */
	@Override
	public void sort(final InputStream is, final OutputStream os) throws IOException {
		final LoaderOptions loaderOptions = new LoaderOptions();
		loaderOptions.setProcessComments(true);
		final DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setIndent(indent);
		dumperOptions.setProcessComments(true);
		dumperOptions.setAnchorGenerator(Node::getAnchor);
		final Yaml loader = new Yaml(loaderOptions);
		final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
		final Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
		final Serializer serializer = new Serializer(new Emitter(writer, dumperOptions), new Resolver(),
				dumperOptions, null);
		serializer.open();
		for (final Node document : loader.composeAll(reader)) {
			sort(document);
			serializer.serialize(document);
		}
		serializer.close();
		writer.flush();
	}

	/**
	 * Sorts the given node in place. If the node is a mapping, its entries are reordered by key;
	 * if it is a sequence, element order is preserved but each element is sorted when recursive
	 * sorting is enabled. Scalars are left unchanged.
	 * @param node The node to sort.
	 */
	public void sort(final Node node) {
		if (node instanceof MappingNode) {
			sortMapping((MappingNode) node);
		} else if (node instanceof SequenceNode) {
			sortSequence((SequenceNode) node);
		}
	}

	private void sortMapping(final MappingNode node) {
		final List<NodeTuple> tuples = new ArrayList<>(node.getValue());
		tuples.sort(Comparator.comparing(NodeTuple::getKeyNode, keyComparator));
		node.setValue(tuples);
		if (recursive) {
			for (final NodeTuple tuple : tuples) {
				sort(tuple.getValueNode());
			}
		}
	}

	private void sortSequence(final SequenceNode node) {
		if (recursive) {
			for (final Node item : node.getValue()) {
				sort(item);
			}
		}
	}

	private static String keyText(final Node node) {
		return node instanceof ScalarNode ? ((ScalarNode) node).getValue() : node.toString();
	}

	/**
	 * Set whether the sorting should be recursive.
	 * If set to {@code true}, the keys of all mappings contained within the root node
	 * will also be sorted, including mappings nested inside sequences.
	 * If set to {@code false}, only the keys of the root mapping are sorted.
	 * The default is {@code true}.
	 * @param recursive Whether or not the sorting should be recursive.
	 */
	public void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * Set the amount of indent used when outputting the YAML. The default is {@code 2}.
	 * @param indent The number of spaces used for one indent level.
	 */
	public void setIndent(final int indent) {
		this.indent = indent;
	}
}
