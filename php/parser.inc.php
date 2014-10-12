<?php

require_once("util.inc.php");

function parser_no_timeout() {
	ini_set('max_execution_time', 0);
}

function parser_remove($needle, $input) {
	return str_replace($needle, "", $input);
}

function parser_preg_remove($needle, $input) {
	return preg_replace("/".$needle."/s", "", $input);
}

function parser_remove_newline($input) {
	return parser_remove(array("\n","\r"), $input);
}

function parser_remove_nbsp($input) {
	return parser_remove("&nbsp;", $input);
}

function parser_match($regex, $input) {
	preg_match("/".$regex."/", $input, $result);
	return $result;
}

function parser_match_all($regex, $input) {
	preg_match_all("/".$regex."/", $input, $result);
	return $result;
}

function parser_preg_replace_dict($dict, $input) {
	foreach($dict as $key => $value) {
		$input = preg_replace("/".$key."/", $value, $input);
	}
	return $input;
}

function parser_str_replace_dict($dict, $input) {
	foreach($dict as $key => $value) {
		$input = str_ireplace($key, $value, $input);
	}
	return $input;
}

function parser_urlencode_url_pieces($url) {
	$url_pieces = explode("/", $url);
	array_walk($url_pieces, _urlencode);
	return implode("/", $url_pieces);
}

function _urlencode(&$string) {
	$string = rawurlencode($string);
}

function parser_echo_count($data, $title) {
	echo $title . ": " . count($data) . "<br>\n";
}

?>