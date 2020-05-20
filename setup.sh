#!/bin/bash
DIR="$( cd "$(dirname "$0")" ; pwd -P )"
cd $DIR/cn1-websockets-demo
bash setup.sh
cd ../cn1-websockets-lib
bash setup.sh
