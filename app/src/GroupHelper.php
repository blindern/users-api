<?php namespace Blindern\UsersAPI;

class GroupHelper extends UserGroupHelper {
	protected static $helper;

	public static function forge()
	{
		if (is_null(static::$helper))
		{
			return new static();
		}

		return static::$helper;
	}

	protected $type = 'group';
}