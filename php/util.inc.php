<?php

//JSON

function util_json_encode($data) {
	array_walk_recursive($data, util_utf8_encode);
	$data = util_json_format(json_encode($data));
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

function util_json_format($json) {
  if (!is_string($json)) {
    if (phpversion() && phpversion() >= 5.4) {
      return json_encode($json, JSON_PRETTY_PRINT);
    }
    $json = json_encode($json);
  }
  $result      = '';
  $pos         = 0;               // indentation level
  $strLen      = strlen($json);
  $indentStr   = "\t";
  $newLine     = "\n";
  $prevChar    = '';
  $outOfQuotes = true;

  for ($i = 0; $i < $strLen; $i++) {
    // Speedup: copy blocks of input which don't matter re string detection and formatting.
    $copyLen = strcspn($json, $outOfQuotes ? " \t\r\n\",:[{}]" : "\\\"", $i);
    if ($copyLen >= 1) {
      $copyStr = substr($json, $i, $copyLen);
      // Also reset the tracker for escapes: we won't be hitting any right now
      // and the next round is the first time an 'escape' character can be seen again at the input.
      $prevChar = '';
      $result .= $copyStr;
      $i += $copyLen - 1;      // correct for the for(;;) loop
      continue;
    }
    
    // Grab the next character in the string
    $char = substr($json, $i, 1);
    
    // Are we inside a quoted string encountering an escape sequence?
    if (!$outOfQuotes && $prevChar === '\\') {
      // Add the escaped character to the result string and ignore it for the string enter/exit detection:
      $result .= $char;
      $prevChar = '';
      continue;
    }
    // Are we entering/exiting a quoted string?
    if ($char === '"' && $prevChar !== '\\') {
      $outOfQuotes = !$outOfQuotes;
    }
    // If this character is the end of an element,
    // output a new line and indent the next line
    else if ($outOfQuotes && ($char === '}' || $char === ']')) {
      $result .= $newLine;
      $pos--;
      for ($j = 0; $j < $pos; $j++) {
        $result .= $indentStr;
      }
    }
    // eat all non-essential whitespace in the input as we do our own here and it would only mess up our process
    else if ($outOfQuotes && false !== strpos(" \t\r\n", $char)) {
      continue;
    }

    // Add the character to the result string
    $result .= $char;
    // always add a space after a field colon:
    if ($outOfQuotes && $char === ':') {
      $result .= ' ';
    }

    // If the last character was the beginning of an element,
    // output a new line and indent the next line
    else if ($outOfQuotes && ($char === ',' || $char === '{' || $char === '[')) {
      $result .= $newLine;
      if ($char === '{' || $char === '[') {
        $pos++;
      }
      for ($j = 0; $j < $pos; $j++) {
        $result .= $indentStr;
      }
    }
    $prevChar = $char;
  }

  return $result;
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