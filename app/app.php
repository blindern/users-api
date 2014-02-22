<?php

require "../vendor/autoload.php";
$config = require "config.php";

// check access
// TODO: access control by credentials/certs?
$allowed = array(
	'83.143.87.202'
);
if (!in_array($_SERVER['REMOTE_ADDR'], $allowed))
{
	die("No access.");
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
