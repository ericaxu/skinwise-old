<?

require_once("parser.inc.php");

$file = $_GET["file"];

if($file) {
	echo util_cache_read($file);
}

?>