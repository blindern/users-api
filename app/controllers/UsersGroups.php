<?php namespace Blindern\UsersAPI\Controllers;

/**
 * This class handles adding or removing groups from users
 */
class UsersGroups {
	public function create($user, $group)
	{
		// POST   /user/<username>/groups/<groupname> => add user to group
	}

	public function delete($user, $group)
	{
		// DELETE /user/<username>/groups/<groupname> => remove user from group
	}
}