<?php namespace Blindern\UsersAPI\Controllers;

use Blindern\UsersAPI\Response;
use Blindern\UsersAPI\User;
use Blindern\UsersAPI\UserHelper;

class Users {
	/**
	 * Returns a list of user objects
	 * GET: /users
	 *
	 * The fields returned is like those returned by a single user fetch,
	 * but the groups will not be loaded by default (see options)
	 *
	 * GET-option: usernames=<user1[,user2,..]>
	 * By default it will load all users, but this can be
	 * restricted by adding GET-variable usernames with comma-
	 * seperted usernames
	 *
	 * GET-option: grouplevel=<level>
	 * This option will include group information for the users
	 * Level indicate how much is returned:
	 * - 1: groups will be a list of group names
	 * - 2: groups will be a list of group objects (without its members)
	 * - 3: --"--                                  (with its members)
	 *
	 */
	public function index()
	{
		$uh = UserHelper::forge();

		if (isset($_GET['usernames']))
		{
			$names = explode(",", $_GET['usernames']);
			$users = $uh->getByNames($names);
		}

		else
		{
			$users = $uh->all();
		}
		
		// we need to load the groups for the users
		$level = isset($_GET['grouplevel']) ? (int) $_GET['grouplevel'] : 0;
		if ($level < 0 || $level > 3) $level = 0;
		
		$list = $uh->generateArray($users, $level, $level);

		return $list;
	}

	/**
	 * Create a new user object
	 * POST: /users
	 *
	 * Data required:
	 * - username (must be lowercase)
	 * - realname
	 * - email
	 *
	 * Optional data:
	 * - phone
	 * - password (in clear text, as we need it to hash it correctly)
	 *
	 * Groups cannot be added here, see appropriate requests for that
	 *
	 * The user object will be returned upon success,
	 * on validation-error it will return a array
	 * of fields that missed validation, and on other
	 * error it will return null (status code?)
	 */
	public function create()
	{
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
		return $user;
	}

	/**
	 * Get a specific user by its username
	 * GET: /user/<username>
	 *
	 * Will return the user object including full
	 * structure of groups
	 *
	 * Fields returned:
	 * - unique_id
	 * - username
	 * - realname (might be null)
	 * - email (might be null)
	 * - phone (might be null)
	 * - groups (array of group structure, see group request, without its members)
	 * 
	 * Password (hashed value) is not returned
	 *
	 * @return The user object on success (user found),
	 *         else null if user not found
	 */
	public function show($username)
	{
		// GET    /user/<username> => get user details

		// TODO: 404

		$uh = UserHelper::forge();
		$user = $uh->find($username);

		return $user->toArray(array(), 2, 2);
	}

	/**
	 * Update a specific user
	 * POST: /user/<username>
	 *
	 * Returns the user object on success,
	 * else it will return a array on fields
	 * that failed validation, or null on unknown error
	 *
	 * Fields that can be updated:
	 * - password (clear text, we will hash it)
	 * - realname
	 * - email
	 * - phone
	 *
	 * Per now unique_id/usernames cannot be updated here,
	 * and must be done manually
	 *
	 * Groups cannot be updated here, see appropriate requests for that
	 */
	public function update($username)
	{
		if (!($user = LdapUser::find($username)))
		{
			return Response::forge(Response::NOT_FOUND, 'Could not find user.');
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

	/**
	 * Delete a specific user
	 * DELETE: /user/<username>
	 *
	 * Deletes the user. Returns the old user object
	 * on success, else returns null
	 */
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