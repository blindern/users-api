<?php

use Blindern\UsersAPI\Response;

require "../vendor/autoload.php";
$config = require "config.php";

// check access
// TODO: access control by credentials/certs?
$allowed = array(
	'83.143.87.202',
	'83.143.83.35',
	'37.191.201.59',
	'37.191.203.140'
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

header('Content-Type: application/json');

if (is_array($data))
{
	$r = Response::forge(Response::SUCCESS, '', $data);
	echo $r;
}

else
{
	echo $data;
}
