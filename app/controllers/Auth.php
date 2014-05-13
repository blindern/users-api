<?php namespace Blindern\UsersAPI\Controllers;

use Blindern\UsersAPI\LDAP\Helpers\Ldap;
use Blindern\UsersAPI\Controllers\Users;
use Blindern\UsersAPI\Response;

class Auth {
	/**
	 * Authenticate by using username and plain password
	 * POST: /simpleauth
	 *
	 * Data required:
	 * - username
	 * - password (in clear text, we need it to auth to LDAP-database)
	 *
	 * @return  The user as authed if success,
	 *          INVALID_REQUEST if parameters missing (and data = null)
	 *          LOGIN_FAIL if auth failed (and data = null)
	 */
	public function simple()
	{
		if (!isset($_POST['username']) || !isset($_POST['password']))
		{
			return Response::forge(Response::INVALID_REQUEST, 'Missing parameters.');
		}

		// set up LDAP
		$config = app()->config['auth']['ldap'];
		$ldap = new Ldap($config);

		if ($ldap->bind($_POST['username'], $_POST['password']))
		{
			// get user object
			$c = new Users();
			return $c->show($_POST['username']);
		}

		return Response::forge(Response::LOGIN_FAIL, '', null);
	}

	/**
	 * TODO: Authenticate by hashed value, if possible?
	 *
	 * Maybe use a salt given from us for encryption, certificates or something else?
	 */
	public function hashed()
	{
		throw new \Exception("Not implemented");
	}
}