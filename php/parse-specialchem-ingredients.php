<?

require_once("parser.inc.php");
parser_no_timeout();

$source_root = "http://www.specialchem4cosmetics.com/services/inci/";
$source_search = $source_root . "index.aspx?p=";
$source_ingredient = $source_root . "ingredient.aspx?id=";

$file_specialchem_json = "data/specialchem-ingredients.json.txt";
$file_specialchem_search_json = "data/specialchem-search.json.txt";

util_create_dir("data");

function getIngredientFromId($id) {
	global $source_ingredient;
	$page_html = util_download("/cache/specialchem/ingredient/".$id.".html", $source_ingredient.$id);
	$page_html = parser_remove_newline($page_html);

	$result_html = parser_match("inciingredienttable(.*?)<\\/table>", $page_html);
	$result_html = $result_html[1];
	$ingredient_data = parser_match_all("<tr>(.*?)<\\/tr>", $result_html);
	$ingredient_data = $ingredient_data[1];

	$ingredient = array();
	$ingredient["inci_name"] = trim(strip_tags($ingredient_data[0]));

	$ingredient_data = parser_match_all("<td id=\"([^\"]*)\"[^>]*>(.*?)<\\/td>", $result_html);
	$ingredient_data = util_rotate_array($ingredient_data, 1, 2);

	$ingredient["cas_number"] = trim(strip_tags($ingredient_data["inci_CASNumber"]));
	$ingredient["inn_name"] = trim(strip_tags($ingredient_data["inci_EINECS_ELINCS"]));
	$ingredient["description"] = trim(strip_tags($ingredient_data["inci_Description"]));
	$ingredient["restriction"] = trim(strip_tags($ingredient_data["inci_Restriction"]));

	$ingredient["restriction"] = parser_preg_remove("Last update on [0-9]+ [a-zA-Z]+ [0-9]+ - ", $ingredient["restriction"]);
	$ingredient["restriction"] = parser_preg_remove("no restriction", $ingredient["restriction"]);

	$ingredient_functions = parser_match_all("<a[^>]*>(.*?)<\\/a>", $ingredient_data["inci_Functions"]);
	$ingredient["functions"] = implode(",", $ingredient_functions[1]);

	return $ingredient;
}

$search_ids = array();

for($i = 1; $i < 647; $i++) {
	$page_html = util_download("/cache/specialchem/search/".$i.".html", $source_search.$i);
	$page_html = parser_remove_newline($page_html);

	$result_html = parser_match("<!-- RESULTS LIST -->(.*?)<\\/table>", $page_html);
	$ingredient_ids = parser_match_all("<a href=\"ingredient.aspx\\?id=([0-9]*)\"", $result_html[1]);

	$search_ids = array_merge($search_ids, $ingredient_ids[1]);
}

util_json_write($file_specialchem_search_json, $search_ids);


$ingredients = array();

foreach($search_ids as $id) {
	$ingredient = getIngredientFromId($id);
	$ingredients[] = $ingredient;
}

$result = array();
$result["ingredients"] = $ingredients;

util_json_write($file_specialchem_json, $result);

?>