<?

require_once("parser.inc.php");
parser_no_timeout();

$source_root = "http://cosdna.com/eng/";
$source_search = $source_root . "stuff.php?q=";

$file_inci_json = "data/inci.json.txt";
$file_inci_to_cosdna_json = "data/inci-to-cosdna.json.txt";
$file_cosdna_json = "data/cosdna.json.txt";

util_create_dir("data");

//Load mapping saved
$mapping = util_json_read($file_inci_to_cosdna_json);

//Load inci data
$inci = util_json_read($file_inci_json);

function getIngredientPageFromSearch($ingredient) {
	global $source_search, $mapping;
	$ingredient = strtoupper($ingredient);
	if($mapping[$ingredient]) {
		return $mapping[$ingredient];
	}
	$page_html = util_download_url($source_search.urlencode($ingredient));
	$page_html = parser_remove_newline($page_html);

	//Detect if we got redirected automatically
	$page_link = parser_match("<a href=\"\\/cht\\/+([a-zA-Z0-9]*)\\.html\">", $page_html);

	$result = "";

	if(count($page_link)) {
		$result = $page_link[1];
	}
	else {
		$table_html = parser_match("StuffResult(.*?)<\\/div>", $page_html);
		$row_html = parser_match_all("<tr valign=\"top\">(.*?)<\\/tr>", $table_html[1]);
		$first_row_cells = parser_match_all("<td[^>]*>(.*?)<\\/td>", $row_html[1][0]);
		$result = parser_match("href=\"([^\"]*)\\.html\"", $first_row_cells[1][1]);
		$result = $result[1];
	}

	$mapping[$ingredient] = $result;
	return $result;
}

if(!$inci["ingredients"]) {
	exit();
}

foreach($inci["ingredients"] as $ingredient) {
	$cosdna_page = getIngredientPageFromSearch($ingredient["name"]);
	if(!$cosdna_page) {
		echo "Cosdna not found " . $ingredient["name"] . "<br>\n";
	}
}

util_json_write($file_inci_to_cosdna_json, $mapping);

$ingredients = util_json_read($file_cosdna_json);
if($ingredients["ingredients"]) {
	$ingredients = $ingredients["ingredients"];
}

$related = array();
$related_search = array();

function getCosdnaDataFromCosdnaId($cosdna_id) {
	global $source_root, $related, $related_search, $ingredients;
	$page_html = util_download("/cache/cosdna/".$cosdna_id.".html", $source_root.$cosdna_id.".html");
	$page_html = parser_remove_newline($page_html);
	$ingredient_html = parser_match("StuffDetail\"(.*?)Stuff_BottomLayout", $page_html);
	$ingredient_function = parser_match_all("<br>([^<]*?)<br>", $page_html);
	$ingredient_details = parser_match_all("<div class=\"(.*?)\">(.*?)<\\/div>", $ingredient_html[1]);
	$ingredient_data = array();
	for ($i=0; $i < count($ingredient_details[0]); $i++) {
		$ingredient_data[$ingredient_details[1][$i]] = $ingredient_details[2][$i];
	}
	$ingredient = array();
	$ingredient["cosdna_id"] = $cosdna_id;
	$ingredient["name"] = $ingredient_data["Stuff_DetailE"];
	if($ingredient_data["Stuff_DetailK"]) {
		$ingredient_names = parser_match("^\\((.*)\\)$", $ingredient_data["Stuff_DetailK"]);
		$ingredient["names"] = util_trim_array(explode(",", $ingredient_names[1]));
	}
	if(count($ingredient_function)) {
		$ingredient["function"] = $ingredient_function[1][0];
		if(count($ingredient_function[1]) > 1) {
			array_shift($ingredient_function[1]);
			$ingredient["cosdna_info"] = implode(",", $ingredient_function[1]);
		}
	}
	if($ingredient_data["Stuff_Infotag"]) {
		$ingredient["cosdna_infotag"] = str_replace(urldecode("%EF%BC%9A"), ":", $ingredient_data["Stuff_Infotag"]);
	}
	if($ingredient_data["Stuff_DetailR"]) {
		$ingredient_related = parser_match_all("<a href=\"([a-zA-Z0-9]*).html\">(.*?)<\\/a>", $ingredient_data["Stuff_DetailR"]);
		$ingredient["cosdna_related"] = array();

		for ($i=0; $i < count($ingredient_related[0]); $i++) {
			$ingredient_related_id = $ingredient_related[1][$i];
			$ingredient_related_name = $ingredient_related[2][$i];
			$ingredient["cosdna_related"][$ingredient_related_id] = $ingredient_related_name;
			if(!$ingredients[$ingredient_related_id] && !$related[$ingredient_related_id]) {
				$related_search[] = $ingredient_related_id;
			}
		}
	}

	return $ingredient;
}

foreach($mapping as $inci_name => $cosdna_id) {
	if(!$cosdna_id) {
		continue;
	}
	$ingredients[$cosdna_id] = getCosdnaDataFromCosdnaId($cosdna_id);
}


while(count($related_search) > 0) {
	$related_search_copy = array_unique($related_search);
	$related_search = array();
	foreach($related_search_copy as $cosdna_id) {
		if(!$cosdna_id) {
			continue;
		}
		$related[$cosdna_id] = getCosdnaDataFromCosdnaId($cosdna_id);
	}
}

//Convert to standard ingredient format
$ingredients_cosdna = $ingredients;
$ingredients = array();
foreach($ingredients_cosdna as $key => $value) {
	unset($value["cosdna_id"]);
	$ingredients[] = $value;
}

$result = array();
$result["ingredients"] = $ingredients;
$result["related"] = $related;

util_json_write($file_cosdna_json, $result);

?>