<?php

//JSON

function util_json_encode($data) {
	array_walk_recursive($data, util_utf8_encode);
	$data = json_encode($data);
	return $data;
}

function util_json_decode($data) {
	$data = json_decode($data, true);
	if(!$data) {
		return array();
	}
	array_walk_recursive($data, util_utf8_decode);
	return $data;
}

//UTF-8

function util_utf8_encode(&$value) {
	$value = utf8_encode($value);
}

function util_utf8_decode(&$value) {
	$value = utf8_decode($value);
}

//Download & Cache

function util_download($cachefile, $url, $user_func = false) {
	$cachefile = getcwd() . $cachefile;
	$data = util_cache_read($cachefile);
	if(!$data) {
		$data = util_download_url($url);
		if($user_func) {
			$data = call_user_func($user_func, $data);
		}
		util_cache_write($cachefile, $data);
	}
	return $data;
}

function util_download_url($url) {
	return file_get_contents($url);
	/*
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 5);
	$data = curl_exec($ch);
	curl_close($ch);
	return $data;
	*/
}

function util_cache_read($filename) {
	if(file_exists($filename)) {
		return gzuncompress(file_get_contents($filename));
	}
	return false;
}

function util_cache_write($filename, $data) {
	if(!$data) return;
	util_create_dir($filename);
	file_put_contents($filename, gzcompress($data));
}

//Directory and file

function util_create_dir($filename) {
	$dir = dirname($filename);
	if (!file_exists($dir)) {
		mkdir($dir, 0744, true);
	}
}

function util_json_read($filename) {
	$json = util_file_read($filename);
	return util_json_decode($json);
}

function util_json_write($filename, $data) {
	$json = util_json_encode($data);
	util_file_write($filename, $json);
}

function util_file_read($filename) {
	return file_get_contents($filename);
}

function util_file_write($filename, $data) {
	file_put_contents($filename, $data);
}

function rrmdir($dir) {
	foreach(glob($dir . '/*') as $file) {
		if(is_dir($file))
			rrmdir($file);
		else
			unlink($file);
	}
	rmdir($dir);
}

//Arrays

function util_ensure_array($array) {
	if(!$array) {
		$array = array();
	}
	return $array;
}

function util_rotate_array($array, $keyrow, $valrow) {
	$result = array();
	for ($i = 0; $i < count($array[$keyrow]); $i++) { 
		$result[$array[$keyrow][$i]] = $array[$valrow][$i];
	}
	return $result;
}

function util_array_merge_assoc() {
	$array = array();
	foreach (func_get_args() as $param) {
		if(!is_array($param)) {
			continue;
		}
		foreach($param as $key => $value) {
			$array[$key] = $value;
		}
	}
	return $array;
}

function util_array_merge_assoc_recursive_array($array) {
	$result = array();
	foreach ($array as $param) {
		$result = util_array_merge_assoc_recursive($result, $param);
	}
	return $result;
}

function util_array_merge_assoc_recursive() {
	$array = array();
	foreach (func_get_args() as $param) {
		if(!is_array($param)) {
			continue;
		}
		foreach($param as $key => $value) {
			if($array[$key] && $value) {
				if(is_array($value) && is_array($array[$key]) && !util_array_is_assoc($value) && !util_array_is_assoc($array[$key])) {
				$array[$key] = array_merge($array[$key], $value);
				}
				$array[$key] = util_array_merge_assoc_recursive($array[$key], $value);
			}
			else{
				$array[$key] = $value;
			}
		}
	}
	return $array;
}

function util_knatsort(&$array) {
	uksort($array, "strnatcmp");
}

function util_sort_unique_string($array) {
	$array = array_unique($array);
	sort($array, SORT_STRING);
	return $array;
}

function util_trim_array($array) {
	foreach($array as $key => $value) {
		$array[$key] = trim($value);
	}
	return $array;
}

function util_array_is_assoc($array) {
  return (bool)count(array_filter(array_keys($array), 'is_string'));
}

//Strings

function util_starts_with($haystack, $needle) {
	return !strncmp($haystack, $needle, strlen($needle));
}

function util_ends_with($haystack, $needle) {
	$length = strlen($needle);
	if ($length == 0) {
		return true;
	}

	return (substr($haystack, -$length) === $needle);
}

function strallpos($haystack, $needle, $offset = 0){ 
	$result = array();
	for($i = $offset; $i < strlen($haystack); $i++){ 
		$pos = strpos($haystack , $needle , $i); 
		if($pos !== FALSE){ 
			$offset =  $pos; 
			if($offset >= $i){ 
				$i = $offset; 
				$result[] = $offset; 
			} 
		} 
	} 
	return $result; 
}

//HTML

function html_decode($input) {
	return html_entity_decode($input, ENT_QUOTES);
}

?>