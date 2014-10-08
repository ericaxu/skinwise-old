<?

require_once("parser.inc.php");
parser_no_timeout();

//Documentation: http://eur-lex.europa.eu/search.html?qid=1412217293942&text=32006D0257&scope=EURLEX&type=quick&lang=en
$source_inci_data = "http://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=CELEX:32006D0257&rid=1";
$file_inci_json = "data/inci.json.txt";

util_create_dir("data");

$index_html = util_download("/cache/inci/index.html", $source_inci_data);
$index_html = parser_remove_newline($index_html);

$index_table = parser_match("Increases or decreases the viscosity of cosmetics(.*)The part entitled Nomenclature Conventions", $index_html);
$index_rows = parser_match_all("<tr class=\"table\">(.*?)<\\/tr>", $index_table[1]);

//Parse ingredients
$ingredients = array();
$ingredient_functions = array();
$ingredient_abbreviations = array();
foreach($index_rows[1] as $index_row) {
	$index_cells = parser_match_all("<td [^>]*>(.*?)<\\/td>", $index_row);
	$index_cells = $index_cells[1];
	foreach($index_cells as $key => $index_cell) {
		$index_cells[$key] = parser_remove_nbsp(trim(strip_tags($index_cell)));
	}

	//Skip header
	if($index_cells[0] == "INCI name") {
		continue;
	}

	$ingredient = array();
	$ingredient["inci_name"] = $index_cells[0];
	$ingredient["inn_name"] = $index_cells[1];
	$ingredient["ph_eur_name"] = $index_cells[2];
	$ingredient["cas_no"] = $index_cells[3];
	$ingredient["ec_no"] = $index_cells[4];
	$ingredient["iupac_name"] = $index_cells[5];
	$ingredient["restriction"] = $index_cells[6];
	$ingredient["functions"] = $index_cells[7];

	//Corrections
	$ingredient["functions"] = str_replace("Sufactant", "Surfactant", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("Skin protectant", "Skin protecting", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("Skin conditioning&rsquo;", "Skin conditioning", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("viscosity/controlling", "viscosity controlling", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("foaming cleansing", "foaming, cleansing", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("foaming cleansing", "foaming, cleansing", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("Hair waving", "hair waving or straightening", $ingredient["functions"]);
	$ingredient["functions"] = str_replace("hair waving or straightening or straightening", "hair waving or straightening", $ingredient["functions"]);

	$ingredients[] = $ingredient;
}

//Parse functions
$index_function_html = parser_match("functions are defined as follows(.*)INCI name", $index_html);
$index_functions = parser_match_all("<table(.*?)<\\/table>", $index_function_html[1]);

foreach($index_functions[1] as $index_row) {
	$function_name = parser_match("<span class=\"bold\">(.*?)<\\/span>", $index_row);
	$function_sections = parser_match_all("<p class=\"normal\">(.*?)<\\/p>", $index_row);
	$function_sections = $function_sections[1];

	$function = array();
	$function["name"] = $function_name[1];
	$function["description"] = $function_sections[count($function_sections)-1];
	$ingredient_functions[] = $function;
}

//Parse abbreviations
$index_abbreviation_html = parser_match("cosmetic ingredients in the Inventory(.*)<\\/table>", $index_html);
$index_abbreviations = parser_match_all("<tr class=\"table\">(.*?)<\\/tr>", $index_abbreviation_html[1]);

foreach($index_abbreviations[1] as $index_row) {
	$abbreviation_sections = parser_match_all("<p class=\"tbl-txt\">(.*?)<\\/p>", $index_row);
	$abbreviation_sections = $abbreviation_sections[1];

	$abbreviation = array();
	$abbreviation["shorthand"] = $abbreviation_sections[0];
	$abbreviation["full"] = $abbreviation_sections[1];
	$ingredient_abbreviations[] = $abbreviation;
}

$result = array();
$result["ingredients"] = $ingredients;
$result["ingredient_functions"] = $ingredient_functions;
$result["ingredient_abbreviations"] = $ingredient_abbreviations;

util_json_write($file_inci_json, $result);

?>