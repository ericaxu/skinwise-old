<?

require_once("parser.inc.php");

$file = $_GET["file"];

if($file) {
	$file = getcwd() . "/cache/" . $file;
	echo util_cache_read($file);
}

?>