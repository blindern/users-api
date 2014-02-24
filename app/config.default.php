<?php

return array(
	"relative_path" => "/users-api",
	
	'auth' => array(
		'ldap' => array(
			'server' => 'ldap.blindern-studenterhjem.no',
			'tls' => true,

			'base_dn' => 'dc=blindern-studenterhjem,dc=no',
			'group_dn' => 'ou=Groups,dc=blindern-studenterhjem,dc=no',
			'user_dn' => 'ou=Users,dc=blindern-studenterhjem,dc=no',
			'bind_dn' => 'uid=USERNAME,ou=Users,dc=blindern-studenterhjem,dc=no',
			'username_field' => 'uid',

			'admin_dn' => 'cn=admin,dc=blindern-studenterhjem,dc=no',
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
				'members' => 'memberUid',
				'description' => 'description'
			),

			// don't show these groups
			'groups_ignore' => array(
				"Domain Users",
				"Domain Admins"
			)
		)
	)
);