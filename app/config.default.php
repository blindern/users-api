<?php

return array(
	"relative_path" => "/users-api",
	
	'auth' => array(
		'ldap' => array(
			'server' => 'ldapmaster.vpn.foreningenbs.no',
			'tls' => true,

			'base_dn' => 'dc=foreningenbs,dc=no',
			'group_dn' => 'ou=Groups,dc=foreningenbs,dc=no',
			'user_dn' => 'ou=Users,dc=foreningenbs,dc=no',
			'bind_dn' => 'uid=USERNAME,ou=Users,dc=foreningenbs,dc=no',
			'username_field' => 'uid',

			'admin_dn' => 'cn=admin,dc=foreningenbs,dc=no',
			'admin_pw' => require "../../ldap-pass.php",

			// name of group where members are considered to be superadmins
			'superadmin_group' => 'admin',

			// field mappeing for users
			'user_fields' => array(
				'unique_id' => 'uid', // what it is identified by
				'id' => 'uidNumber',
				'username' => 'uid',
				'email' => 'mail',
				'realname' => 'cn',
				'phone' => 'mobile'
			),

			// field mapping for groups
			'group_fields' => array(
				'unique_id' => 'cn', // what it is identified by
				'id' => 'gidNumber',
				'name' => 'cn',
				'members' => 'member', // memberUid
				'description' => 'description',
				'owners' => 'owner'
			),

			// don't show these groups
			'groups_ignore' => array(
				"Domain Users",
				"Domain Admins",
				"Account Operators",
				"Administrators",
				"Backup Operators",
				"Domain Computers",
				"Domain Guests",
				"Print Operators",
				"Replicators"
			),

			'groups_full_dn' => true,
			'groups_nested' => true
		),

		'cache_file' => dirname(__FILE__).'/cache/userdata.tmp',
		'cache_timeout' => 300 // TODO
	)
);
