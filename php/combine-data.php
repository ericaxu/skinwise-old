<?

require_once("parser.inc.php");
parser_no_timeout();

$file_specialchem_json = "data/specialchem-ingredients.json.txt";
$file_paula_products_json = "data/paula-products.json.txt";
$file_inci_json = "data/inci.json.txt";
$data_json = "data/data.json.txt";

$specialchem_ingredients = util_json_read($file_specialchem_json);
$inci_ingredients = util_json_read($file_inci_json);
$paula_products = util_json_read($file_paula_products_json);

//Remove paula's products without ingredient list
foreach($paula_products["products"] as $key => $product) {
	if($product["ingredients"] == "" && $product["key_ingredients"] == "") {
		unset($paula_products["products"][$key]);
	}
}

$paula_products["products"] = array_values($paula_products["products"]);

//Merge functions
$ingredient_functions = array_merge($specialchem_ingredients["ingredient_functions"], $inci_ingredients["ingredient_functions"]);

$data = array(
	"ingredients" => $specialchem_ingredients["ingredients"],
	"ingredient_functions" => $ingredient_functions,
	"ingredient_abbreviations" => $inci_ingredients["ingredient_abbreviations"],
	"products" => $paula_products["products"]
);

util_json_write($data_json, $data);

parser_echo_count($data["ingredients"], "Ingredients");
parser_echo_count($data["ingredient_functions"], "Functions");
parser_echo_count($data["ingredient_abbreviations"], "Abbreviations");
parser_echo_count($data["products"], "Products");

?>