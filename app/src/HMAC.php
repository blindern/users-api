<?php namespace Blindern\UsersAPI;

/**
 * Take care of request-authentication by using HMAC-method
 */
class HMAC {
	/**
	 * Validate the request to see if it matches the API-key
	 */
	public function verify_request()
	{
		$timeout = $this->getTimeout();
		$uri = $_SERVER['REQUEST_URI'];

		// HMAC-data is stored in headers
		$headers = apache_request_headers();
		if (!isset($headers['X-API-Hash']) || !isset($headers['X-API-Time']))
			return false;
		// verify time
		if ($headers['X-API-Time'] < time()-$timeout ||
		    $headers['X-API-Time'] > time()+$timeout)
		    return false;

		// verify key
		$hash = $this->generateHMACHash($headers['X-API-Time'], $_SERVER['REQUEST_METHOD'], $uri, $_POST);
		if (strcmp($hash, $headers['X-API-Hash']) != 0)
			return false;

		return true;
	}

	/**
	 * Generate a HMAC-hash
	 */
	public function generateHMACHash($time, $method, $uri, array $post_variables)
	{
		$data = json_encode(array((string)$time, $method, $uri, (array)$post_variables));
		return hash_hmac('sha256', $data, $this->getPrivateKey());
	}

	/**
	 * Get private key
	 */
	public function getPrivateKey()
	{
		return app()->config['auth']['api_key'];
	}

	/**
	 * Get timeout
	 */
	public function getTimeout()
	{
		return app()->config['auth']['api_timeout'];
	}
}