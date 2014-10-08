<?

require_once("parser.inc.php");
parser_no_timeout();

$source_cir_data = "CIR.htm";
$file_cir_json = "data/cir.json.txt";

util_create_dir("data");

$index_html = util_file_read($source_cir_data);
$index_html = parser_remove_newline($index_html);

$index_table = parser_match("U\\.S\\. INCI Name\\:(.*)Return to CIR web site", $index_html);
$cir_data = parser_match_all("id=([0-9]*)', 'Link', 'scrollbars=yes,status=no,resizable=yes,toolbar=yes,location=yes'\\)\">(.*?)<\\/a>", $index_table[1]);

$ingredients = array();

for ($i = 0; $i < count($cir_data[0]); $i++) {
	$ingredient = array();
	$ingredient["cir_id"] = intval($cir_data[1][$i]);
	$ingredient["name"] = trim($cir_data[2][$i]);
	$ingredients[] = $ingredient;
}

$result = array();
$result["ingredients"] = $ingredients;

util_json_write($file_cir_json, $result);

?>