# TODO demo app - development aspects

### todos-api - cURL commands

Create:
```http
# create with minimum items, defaults:
# category to Default category 
# deadline to local time in "yyyy/mm/dd" format
# id is assigned a UUID if not set

curl --location --request POST 'localhost:8081' \
--header 'Content-Type: application/json' \
--data-raw '{
    "completed": false,
    "title": "item1"
}'

# create with all fields
curl --location --request POST 'localhost:8081' \
--header 'Content-Type: application/json' \
--data-raw '{
    "completed": false,
    "title": "item1",
    "category": "personal",
    "deadline": "2020/04/20"
}'

# replace entire entity with a specific 'id'
curl --location --request POST 'localhost:8081/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "completed": false,
    "id": "1",
    "title": "item1",
    "category": "personal",
    "deadline": "2020/04/20"
}'
```

Update:
```http
# update an entity for a specific 'id'
curl --location --request PATCH 'localhost:8081/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "completed": false,
    "id": "1",
    "title": "item1",
    "category": "personal",
    "deadline": "2020/04/20"
}'
```

Delete:
```http
# delete all records and clear cache
curl --location --request DELETE 'localhost:8081' \
--header 'Content-Type: application/json' 

# delete a TODO and clear from cache
curl --location --request DELETE 'localhost:8081/1'
```

Get:
```http
# get all items
curl localhost:8081

# get all items by id
curl localhost:8081/1
```

### todos-redis - cURL commands
```http
# create an item in the cache
curl --location --request POST 'localhost:8888' \
--header 'Content-Type: application/json' \
--data-raw '   {
        "id": "item12",
        "title": "Add demo todo item #12",
        "complete": false,
        "category": "personal",
        "deadline": "2020/04/20"
    }'

# update an item in the cache
curl --location --request POST 'localhost:8888' \
--header 'Content-Type: application/json' \
--data-raw '   {
        "id": "item12",
        "title": "Add demo todo item #12",
        "complete": false,
        "category": "business",
        "deadline": "2020/04/20"
    }'
    
# get all items in cache
curl --location --request GET 'localhost:8888'

# get item by id
curl --location --request GET 'localhost:8888/c6509a5a-4ff7-48d2-941b-8f076969cb0c'
curl --location --request GET 'localhost:8888/test3'

# delete all
curl --location --request DELETE 'localhost:8888'

# delete by 'id'
curl --location --request DELETE 'localhost:8888/test3'

# load demo test data - specify how many items
curl --location --request POST 'localhost:8888/load/10'
