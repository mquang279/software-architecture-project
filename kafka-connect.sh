KAFKA_CONNECT_URL=http://localhost:8084/connectors/

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" $KAFKA_CONNECT_URL -d @reservation-connect.json

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" $KAFKA_CONNECT_URL -d @seat-connect.json

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" $KAFKA_CONNECT_URL -d @payment-connect.json

curl -i -X GET $KAFKA_CONNECT_URL