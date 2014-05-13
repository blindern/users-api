<?php namespace Blindern\UsersAPI;

use \Blindern\UsersAPI\DataProvider;

class User extends UserGroupCommon {
	/**
	 * Array-representation
	 *
	 * @param array field to ignore
	 * @param int memberdepth (0 = no group data, 1 = groups, 2 = groups with members expanded, 3 = with user data)
	 * @param int ownerdepth (as above)
	 */
	public function toArray(array $except = array(), $memberdepth = 1, $ownerdepth = 1)
	{
		$d = $this->attributes;
		foreach ($except as $e)
			unset($d[$e]);

		$provider = DataProvider::forge();
		$data = $provider->getData();
		$gh = GroupHelper::forge();
			
		// show list of groups
		if ($memberdepth >= 1)
		{
			$d['groups_relation'] = array();
			if (isset($data->user_groups[$this->username]))
			{
				$d['groups_relation'] = $data->user_groups[$this->username];

				// show with group data
				if ($memberdepth >= 2)
				{
					$d['groups'] = array();
					foreach ($data->user_groups[$this->username] as $groupname => $dummy)
					{
						$group = $gh->find($groupname);
						$depth = $memberdepth >= 3 ? 2 : 0;
						$d['groups'][] = $group->toArray(array(), $depth, $depth);
					}
				}
			}
		}

		// only show list of groups
		if ($ownerdepth >= 1)
		{
			$d['groupsowner_relation'] = array();
			if (isset($data->user_owns[$this->username]))
			{
				$d['groupsowner_relation'] = $data->user_owns[$this->username];
			}
		}

		return $d;
	}
}