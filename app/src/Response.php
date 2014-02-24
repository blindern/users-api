<?php namespace Blindern\UsersAPI;

class Response {
	const SUCCESS = 0;
	const ERROR = 1;
	const INVALID_REQUEST = 2;
	const INVALID_DATA = 3;
	const USER_NOT_FOUND = 10;

	public static function forge($statusCode, $statusText, $result = null)
	{
		$x = new static();
		$x->statusCode = $statusCode;
		$x->statusText = $statusText;
		$x->result = $result;
		return $x;
	}

	public $statusCode = self::SUCCESS;
	public $statusText = '';
	public $result;

	public function __toString()
	{
		$arr = array(
			'status' => array(
				'code' => $this->statusCode,
				'text' => $this->statusText
			),
			'result' => $this->result
		);

		return json_encode($arr);
	}
}