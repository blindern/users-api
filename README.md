users-api
=========

API mellom brukerdatabasen og øvrige tjenester

## user-model

* userid (only used by Linux filesystem?)
* username
* realname
* email
* phone
* groups => group-model collection

```username``` should always be lowercase

## group-model

* groupid (only used by Linux filesystem?)
* groupname
* members => user-model collection
