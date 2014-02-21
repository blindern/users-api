<?php

require "../vendor/autoload.php";
$config = require "config.php";

// get route
$route = substr($_SERVER['REQUEST_URI'], strlen($config['relative_path']));
if (($p = strpos($route, "?")) !== false)
{
	$route = substr($route, 0, $p);
}
$routes = explode("/", ltrim($route, "/"));

// check access
// TODO: access control (by IP or credentials)


// TO IMPLEMENT:
/*

GET    /users => get list of users with details
       filters: usernames
POST   /users => add new user
GET    /user/<username> => get user details
POST   /user/<username> => update user info
DELETE /user/<username> => delete user

POST   /user/<username>/groups/<groupname> => add user to group
DELETE /user/<username>/groups/<groupname> => remove user from group

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
