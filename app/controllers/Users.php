<?php namespace Blindern\UsersAPI\Controllers;

class Users {
	public function index()
	{
		//
		// GET    /groups => get list of groups with details
		// filters: groupnames
		//
	}

	public function create()
	{
		//
		// POST   /groups => add new group
		//
	}

	public function show($groupname)
	{
		// GET    /group/<groupname> => get group details
	}

	public function update($groupname)
	{
		// POST   /group/<groupname> => update group info
	}

	public function delete($groupname)
	{
		// DELETE /group/<groupname> => delete group
	}
}