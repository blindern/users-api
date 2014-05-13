<?php namespace Blindern\UsersAPI;

use \Blindern\UsersAPI\LDAP\Helpers\Ldap;

class DataProvider {
	/**
	 * The main dataprovider-object
	 *
	 * @var DataProvider
	 */
	protected static $provider;

	/**
	 * Get main dataprovider-object
	 */
	public static function forge()
	{
		if (is_null(static::$provider))
			static::$provider = new static();

		return static::$provider;
	}

	/**
	 * LDAP-object
	 * @var \Blindern\UsersAPI\LDAP\Helpers\Ldap
	 */
	public $ldap;

	/**
	 * Cached data
	 * @var DataSet
	 */
	protected $cache;

	/**
	 * Constructor
	 */
	public function __construct()
	{
		$config = app()->config['auth']['ldap'];
		$this->ldap = new Ldap($config);
	}

	/**
	 * Get data from cache, or genereate
	 */
	protected function loadData()
	{
		$f = app()->config['auth']['cache_file'];
		if (is_file($f))
		{
			$old = unserialize(file_get_contents($f));
			if ($old['time']+app()->config['auth']['cache_timeout'] > time())
			{
				$this->cache = $old['data'];
				return;
			}
		}

		$data = $this->ldap->getDataHelper()->getFullData();
		$set = new DataSet();
		$set->users = $data['users'];
		$set->groups = $data['groups'];
		$set->group_users = $data['group_users'];
		$set->user_groups = $data['user_groups'];
		$set->user_owns = $data['user_owns'];
		$this->cache = $set;

		file_put_contents($f, serialize(array("time" => time(), "data" => $this->cache)));
	}

	/**
	 * Get data
	 */
	public function getData()
	{
		if (is_null($this->cache))
			$this->loadData();
		return $this->cache;
	}
}