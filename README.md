users-api
=========

API between user database and other services.

All services having access to this resource have full access to user database.

## Models

### user-model

* userid (unique, only used by Linux filesystem?)
* username (unique)
* realname
* email
* phone
* groups => group-model collection

```username``` should always be lowercase

Password-updates can be done by pushing 'password' when updating a user.

### group-model

* groupid (unique, only used by Linux filesystem?)
* groupname (unique)
* members => user-model collection

## Requests

### Authing

Simple method:
```/simpleauth```

Must be used over SSL.

POST-data:
* username
* password in plaintext