
```sh
docker-compose up -d
export SCYLLA='http://127.0.0.1:8000'
```

```sh
aws --endpoint-url $SCYLLA dynamodb create-table \
    --table-name movie \
    --attribute-definitions \
        AttributeName=movieId,AttributeType=S \
        AttributeName=title,AttributeType=S \
        AttributeName=genre,AttributeType=S \
    --key-schema \
        AttributeName=movieId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --global-secondary-indexes \
        '[
            {
                "IndexName": "title-index",
                "KeySchema": [{"AttributeName":"title","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 5, "WriteCapacityUnits": 5}
            },
            {
                "IndexName": "genre-index",
                "KeySchema": [{"AttributeName":"genre","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 5, "WriteCapacityUnits": 5}
            }
        ]'
```


```sh
aws --endpoint-url $SCYLLA dynamodb put-item \
    --table-name movie \
    --item \
        '{
            "movieId": {"S": "1e7b56f3-0c65-4fa6-9a32-6d0a65fbb3a5"},
            "title": {"S": "Inception"},
            "releaseYear": {"N": "2010"},
            "genre": {"S": "Science Fiction"},
            "rating": {"N": "8.8"},
            "director": {"S": "Christopher Nolan"}
        }'

aws --endpoint-url $SCYLLA dynamodb put-item \
    --table-name movie \
    --item \
        '{
            "movieId": {"S": "2a4b6d72-789b-4a1a-9c7f-74e5a8f7676d"},
            "title": {"S": "The Matrix"},
            "releaseYear": {"N": "1999"},
            "genre": {"S": "Action"},
            "rating": {"N": "8.7"},
            "director": {"S": "The Wachowskis"}
        }'

aws --endpoint-url $SCYLLA dynamodb put-item \
    --table-name movie \
    --item \
        '{
            "movieId": {"S": "3f6c8f74-2e6a-48e9-a07f-034f8a67b9e6"},
            "title": {"S": "Interstellar"},
            "releaseYear": {"N": "2014"},
            "genre": {"S": "Adventure"},
            "rating": {"N": "8.6"},
            "director": {"S": "Christopher Nolan"}
        }'
```

```sh
aws --endpoint-url $SCYLLA dynamodb scan --table-name movie
```

```sh
aws --endpoint-url $SCYLLA dynamodb get-item \
    --table-name movie \
    --key \
        '{
            "movieId": {"S": "1e7b56f3-0c65-4fa6-9a32-6d0a65fbb3a5"}
        }'
```