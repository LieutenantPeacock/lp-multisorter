/**
 * Main class for printing usage instructions.
 * @author LieutenantPeacock
 *
 */
public class SortHelp {
	public static void main(final String[] args) {
		System.out.println("Lt. Peacock's Multisorter");
		System.out.println();
		System.out.println("To sort XML          : java -cp lp-multisorter.jar XMLSorter         [inputFile] [outputFile]");
		System.out.println("To sort OpenAPI JSON : java -cp lp-multisorter.jar OpenApiJSONSorter [inputFile] [outputFile]");
		System.out.println();
		System.out.println("If outputFile is not specified, the output is to stdout.");
		System.out.println("If inputFile is not specified, input is taken from stdin.");
	}
}
