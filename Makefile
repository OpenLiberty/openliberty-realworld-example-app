test:
	echo "pizza"

install:
	mvn clean install

dropins:
	cp utility/com.ibm.ws.security.mp.jwt_1.0.37.jar target/liberty/wlp/lib/
	rm -rf target/liberty/wlp/usr/servers/defaultServer/workarea/

drun: install dropins
	mvn liberty:run-server

run: install dropins
	mvn liberty:run-server

fullrun:
	mvn clean install liberty:run-server -DskipTests
	
