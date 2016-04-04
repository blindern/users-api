<?php namespace Blindern\UsersAPI\LDAP\Helpers;

use Blindern\UsersAPI\LDAP\Helpers\Ldap;
use Blindern\UsersAPI\LDAP\Helpers\CommonHelper;
use Blindern\UsersAPI\LDAP\LdapUser;
use Blindern\UsersAPI\LDAP\LdapGroup;

class DataHelper {
	/**
	 * LDAP-object
	 * @var \Blindern\UsersAPI\LDAP\Helpers\Ldap
	 */
	public $ldap;
	
	/**
	 * Constructor
	 */
	public function __construct(Ldap $ldap)
	{
		$this->ldap = $ldap;
	}

	/**
	 * Generate cache of all users and groups
	 */
	public function getFullData()
	{
		$users = $this->ldap->getUserHelper()->all();
		$groups = $this->ldap->getGroupHelper()->all();

		$group_members = array();
		$tmp_storage = array();
		foreach ($groups as $group)
		{
			$group_members[$group->name] = $this->membersExpand(array(), $group, $users, $groups, $tmp_storage);
		}

		// sort the groups members
		foreach ($group_members as $groupname => &$members)
		{
			$names = array();
			foreach (array_keys($members) as $username)
			{
				$dn = 'uid='.$username.','.$this->ldap->config['user_dn'];
				// users might be deleted while the group membership still exists
				// so check if this user actually exists, else just add the username to avoid sorting issues
				if (isset($users[$dn])) {
					$names[] = $users[$dn]->realname;
				} else {
					$names[] = $username;
				}
			}
			array_multisort($names, SORT_ASC, $members);
		}
		unset($members);
		
		// make reference to groups from users
		// the list is: user x have access to group y through membership in groups z
		$user_groups = array();
		foreach ($group_members as $group => $members)
		{
			foreach ($members as $user => $memberships)
			{
				$user_groups[$user][$group] = $memberships;
			}
		}

		// make reference to owned groups from users
		$user_owns = array();
		foreach ($groups as $group)
		{
			foreach ($group->getOwners() as $owner)
			{
				if (!preg_match("~^(.+?)=(.+?),(.+)$~", $owner, $matches))
					continue;

				if ($matches[3] == $this->ldap->config['user_dn'])
				{
					$user_owns[$matches[2]][$group->name][] = $group->name;
				}

				elseif ($matches[3] == $this->ldap->config['group_dn'])
				{
					if (isset($group_members[$matches[2]]))
					{
						$members = $group_members[$matches[2]];
						foreach (array_keys($members) as $member)
						{
							$user_owns[$member][$group->name][] = $matches[2];
						}
					}
				}
			}
		}

		// stringify
		$users_s = array();
		foreach ($users as $key => $user)
		{
			$users_s[$user->username] = $user->toArray();
		}
		$groups_s = array();
		foreach ($groups as $key => $group)
		{
			$groups_s[$group->name] = $group->toArray();
		}

		// create reference from email adresses to usernames
		$emails = array();
		foreach ($users as $user)
		{
			// there are no constraints to make email unique, multiple users might possibly have same email
			if (!empty($user->email))
			{
				$email = strtolower($user->email);
				if (!isset($emails[$email])) $emails[$email] = array();
				$emails[$email][] = $user->username;
			}
		}

		return array(
			'users' => $users_s,
			'groups' => $groups_s,
			'group_users' => $group_members,
			'user_groups' => $user_groups,
			'user_owns' => $user_owns,
			'emails' => $emails);
	}

	/**
	 * Expand the real members array to all references
	 * Keep a reference to where the user is originating from
	 *
	 * @var array List of previous parsed groups, to avoid recursion
	 *
	 * @return Array of members of the current group, referencing the groups that gave this association
	 */
	public function membersExpand($recursive_list = array(), $g, $users, $groups, &$tmp_storage)
	{
		$dn = $g->get_dn();

		// avoid recursion
		if (in_array($dn, $recursive_list)) return array();
		$recursive_list[] = $dn;

		// already parsed?
		if (isset($tmp_storage[$g->name]))
		{
			return $tmp_storage[$g->name];
		}

		$tmp_storage[$g->name] = array();
		foreach ($g->getMembers() as $row)
		{
			if (!preg_match("~^(.+?)=(.+?),(.+)$~", $row, $matches))
				continue;

			if ($matches[3] == $this->ldap->config['user_dn'])
			{
				$tmp_storage[$g->name][$matches[2]][] = $g->name;
			}

			elseif ($matches[3] == $this->ldap->config['group_dn'])
			{
				if (isset($groups[$row]))
				{
					$sub = $this->membersExpand($recursive_list, $groups[$row], $users, $groups, $tmp_storage);
					
					$keys = array_keys($sub);

					foreach ($keys as $user)
					{
						$tmp_storage[$g->name][$user][] = $groups[$row]->name;
					}
				}
			}
		}

		// sort the list


		return $tmp_storage[$g->name];
	}
}