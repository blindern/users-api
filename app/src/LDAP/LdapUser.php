<?php namespace Blindern\UsersAPI\LDAP;

use Blindern\UsersAPI\LDAP\Helpers\Ldap;
use Blindern\UsersAPI\LDAP\Helpers\UserHelper;

class LdapUser {
	/**
	 * LDAP-object
	 * @var \Blindern\UsersAPI\LDAP\Helpers\Ldap
	 */
	protected $ldap;

	/**
	 * All of the user's attributes.
	 *
	 * @var array
	 */
	protected $attributes;

	/**
	 * Attributes updated but not stored
	 *
	 * @var array
	 */
	protected $attributes_updated = array();

	/**
	 * New user object not stored?
	 *
	 * @var bool
	 */
	protected $is_new = false;

	/**
	 * Group list
	 *
	 * @var array
	 */
	protected $groups;

	/**
	 * Helper
	 *
	 * @var UserHelper
	 */
	protected $helper;

	/**
	 * Create a new generic User object.
	 *
	 * @param  array  $attributes
	 * @return void
	 */
	public function __construct(array $attributes, Ldap $ldap)
	{
		$this->attributes = $attributes;
		$this->ldap = $ldap;
		$this->helper = $this->ldap->getUserHelper();
		$this->is_new = empty($attributes);
	}

	/**
	 * Get the unique identifier for the user.
	 *
	 * @return mixed
	 */
	public function getAuthIdentifier()
	{
		return $this->attributes['username'];
	}

	/**
	 * Get the password for the user.
	 *
	 * @return string
	 */
	public function getAuthPassword()
	{
		// Not available
		return null;
		//return $this->attributes['password'];
	}

	/**
	 * Initialize groups array
	 */
	public function initGroups()
	{
		$this->groups = array();
	}

	/**
	 * Assign group to user internally
	 */
	public function appendGroup($group)
	{
		$this->groups[] = $group;
	}

	/**
	 * Store changes to server, including new users
	 */
	public function store()
	{
		$this->ldap->bindPrivileged();

		// don't have username?
		if (!isset($this->username))
		{
			throw new Exception("Can't store user without username.");
		}

		// new user?
		if ($this->is_new)
		{
			if ($this->helper->find($this->username))
			{
				throw new Exception("User already exists.");
			}

			$skel = array(
				'objectClass' => array(
					'account',
					'posixAccount'
				),
				'cn' => $this->username,
				'uid' => $this->username,
				'uidNumber' => $this->helper->getNextID(),
				//'gidNumber' => $this->ldap->config['default_gid'],
				'homeDirectory' => '/home/'.$this->username,
				'loginShell' => '/usr/sbin/nologin',
				'gecos' => $this->username,
				'description' => 'User account'
			);

			// create this object
			ldap_add($this->ldap->get_connection(), $this->get_dn(), $skel);
			$this->is_new = false;
		}

		if ($this->attributes_updated)
		{
			$new = array();
			foreach ($this->attributes_updated as $field)
			{
				if (!isset($new[$field]))
				{
					$new[$field] = $this->attributes[$field];
				}
			}

			ldap_mod_replace($this->ldap->get_connection(), $this->get_dn(), $new);
			$this->attributes_updated = array();
		}
	}

	/**
	 * Get DN for the object
	 *
	 * @return string
	 */
	public function get_dn()
	{
		if (isset($this->dn)) return $this->dn;
		return $this->ldap->get_bind_dn($this->username);
	}

	/**
	 * Dynamically access the user's attributes.
	 *
	 * @param  string  $key
	 * @return mixed
	 */
	public function __get($key)
	{
		return $this->attributes[$key];
	}

	/**
	 * Dynamically set an attribute on the user.
	 *
	 * @param  string  $key
	 * @param  mixed   $value
	 * @return void
	 */
	public function __set($key, $value)
	{
		$this->attributes[$key] = $value;
		$this->attributes_updated[] = $key;
	}

	/**
	 * Dynamically check if a value is set on the user.
	 *
	 * @param  string  $key
	 * @return bool
	 */
	public function __isset($key)
	{
		return isset($this->attributes[$key]);
	}

	/**
	 * Dynamically unset a value on the user.
	 *
	 * @param  string  $key
	 * @return bool
	 */
	public function __unset($key)
	{
		unset($this->attributes[$key]);
	}

	/**
	 * Convert to array
	 *
	 * @param array Fields to ignore
	 * @return array
	 */
	public function toArray(array $except = array())
	{
		$d = $this->attributes;
		unset($d['dn']);
		foreach ($except as $e)
			unset($d[$e]);

		return $d;
	}

	/**
	 * Check for same user
	 *
	 * @param LdapUser
	 * @return bool
	 */
	public function isSame(LdapUser $user)
	{
		return $this->username == $user->username;
	}
}
