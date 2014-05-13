<?php namespace Blindern\UsersAPI;

use \Blindern\UsersAPI\DataProvider;

abstract class UserGroupCommon implements \JsonSerializable {
	/**
	 * All of the object's attributes.
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
	 * New object not stored?
	 *
	 * @var bool
	 */
	protected $is_new = false;

	/**
	 * Create a new object
	 *
	 * @param  array  $attributes
	 * @return void
	 */
	public function __construct(array $attributes)
	{
		$this->attributes = $attributes;
		$this->is_new = empty($attributes);
	}

	/**
	 * Store changes to server, including new groups
	 */
	public function store()
	{
		throw new \Exception("Not implemented");
	}

	/**
	 * Dynamically access the object's attributes.
	 *
	 * @param  string  $key
	 * @return mixed
	 */
	public function __get($key)
	{
		return $this->attributes[$key];
	}

	/**
	 * Dynamically set an attribute on the object.
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
	 * Dynamically check if a value is set on the object.
	 *
	 * @param  string  $key
	 * @return bool
	 */
	public function __isset($key)
	{
		return isset($this->attributes[$key]);
	}

	/**
	 * Dynamically unset a value on the object.
	 *
	 * @param  string  $key
	 * @return bool
	 */
	public function __unset($key)
	{
		unset($this->attributes[$key]);
	}

	/**
	 * Make array for JSON
	 *
	 * @return array
	 */
	public function jsonSerialize()
	{
		return $this->toArray();
	}
}
