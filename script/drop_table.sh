#!/bin/bash

# Variables for MySQL
USER="root"
PASSWORD="password"
DATABASE="spring_member"
HOST="localhost"

# MySQL command to execute
SQL="
SET FOREIGN_KEY_CHECKS = 0;
DROP table authorities, users;
SET FOREIGN_KEY_CHECKS = 1;"

# Execute the command
# mysql -u $USER -p$PASSWORD -h $HOST $DATABASE -e "DROP table users_authorities;"
mysql -u $USER -p$PASSWORD -h $HOST $DATABASE -e "$SQL"
