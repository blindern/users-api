<?php namespace Blindern\UsersAPI\Controllers;

use HenriSt\OpenLdapAuth\Helpers\Ldap;
use HenriSt\OpenLdapAuth\LdapUsers;
use Blindern\UsersAPI\Response;

class Users {
	public function index()
	{
		//
		// GET    /users => get list of users with details
		// filters: usernames
		//

		return Ldap::forge()->getUserHelper()->all(true);
	}

	public function create()
	{
		//
		// POST   /users => add new user
		//

		// data we require:
		// username
		// realname
		// email

		$fields = array('username', 'realname', 'email', 'phone', 'password');
		$require = array('username', 'realname', 'email');

		foreach ($require as $f)
		{
			if (!isset($_POST[$f]))
			{
				return Response::forge(Response::INVALID_REQUEST, 'Required fields missing.');
			}
		}

		// set up LDAP
		$config = app()->config['auth']['ldap'];
		$ldap = new Ldap($config);

		$user = new LdapUser(array(), $ldap);
		foreach ($fields as $f)
		{
			if (isset($_POST[$f]))
			{
				if ($f == 'password')
				{
					// TODO $user->setPassword($_POST[$f]);
				}

				else
				{
					$user->$f = $_POST[$f];
				}
			}
		}

		$user->store();
		return Response::forge(Response::SUCCESS, '', $user);
	}

	public function show($username)
	{
		// GET    /user/<username> => get user details

		$user = Ldap::forge()->getUserHelper()->find($username);
		return Response::forge(Response::SUCCESS, null, $user);
	}

	public function update($username)
	{
		// POST   /user/<username> => update user info
		// username must be manually updated

		if (!($user = LdapUser::find($username)))
		{
			return Response::forge(Response::USER_NOT_FOUND, 'Could not find user.');
		}

		// fields that can be updated
		$fields = array('realname', 'email', 'phone', 'password');
		$update = array();

		foreach ($fields as $f)
		{
			if (isset($_POST[$f]))
			{
				$update[$f] = $_POST[$f];
			}
		}

		// validate data
		$helper = new UserHelper();
		$validate = $helper->validate($update);
		if ($validate !== true)
		{
			return Response::forge(Response::INVALID_DATA, 'Data did not validate.', $validate);
		}

		// update data
		foreach ($update as $field => $data)
		{
			$user->$field = $data;
		}

		$user->store();
		return Response::forge(Response::SUCCESS, '', $user);
	}

	public function delete($username)
	{
		// DELETE /user/<username> => delete user
		if (!($user = LdapUser::find($username)))
		{
			return Response::forge(Response::USER_NOT_FOUND, 'Could not find user.');
		}

		if ($user->delete())
		{
			return Response::forge(Response::SUCCESS, '', $user);
		}

		return Response::forge(Response::ERROR, 'Unknown error.');
	}
}