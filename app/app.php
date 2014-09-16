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
	die("HMAC-authorization failed.");

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
