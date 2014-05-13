<?php namespace Blindern\UsersAPI\LDAP\Helpers;

abstract class CommonHelper {
	protected $type = 'common';
	protected $objectClass = '';

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
	 * Get all objects
	 *
	 * @return array
	 */
	public function all()
	{
		return $this->getByFilter();
	}

	/**
	 * Find one
	 *
	 * @return self
	 */
	public function find($name)
	{
		$objs = $this->getByNames(array($name));
		if (isset($objs[0])) return $objs[0];
	}

	/**
	 * Get details about a collection
	 * @param array list of names
	 * @return array(array|null object, ..)
	 */
	public function getByNames(array $names)
	{
		$list = array();
		foreach ($names as $name)
		{
			$list[] = sprintf('(%s=%s)', $this->field('unique_id'), Ldap::escape_string($name));
		}
		$filter = sprintf('(|%s)', implode("", $list));

		return $this->getByFilter($filter);
	}

	/**
	 * Get field name in LDAP
	 *
	 * @return string
	 */
	public function field($name)
	{
		return $this->ldap->config[$this->type.'_fields'][$name];
	}

	/**
	 * Get next available ID
	 */
	public function getNextID()
	{
		$field = strtolower($this->field('id'));
		$r = ldap_search($this->ldap->get_connection(), $this->ldap->config[$this->type.'_dn'], '(objectClass='.$this->objectClass.')', array($field));
		$e = ldap_get_entries($this->ldap->get_connection(), $r);

		$values = array();
		for ($i = 0; $i < $e['count']; $i++)
		{
			$values[] = $e[$i][$field][0];
		}

		return max($values)+1;
	}
}