# http POST "localhost:8080/home/add?name=John&email=temp"
# http GET "localhost:8080/home/all"
# http "localhost:8080/home/error"
http --auth=admin:password GET "localhost:8080/home"
# curl -X POST "localhost:8080/home/add?name=John&email=temp"
# curl localhost:8080/home/all
