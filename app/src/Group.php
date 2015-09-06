<?php namespace Blindern\UsersAPI;

use \Blindern\UsersAPI\DataProvider;

class Group extends UserGroupCommon {
	/**
	 * Array-representation
	 *
	 * @param array field to ignore
	 * @param int memberdepth (0 = no user data, 1 = members, 2 = members expanded, 3 = with user data)
	 * @param int ownerdepth (as above)
	 */
	public function toArray(array $except = array(), $memberdepth = 1, $ownerdepth = 1)
	{
		$uh = UserHelper::forge();

		$d = $this->attributes;
		foreach ($except as $e)
			unset($d[$e]);

		if ($memberdepth < 1)
			unset($d['members']);
		elseif ($memberdepth > 1)
		{
			$provider = DataProvider::forge();
			$data = $provider->getData();

			if ($memberdepth >= 2)
			{
				$d['members_relation'] = $data->group_users[$this->name];
			}

			if ($memberdepth >= 3)
			{
				$d['members_data'] = array();
				foreach ($data->group_users[$this->name] as $user => $dummy)
				{
					$u = $uh->find($user);
					if ($u) {
						$d['members_data'][$user] = $u->toArray();
					}
				}
			}
		}

		if ($ownerdepth < 1)
			unset($d['owners']);
		elseif ($ownerdepth > 1)
		{
			/*$provider = DataProvider::forge();
			$data = $provider->getData();

			if ($ownerdepth >= 2)
			{
				$d['owners_relation'] = $data->group_users[$this->name];
			}

			if ($ownerdepth >= 3)
			{
				$d['owners_data'] = array();
				foreach ($data->group_users[$this->name] as $user => $dummy)
				{
					$u = $uh->find($user);
					$d['owners_data'][$user] = $u->toArray();
				}
			}*/
		}

		return $d;
	}
}
