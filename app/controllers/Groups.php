<?php namespace Blindern\UsersAPI\Controllers;

use Blindern\UsersAPI\Group;
use Blindern\UsersAPI\Response;
use Blindern\UsersAPI\GroupHelper;

class Groups {
	/**
	 * Returns a list of group objects
	 * GET: /groups
	 *
	 * For fields returned, see the fetching of a specific group
	 *
	 * GET-option: groupnames=<name1[,name2,..]>
	 * By default it will load and return all groups,
	 * but this can be filtered by groupnames by having a
	 * comma-seperated list in GET-variable 'groupnames'
	 *
	 * GET-option: getmembers
	 * By default the return data will contain a array of all members,
	 * and by using this option the list will be expanded as is returned
	 * when fetching a single group
	 *
	 * @return an array of user objects
	 */
	public function index()
	{
		$h = GroupHelper::forge();

		if (isset($_GET['groupnames']))
		{
			$names = explode(",", $_GET['groupnames']);
			$groups = $h->getByNames($names);
		}

		else
		{
			$groups = $h->all();
		}

		$depth = isset($_GET['getmembers']) ? 4 : 0;
		$groups = $h->generateArray($groups, $depth, $depth);

		return Response::forge(Response::SUCCESS, null, $groups);
	}

	/**
	 * Creates a new group
	 * POST: /groups
	 *
	 * Data required:
	 * - groupname
	 *
	 * Optional data:
	 * - description
	 *
	 * To be added later?
	 * - id (must not be in use)
	 *
	 * Members cannot be added here, see appropriate requests for that
	 *
	 * @return The new group object will be returned on success,
	 *         and null on unknown error
	 */
	public function create()
	{
		// TODO: create group
		throw new \Exception("Not implemented");
	}

	/**
	 * Get a specific group
	 * GET: /group
	 *
	 * Will return the group object including full structure of
	 * the members
	 *
	 * Fields returned:
	 * - unique_id
	 * - id
	 * - name
	 * - members (array of user structure, see user request, excluding groups)
	 */
	public function show($groupname)
	{
		$gh = GroupHelper::forge();

		$group = $gh->find($groupname);
		if (is_null($group)) {
			return Response::forge(Response::NOT_FOUND, 'Could not find group.');
		}

		return $group->toArray(array(), 3, 3);
	}

	/**
	 * Update a specific group
	 * POST: /group/<groupname>
	 *
	 * Returns the group object on success,
	 * else it will return a array on fields
	 * that failed validation, or null on unknown error
	 *
	 * Fields that can be updated:
	 * - name
	 * - description
	 *
	 * Per now unique_id/usernames cannot be updated here,
	 * and must be done manually
	 *
	 * Members cannot be updated here, see appropriate requests for that
	 */
	public function update($groupname)
	{
		// TODO: update group
		throw new \Exception("Not implemented");
	}

	/**
	 * Delete a specific group
	 * DELETE: /group/<groupname>
	 *
	 * Deletes the group. Returns the old group object
	 * on success, else returns null
	 */
	public function delete($groupname)
	{
		// TODO: delete group
		throw new \Exception("Not implemented");
	}
}