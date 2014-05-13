<?php namespace Blindern\UsersAPI;

class UserHelper extends UserGroupHelper {
	protected static $helper;

	public static function forge()
	{
		if (is_null(static::$helper))
		{
			return new static();
		}

		return static::$helper;
	}

	protected $type = 'user';

	/**
	 * Validate fields for user
	 *
	 * Is run before a user is created or updated
	 *
	 * @param array fields with the new data
	 * @return mixed True on valid data, fieldname on error
	 */
	public function validate($fields)
	{
		foreach ($fields as $field => $data)
		{
			switch ($field)
			{
				case "username":
					if (!preg_match("~^[a-z0-9\\-_]+$~", $data))
					{
						return 'username';
					}
					break;

				case "password":
					if (!preg_match("~^.{8,}$~", $data))
					{
						return 'password';
					}
					break;

				case "realname":
					// no check here?
					break;

				case "email":
					// TODO: check for valid email
					break;

				case "phone":
					if (!preg_match("~^(|\\d{8})$~", $data))
					{
						return 'phone';
					}
					break;
			}
		}

		return true;
	}
}