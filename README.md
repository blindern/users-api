users-api
=========

API between user database and other services. The objective is to have an API
so that the underlying user database can be swapped from LDAP to other types,
if necessary.

All services having access to this resource have full access to user database.

## Models

### user-model

* userid (unique, only used by Linux filesystem?)
* username (unique)
* realname
* email
* phone
* groups_relation => array(group => givenbygroup, ..)
* groups => group-model collection

```username``` should always be lowercase

Password-updates can be done by pushing 'password' when updating a user.

### group-model

* groupid (unique, only used by Linux filesystem?)
* groupname (unique)
* owners => list of owners grouped by users and subgroups
* members => list of members grouped by users and subgroups
* members_relation => expanded list of members with source group in subarray
* members_data => user-model collection

## Responses

JSON is returned following this syntax:
```json
{
	'status': {
		'code': 'STATUSCODE'
		'text': 'data here'
	},
	'result: DATA-GENERATED
}
```

STATUSCODE of 0 means success, other is error

DATA-GENERATED is the actual response

## Requests

### Authing

Simple method: ```/simpleauth```

Must be used over SSL.

POST-data:
* username
* password in plaintext

## Secret data
The file ```/app/secrets.php``` needs to return an array with the following data:

```php
<?php

return array(
	'ldap_pass' => 'REPLACE',
	'api_key' => 'REPLACE'
);
```

## To do
* Document HMAC-signing (it is required for all requests)