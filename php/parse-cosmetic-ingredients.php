<?

require_once("parser.inc.php");
parser_no_timeout();

$source_root = "http://www.cosmetic-ingredients.net/";
$source_index = $source_root . "index.php";
$source_brand = $source_root . "product.php?type=' OR '1'='1&brand=";

$file_cosmetic_ingredients_json = "data/cosmetic-ingredients.json.txt";

util_create_dir("data");


$corrections = array(
	"(Vaccinium Myrtillus (Bilberry) Extract" => "Vaccinium Myrtillus (Bilberry) Extract",
	", Organic Lemon Peel Oil)" => ", Organic Lemon Peel Oil",
	);


$index_html = util_download("/cache/cosmetic-ingredients/index.html", $source_index);
$index_html = parser_remove_newline($index_html);

$index_table = parser_match("<select name=[^\\>]*>(.*?)<\\/select>", $index_html);
$index_rows = parser_match_all("<option value=\"product.php\?brand=(.*?)<\\/option>", $index_table[1]);

$brands = array();
$products = array();
$ingredients = array();
$unknown_ingredients = array();

function getIngredients($ingredients_html) {
	$ingredients_found = parser_match_all("<a href='ingredient.php\\?ingredient_id=([0-9]*)' title='([^']*)'.*?>(.*?)<\\/a>", $ingredients_html);

	$ingredients = array();
	for ($i = 0; $i < count($ingredients_found[0]); $i++) {
		$ingredient = array();
		$ingredient_id = intval($ingredients_found[1][$i]);
		$ingredient["name"] = $ingredients_found[3][$i];
		$ingredient["short_desc"] = $ingredients_found[2][$i];
		$ingredients[$ingredient_id] = $ingredient;
	}
	return $ingredients;
}

function getUnknownIngredients($ingredients_html) {
    global $corrections;
	$ingredients = array();
	$result = preg_replace("/<a href='ingredient.php\\?ingredient_id=([0-9]*)' title='([^']*)'.*?>(.*?)<\\/a>/", "", $ingredients_html);
	$result = strip_tags($result);
	$result = parser_str_replace_dict($corrections, $result);
	$replaces = array(
		"&amp;" => ",",
		";" => ",",
		"\\/" => ",",
		"Key Ingredients: " => "",
		);
	$result = parser_preg_replace_dict($replaces, $result);
	$result = rtrim($result, ".");
	$items = preg_split('/,(?=[^\)]*(?:\(|$))/', $result);
	foreach($items as $ingredient) {
		$ingredient = trim($ingredient);
		if($ingredient == "" || $ingredient == "n/a") {
			continue;
		}

		if(substr($ingredient, -1) == "%" && is_numeric(substr($ingredient, 0, strlen($ingredient)-1))) {
			continue;
		}

		$ingredients[] = $ingredient;
	}
	return $ingredients;
}

foreach($index_rows[1] as $value) {
	$brandDetails = explode("\"> ", $value);
	$brandId = intval($brandDetails[0]);
	$brand = array();
	$brand["name"] = $brandDetails[1];
	if($brandId >= 0) {
		$brands[$brandId] = $brand;
	}
}

foreach($brands as $brandId => $brandName) {
	$brand_html = util_download("/cache/cosmetic-ingredients/brands/".$brandId.".html",	$source_brand.$brandId);
	$brand_html = parser_remove_newline($brand_html);
	$brand_website = parser_match("<strong>Official Web Site:(.*?)<\\/p>", $brand_html);
	$brand_website = parser_match("<a href=\"(.*?)\">", $brand_website[1]);
	$brands[$brandId]["website"] = $brand_website[1];

	$products_html = parser_match("<Products>(.*?)<\\/Products>", $brand_html);
	$product_html_list = parser_match_all("<Product>(.*?)<\\/Product>", $products_html[1]);

	foreach($product_html_list[1] as $product_html) {
		$product = array();
		$product["brand"] = $brandId;

		$product_id = parser_match("<a name=\"([0-9]*)\">", $product_html);
		$product_id = intval($product_id[1]);

		$product_image = parser_match("<img class=\"FigureImage\" src=\"([^\"]*?)\">", $product_html);
		$product["image"] = $product_image[1];

		$product_type_and_name = parser_match("<ProductName><a href=.*?type=([a-zA-Z]*).*?>(.*?)<\\/a><\\/ProductName>", $product_html);
		$product["type"] = $product_type_and_name[1];
		$product["name"] = $product_type_and_name[2];

		$product_active_ingredients = parser_match("ActiveIngredientList.*?<Value>(.*?)<\\/Value>", $product_html);
		$product_active_ingredients = $product_active_ingredients[1];
		$product_unknown_active_ingredients = getUnknownIngredients($product_active_ingredients);
		$product_active_ingredients = getIngredients($product_active_ingredients);

		$product_ingredients = parser_match("\"IngredientList.*?<Value>(.*?)<\\/Value>", $product_html);
		$product_ingredients = $product_ingredients[1];
		$product_unknown_ingredients = getUnknownIngredients($product_ingredients);
		$product_ingredients = getIngredients($product_ingredients);

		$product["active_ingredients"] = array_keys($product_active_ingredients);
		$product["ingredients"] = array_keys($product_ingredients);
		$product["unknown_ingredients"] = $product_unknown_ingredients;
		$product["active_unknown_ingredients"] = $product_unknown_active_ingredients;

		$ingredients = util_array_merge_assoc($ingredients, $product_active_ingredients, $product_ingredients);
		$product_unknown_ingredients = array_merge($product_unknown_active_ingredients, $product_unknown_ingredients);
		$unknown_ingredients = array_merge($unknown_ingredients, $product_unknown_ingredients);

		$products[$product_id] = $product;
	}
}

$unknown_ingredients = util_sort_unique_string($unknown_ingredients);

$result = array();
$result["brands"] = $brands;
$result["products"] = $products;
$result["ingredients"] = $ingredients;
$result["unknown_ingredients"] = $unknown_ingredients;

util_json_write($file_cosmetic_ingredients_json, $result);

?>