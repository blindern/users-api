<?php namespace Blindern\UsersAPI\Controllers;

use HenriSt\OpenLdapAuth\Helpers\Ldap;
use HenriSt\OpenLdapAuth\LdapGroup;
use Blindern\UsersAPI\Response;

class Groups {
	public function index()
	{
		//
		// GET    /groups => get list of groups with details
		// filters: groupnames
		//

		return Ldap::forge()->getGroupHelper()->all();
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

		return Ldap::forge()->getGroupHelper()->find($groupname);
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