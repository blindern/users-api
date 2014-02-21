<?php

use \Blindern\UsersAPI\Controllers\Users;
use \Blindern\UsersAPI\Controllers\Groups;

require "../vendor/autoload.php";
$config = require "config.php";

// get route
$route = substr($_SERVER['REQUEST_URI'], strlen($config['relative_path']));
if (($p = strpos($route, "?")) !== false)
{
	$route = substr($route, 0, $p);
}
$routes = explode("/", ltrim($route, "/"));
$method = $_SERVER['REQUEST_METHOD'];

// check access
// TODO: access control by credentials/certs?
$allowed = array(
	'83.143.87.202'
);
if (!in_array($_SERVER['REMOTE_ADDR'], $allowed))
{
	die("No access.");
}

// for simplicity we keep the routes-handling here for now

// users
if ($route == 'users')
{
	$c = new Users();
	if ($method == 'GET')
	{
		return $c->get();
	}

	elseif ($method == 'POST')
	{
		return $c->post();
	}
}






// TO IMPLEMENT:
/*

X GET    /user/<username> => get user details
X POST   /user/<username> => update user info
X DELETE /user/<username> => delete user

x POST   /user/<username>/groups/<groupname> => add user to group
X DELETE /user/<username>/groups/<groupname> => remove user from group

GET    /groups => get list of groups with details
       filters: groupnames
POST   /groups => create new group
GET    /group/<groupname> => get group details
DELETE /group/<groupname> => delete group

POST   /auth => attempt actual authentication
       (check how we can hash this password so we don't get cleartext,
	    and maybe use a salt given from us?)
 */


die("Not implemented.");
