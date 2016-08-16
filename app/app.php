<?php

use Blindern\UsersAPI\Response;

require "../vendor/autoload.php";
$config = require "config.php";

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

// HMAC-check
$hmac = new \Blindern\UsersAPI\HMAC();
if (!$hmac->verify_request())
{
	// allow GET from localhost
	$is_local = $_SERVER['REQUEST_METHOD'] != 'GET' || $_SERVER['REMOTE_ADDR'] == '127.0.0.1';
	$is_local = $is_local && !isset($_SERVER['HTTP_X_FORWARDED_FOR']); // reject forwarded requests

	if (!$is_local) {
		header("HTTP/1.0 403 Forbidden");
		die("HMAC-authorization failed.");
	}
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
