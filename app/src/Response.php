<?php namespace Blindern\UsersAPI;

class Response {
	const SUCCESS = 200;
	const ERROR = 500;
	const INVALID_REQUEST = 400;
	const INVALID_DATA = 406;
	const LOGIN_FAIL = 401;
	const NOT_FOUND = 404;

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