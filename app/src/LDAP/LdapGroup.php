<?php namespace Blindern\UsersAPI\LDAP;

use Blindern\UsersAPI\LDAP\Helpers\Ldap;
use Blindern\UsersAPI\LDAP\Helpers\LdapException;
use Blindern\UsersAPI\LDAP\Helpers\GroupHelper;

class LdapGroup {
	/**
	 * LDAP-object
	 * @var \Blindern\UsersAPI\LDAP\Helpers\Ldap
	 */
	protected $ldap;

	/**
	 * All of the group's attributes.
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
	 * New group object not stored?
	 *
	 * @var bool
	 */
	protected $is_new = false;

	/**
	 * Members of the group (full DN, can be another group)
	 *
	 * @var array of DNs
	 */
	protected $members;

	/**
	 * Owners of the group (full DN, can be another group)
	 *
	 * When another group is references, it is the members of that group
	 * that counts, not the owner
	 *
	 * @var array of DNs
	 */
	protected $owners;

	/**
	 * Helper
	 *
	 * @var GroupHelper
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
		$this->helper = $this->ldap->getGroupHelper();
		$this->is_new = empty($attributes);
	}

	/**
	 * Store changes to server, including new groups
	 */
	public function store()
	{
		$this->ldap->bindPrivileged();
		
		// don't have groupname?
		if (!isset($this->name))
		{
			throw new LdapException("Can't store group without groupname.");
		}

		// new group?
		if ($this->is_new)
		{
			if ($this->helper->find($this->name))
			{
				throw new LdapException("Group already exists.");
			}

			$skel = array(
				'objectClass' => array(
					'posixGroup'
				),
				'cn' => $this->name,
				'gidNumber' => $this->helper->getNextID(),
				'description' => 'Group account'
			);

			foreach (array_keys($this->attributes_updated, 'name') as $key)
			{
				unset($this->attributes_updated[$key]);
			}
			
			// create this object
			if (!ldap_add($this->ldap->get_connection(), $this->get_dn(), $skel))
			{
				throw new LdapException("Unknown error.");
			}
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

			if (!ldap_mod_replace($this->ldap->get_connection(), $this->get_dn(), $new))
			{
				throw new LdapException("Unknown error.");
			}
			$this->attributes_updated = array();
		}
	}

	public function noMembers()
	{
		$this->members = null;
	}

	/**
	 * Set list of members
	 */
	public function setMembers($list)
	{
		$this->members = $list;
	}

	/**
	 * Set list of owners
	 */
	public function setOwners($list)
	{
		$this->owners = $list;
	}

	/**
	 * Get list of members
	 */
	public function getMembers()
	{
		if (is_null($this->members))
		{
			// not loaded
			throw new LdapException("Not implemented");
		}

		return (array) $this->members;
	}

	/**
	 * Get list of owners
	 */
	public function getOwners()
	{
		if (is_null($this->owners)) return array();
		return $this->owners;
	}

	/**
	 * Group DNs in users and groups
	 */
	protected function groupDNlist($list)
	{
		$ret = array(/*
			"users" => array(),
			"groups" => array()*/);

		foreach ($list as $dn)
		{
			if (!preg_match("~^(.+?)=(.+?),(.+)$~", $dn, $matches))
				continue;

			if ($matches[3] == $this->ldap->config['user_dn'])
			{
				$ret['users'][] = $matches[2];
			}

			elseif ($matches[3] == $this->ldap->config['group_dn'])
			{
				$ret['groups'][] = $matches[2];
			}
		}

		return $ret;
	}

	/**
	 * Get DN for the object
	 *
	 * @return string
	 */
	public function get_dn()
	{
		if (isset($this->dn)) return $this->dn;
		return sprintf("%s=%s,%s",
			$this->helper->field('unique_id'),
			Ldap::escape_string($this->name),
			$this->ldap->config['group_dn']
		);
	}

	/**
	 * Dynamically access the group's attributes.
	 *
	 * @param  string  $key
	 * @return mixed
	 */
	public function __get($key)
	{
		return $this->attributes[$key];
	}

	/**
	 * Dynamically set an attribute on the group.
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
	 * Dynamically check if a value is set on the group.
	 *
	 * @param  string  $key
	 * @return bool
	 */
	public function __isset($key)
	{
		return isset($this->attributes[$key]);
	}

	/**
	 * Dynamically unset a value on the group.
	 *
	 * @param  string  $key
	 * @return bool
	 */
	public function __unset($key)
	{
		unset($this->attributes[$key]);
	}

	/**
	 * Array-representation
	 *
	 * @param array field to ignore
	 */
	public function toArray(array $except = array())
	{
		$d = $this->attributes;
		unset($d['dn']);
		foreach ($except as $e)
			unset($d[$e]);

		// members
		if (!is_null($this->members) && !in_array("members", $except))
		{
			$d = array_merge($d, array("members" => $this->groupDNlist($this->getMembers())));
		}

		// owners
		if (!is_null($this->owners) && !in_array("owners", $except))
		{
			$d = array_merge($d, array("owners" => $this->groupDNlist($this->getOwners())));
		}

		return $d;
	}
}
