package src.controllers.util;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import play.twirl.api.Html;

public class Prettyfy {
	public static Html prettify(Html html) {
		HtmlCompressor compressor = new HtmlCompressor();
		String output = html.body().trim();

		compressor.setPreserveLineBreaks(true);

		output = compressor.compress(output);
		return Html.apply(output);
	}
}
