users-api
=========

API between user database and other services.

All services having access to this resource have full access to user database.

## Models

### user-model

* userid (only used by Linux filesystem?)
* username
* realname
* email
* phone
* groups => group-model collection

```username``` should always be lowercase

### group-model

* groupid (only used by Linux filesystem?)
* groupname
* members => user-model collection
