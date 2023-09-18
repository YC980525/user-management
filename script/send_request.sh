
http -v GET "localhost:8080/home/all"
http -v --auth=admin:password GET "localhost:8080/home/all"
http -v --auth=admin:password GET "localhost:8080/home/admin"
# http -v --auth=dummy1:dummy GET "localhost:8080/home/dummy1"
# http -v POST "localhost:8080/home/add?name=John&email=temp"
# http -v "localhost:8080/home/error"
