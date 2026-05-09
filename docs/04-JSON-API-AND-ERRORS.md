# JSON:API and Errors

## Content type

```text
Content-Type: application/vnd.api+json
```

## Success response

```json
{
  "data": {
    "type": "price-lists",
    "id": "default",
    "attributes": {
      "products": []
    }
  }
}
```

## Error response

```json
{
  "errors": [
    {
      "status": "400",
      "title": "Bad Request",
      "detail": "Path parameter 'priceListId' is required"
    }
  ]
}
```

## GET vs POST/PATCH

GET by path id:

```text
GET /price-lists/{priceListId}
```

No JSON:API request body needed.

POST/PATCH body:

```json
{
  "data": {
    "type": "price-lists",
    "attributes": {
      "name": "Lista verano",
      "currency": "BRL"
    }
  }
}
```

## Error mapping

```text
400 Bad Request        malformed request, missing path param, invalid query, invalid JSON
404 Not Found          resource not found
409 Conflict           duplicate, concurrency or invalid state conflict
422 Unprocessable      syntactically valid request but business rule violation
429 Too Many Requests  rate limit / quota
500 Internal Error     unexpected error
```

## Future JSON:API evolution

Current simplification: nested products can live in `attributes.products`.

Future stricter JSON:API modeling can move nested resources to:

```text
relationships
included
links
meta
pagination
filters
```
