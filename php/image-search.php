<?

require_once("parser.inc.php");
parser_no_timeout();

$data_json = "data/data.json.txt";
$product_images_json = "data/product_images.json.txt";
$data = util_json_read($data_json);
$products = $data["products"];

$images = array();

$i = 0;
foreach($products as $product) {
	$query = $product["brand"] . " " . $product["name"];
	$query = urlencode(parser_preg_remove("[^0-9a-zA-Z ]", $query));
	$result_json = util_download("/cache/duckduckgo/".$query.".html", "https://duckduckgo.com/i.js?o=json&q=" . $query);

	$result = util_json_decode($result_json);
	$results = $result["results"];

	$first = $results[0];
	$image = array();
	$image["name"] = $product["name"];
	$image["brand"] = $product["brand"];
	$image["source"] = $first["s"];
	$image["width"] = $first["iw"];
	$image["height"] = $first["ih"];
	$image["url"] = $first["j"];

	$images[] = $image;
	$i++;
	if($i % 100 == 0) {
		$result = array();
		$result["images"] = $images;
		util_json_write($product_images_json, $result);
	}
}

$result = array();
$result["images"] = $images;
util_json_write($product_images_json, $result);

?>