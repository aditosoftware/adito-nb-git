#!/bin/bash
read -p "Enter keystore password:" keystorepass
mvn clean install
cd nbm
mvn package nbm:nbm -Dkeystorepass=$keystorepass
cd ..
#mvn deploy