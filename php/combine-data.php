<?

require_once("parser.inc.php");
parser_no_timeout();

$file_specialchem_json = "data/specialchem-ingredients.json.txt";
$file_paula_products_json = "data/paula-products.json.txt";
$file_inci_to_cosdna_json = "data/inci-to-cosdna.json.txt";
$product_images_json = "data/product_images.json.txt";
$file_cosdna_json = "data/cosdna.json.txt";
$file_inci_json = "data/inci.json.txt";
$data_json = "data/data.json.txt";

$specialchem_ingredients = util_json_read($file_specialchem_json);

//Load alternate names from cosdna
$cosdna_mapping = util_json_read($file_inci_to_cosdna_json);
$cosdna_ingredients = util_json_read($file_cosdna_json);
$cosdna_ingredients = $cosdna_ingredients["ingredients"];
$ingredients = $specialchem_ingredients["ingredients"];

foreach($ingredients as $key => $ingredient) {
	$ingredient_name = strtoupper($ingredient["name"]);
	if($cosdna_mapping[$ingredient_name]) {
		$cosdna_id = $cosdna_mapping[$ingredient_name];
		$cosdna_ingredient = $cosdna_ingredients[$cosdna_id];
		$names = $cosdna_ingredient["names"];
		if($names) {
			$ingredient["names"] = $names;
			$ingredient["names"][] = $cosdna_ingredient["name"];
			$ingredients[$key] = $ingredient;
		}
	}
}

$ingredients = array_values($ingredients);

unset($cosdna_mapping);
unset($cosdna_ingredients);

$paula_products = util_json_read($file_paula_products_json);

//Remove paula's products without ingredient list
foreach($paula_products["products"] as $key => $product) {
	if($product["ingredients"] == "" && $product["key_ingredients"] == "") {
		unset($paula_products["products"][$key]);
	}
}

$products = array_values($paula_products["products"]);
unset($paula_products);

//Merge functions
$inci_ingredients = util_json_read($file_inci_json);
$ingredient_functions = array_merge($specialchem_ingredients["ingredient_functions"], $inci_ingredients["ingredient_functions"]);
unset($inci_ingredients);

$images = util_json_read($product_images_json);
$images = $images["images"];
for ($i=0; $i < count($products); $i++) {
	if($images[$i]["brand"] == $products[$i]["brand"] && $images[$i]["name"] == $products[$i]["name"]) {
		$products[$i]["image"] = $images[$i]["url"];
	}
	else {
		echo "Product & Image mismatch " . $i;
		print_r($images[$i]);
		print_r($products[$i]);
		exit();
	}
}

$data = array(
	"ingredients" => $ingredients,
	"ingredient_functions" => $ingredient_functions,
	"products" => $products
);

util_json_write($data_json, $data);

parser_echo_count($data["ingredients"], "Ingredients");
parser_echo_count($data["ingredient_functions"], "Functions");
parser_echo_count($data["ingredient_abbreviations"], "Abbreviations");
parser_echo_count($data["products"], "Products");

?>