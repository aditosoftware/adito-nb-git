#!/bin/bash
read -p "Enter keystore password:" keystorepass
mvn clean package nbm:nbm -Dkeystorepass=$keystorepass