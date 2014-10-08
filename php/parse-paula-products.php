<?

require_once("parser.inc.php");
parser_no_timeout();

$source_root = "http://www.paulaschoice.com";
$source_product_list = "http://www.paulaschoice.com/beautypedia-skin-care-reviews?sort=product&direction=asc&pageSize=100&pageNumber=";
$file_product_urls = "data/paula-product-urls.json.txt";
$file_products = "data/paula-products.json.txt";

util_create_dir("data");

function clean_paula_page($html) {
	$html = parser_preg_remove("<input type=\"hidden\"[^>]*>", $html);
	$html = parser_preg_remove("<head.*?<\\/head>", $html);
	$html = parser_preg_remove("<script.*?<\\/script>", $html);
	return $html;
}

$urls = util_json_read($file_product_urls);

//Find max
$index_html = util_download("/cache/paula/list/1.html", $source_product_list."1", "clean_paula_page");
$index_html = parser_remove_newline($index_html);
$index_table = parser_match("<select name=\"[^\"]*pageNumberList\"(.*?)<\\/select>", $index_html);
$index_rows = parser_match_all("<option[^>]*>(.*?)<\\/option>", $index_table[1]);
$index_rows = $index_rows[1];


$numpages = $index_rows[count($index_rows) - 1];
$numpages = intval($numpages);

for ($i = 1; $i <= $numpages; $i++) {
	$page_html = util_download("/cache/paula/list/".$i.".html", $source_product_list.$i, "clean_paula_page");
	$page_html = parser_remove_newline($page_html);
	$page_table = parser_match("Size<\\/th>(.*)<\\/tbody>", $page_html);
	$page_rows = parser_match_all("<tr>(.*?)<\\/tr>", $page_table[1]);

	foreach($page_rows[1] as $page_row) {
		$page_cols = parser_match_all("<td>(.*?)<\\/td>", $page_row);
		$page_cols = $page_cols[1];
		//0 = checkbox
		//1 = rating
		//2 = product
		//3 = brand
		//4 = category
		//5 = price
		//6 = size

		$product_link = parser_match("href=\"([^\"]*)\"", $page_cols[2]);

		$urls[] = $product_link[1];
	}
}

$urls = array_unique($urls);

util_json_write($file_product_urls, $urls);

$products = array();

foreach($urls as $url) {
	$filename = urlencode(str_replace("/beautypedia-skin-care-reviews/by-brand/", "", $url));
	$url = parser_urlencode_url_pieces($url);
	$page_html = util_download("/cache/paula/product/".$filename.".html", $source_root . $url, "clean_paula_page");
	$page_html = parser_remove_newline($page_html);
	$page_html = parser_match("<div class=\"grid-row clearfix\">(.*?)id=\"leavingSite\"", $page_html);
	$page_html = $page_html[1];

	$product_name = parser_match("<div class=\"u-miscellaneous-pagetitle clearfix\">(.*?)<\\/div>", $page_html);
	$product_name = trim(strip_tags($product_name[1]));

	$product_brand = parser_match("<div class=\"brand\">by <a[^>]*>(.*?)<\\/a>", $page_html);
	$product_brand = trim(strip_tags($product_brand[1]));

	$product_claims = parser_match("<div id=\"[^\"]*pnlTabBodyClaims\"[^>]*>(.*?)<\\/div>", $page_html);
	$product_claims = trim(strip_tags($product_claims[1]));

	$product_ingredients = parser_match("<div id=\"[^\"]*pnlTabBodyIngredients\"[^>]*>(.*?)<\\/div>", $page_html);
	$product_ingredients = trim(strip_tags($product_ingredients[1]));

	$product = array();
	$product["name"] = $product_name;
	$product["brand"] = $product_brand;
	$product["claims"] = $product_claims;
	$product["ingredients"] = $product_ingredients;
	$products[] = $product;
}

$result = array();
$result["products"] = $products;

util_json_write($file_products, $result);

?>