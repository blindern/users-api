<?php

require "../vendor/autoload.php";
$config = require "config.php";

// check access
// TODO: access control by credentials/certs?
$allowed = array(
	'83.143.87.202',
	'37.191.201.59'
);
if (!in_array($_SERVER['REMOTE_ADDR'], $allowed))
{
	die("No access.");
}

class appobj {
	public static function get()
	{
		if (!static::$g)
		{
			static::$g = new static();
			static::$g->config = require "config.php";
		}
		return static::$g;
	}
	protected static $g;
	public $config;
}

function app()
{
	return appobj::get();
}

$data = require "route.php";
if ($data === null)
{
	die("Not implemented.");
}

if (is_array($data))
{
	echo json_encode($data);
}

else
{
	echo $data;
}
