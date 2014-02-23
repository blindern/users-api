<?php namespace Blindern\UsersAPI\Controllers;

use Blindern\UsersAPI\Controllers\Users;

class Auth {
	/**
	 * Authenticate by using username and plain password
	 *
	 * POST-data: username, password
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

		return Respose::forge(Response::SUCCESS, '', null);
	}

	public function hashed()
	{
		// TODO: how can we implement this?
		// (maybe use a salt given from us?)
	}
}