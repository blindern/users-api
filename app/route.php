<?php

use \Blindern\UsersAPI\Controllers\Users;
use \Blindern\UsersAPI\Controllers\UsersGroups;
use \Blindern\UsersAPI\Controllers\Groups;

// get route
$route = substr($_SERVER['REQUEST_URI'], strlen($config['relative_path']));
if (($p = strpos($route, "?")) !== false)
{
	$route = substr($route, 0, $p);
}
$routes = explode("/", ltrim($route, "/"));
$method = $_SERVER['REQUEST_METHOD'];

// for simplicity we keep the routes-handling here for now

// users
if ($route == 'users')
{
	$c = new Users();
	if ($method == 'GET')
	{
		return $c->index();
	}

	elseif ($method == 'POST')
	{
		return $c->create();
	}
}

// specific user
elseif ($routes[0] == 'user' && isset($routes[1]))
{
	if (!isset($routes[2]))
	{
		$c = new Users();
		if ($method == 'GET')
		{
			return $c->show($routes[1]);
		}

		elseif ($method == 'POST')
		{
			return $c->update($routes[1]);
		}

		elseif ($method == 'DELETE')
		{
			return $c->delete($routes[1]);
		}
	}

	elseif ($routes[2] == 'groups' && isset($routes[3]) && !isset($routes[4]))
	{
		$c = new UsersGroups();
		if ($method == 'POST')
		{
			return $c->create($routes[1], $routes[3]);
		}

		elseif ($method == 'DELETE')
		{
			return $c->delete($routes[1], $routes[3]);
		}
	}
}

// groups
elseif ($route == 'groups')
{
	$c = new Groups();
	if ($method == 'GET')
	{
		return $c->index();
	}

	elseif ($method == 'POST')
	{
		return $c->create();
	}
}

// specific group
elseif ($routes[0] == 'group' && isset($routes[1]) && !isset($routes[2]))
{
	$c = new Groups();
	if ($method == 'GET')
	{
		return $c->show($routes[1]);
	}

	elseif ($method == 'POST')
	{
		return $c->update($routes[1]);
	}

	elseif ($method == 'DELETE')
	{
		return $c->delete($routes[1]);
	}
}

// verify credentials
elseif ($route == 'auth' && $method == 'POST')
{
	$c = new Auth();
	return $c->hashed();
}
elseif ($route == 'simpleauth' && $method == 'POST' && $_SERVER["HTTPS"] == 'on')
{
	$c = new Auth();
	return $c->simple();
}