<?php namespace Blindern\UsersAPI\LDAP\Helpers;

use Blindern\UsersAPI\LDAP\Helpers\Ldap;
use Blindern\UsersAPI\LDAP\Helpers\CommonHelper;
use Blindern\UsersAPI\LDAP\LdapUser;

class UserHelper extends CommonHelper {
	protected $type = 'user';
	protected $objectClass = 'posixAccount';

	/**
	 * Get info about users (all by default)
	 * Sorts the list by realnames
	 *
	 * @param string $search_by LDAP-string for searching, eg. (uid=*), defaults to all users
	 * @return array of LdapUser
	 */
	public function getByFilter($search_by = null)
	{
		// handle search by
		$search_by = empty($search_by) ? '(uid=*)' : $search_by;

		// handle fields
		$user_fields = $this->ldap->config['user_fields'];
		$fields = array_values($user_fields);
		/*if (!empty($extra_fields))
		{
			if (!is_array($extra_fields))
			{
				throw new \Exception("Extra fields is not an array.");
			}

			$fields = array_merge($fields, $extra_fields);
			foreach ($extra_fields as $field)
			{
				$user_fields[$field] = $field;
			}
		}*/

		// retrieve info from LDAP
		$r = ldap_search($this->ldap->get_connection(), $this->ldap->config['user_dn'], $search_by, $fields);
		$e = ldap_get_entries($this->ldap->get_connection(), $r);

		// ldap_get_entries makes attributes lowercase
		foreach ($user_fields as &$field)
		{
			$field = strtolower($field);
		}

		$users = array();
		$users_names = array();
		for ($i = 0; $i < $e['count']; $i++)
		{
			// map fields
			$row = array();
			foreach ($user_fields as $map => $to)
			{
				$to = strtolower($to); // ldap_get_entries makes attributes lowercase
				if (isset($e[$i][$to]))
				{
					// NOTE: Only allowes for one value, there may be many
					$row[$map] = $e[$i][$to][0];
				}
				else
				{
					$row[$map] = null;
				}
			}

			$row['dn'] = $e[$i]['dn'];
			$users_names[] = strtolower($row['realname']);

			$user = new LdapUser($row, $this->ldap);
			$users[$user->get_dn()] = $user;
		}

		// sort by realname
		array_multisort($users_names, $users);

		return $users;
	}
}