

curl --location --request POST 'localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--data-raw '{
"userId": "asraf",
"password":"mypassword"
}'