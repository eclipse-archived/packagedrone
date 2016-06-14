# Upload V2

    curl -X PUT --data-binary @README.md http://deploy:583ef5a61ec1694cf0827c2de39b59b4eec222e44f983c2c064b3586e4e65637@localhost:8080/api/v2/upload/channel/c7b64150-911c-4364-8da9-035314832648/README.md?test:foo=bar
    
# Upload V3

    curl -X PUT --data-binary @README.md http://deploy:583ef5a61ec1694cf0827c2de39b59b4eec222e44f983c2c064b3586e4e65637@localhost:8080/api/v3/upload/plain/channel/c7b64150-911c-4364-8da9-035314832648/README.md?test:foo=bar