<?php namespace Blindern\UsersAPI;

abstract class UserGroupHelper {
	protected $type = 'common';

	/**
	 * Collection of all objects
	 */
	protected $all = array();

	/**
	 * Get object name
	 */
	protected function getObjName()
	{
		$type = $this->type == "user" ? "User" : "Group";
		return "\\Blindern\\UsersAPI\\".$type;
	}

	/**
	 * Get all objects
	 *
	 * @return array
	 */
	public function all()
	{
		$provider = DataProvider::forge();
		$data = $provider->getData();
		$objname = $this->getObjName();

		$ret = array();
		foreach ($data->{$this->type.'s'} as $name => $group)
		{
			if (!isset($this->all[$name]))
				$this->all[$name] = new $objname($group);

			$ret[] = $this->all[$name];
		}

		return $ret;
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
	 * Get collection
	 * @param array list of names
	 * @return array(array|null object, ..)
	 */
	public function getByNames(array $names)
	{
		$objname = $this->getObjName();
		$provider = DataProvider::forge();

		$data = $provider->getData();

		$ret = array();
		foreach ($names as $name)
		{
			if (isset($this->all[$name]))
				$ret[] = $this->all[$name];

			elseif (isset($data->{$this->type.'s'}[$name]))
			{
				$this->all[$name] = new $objname($data->{$this->type.'s'}[$name]);
				$ret[] = $this->all[$name];
			}
		}

		return $ret;
	}

	/**
	 * Generate array of objects
	 * @var array(object, object, ..)
	 */
	public function generateArray(array $objects, $memberdepth, $ownerdepth)
	{
		$ret = array();
		foreach ($objects as $object)
		{
			$ret[] = $object->toArray(array(), $memberdepth, $ownerdepth);
		}

		return $ret;
	}
}