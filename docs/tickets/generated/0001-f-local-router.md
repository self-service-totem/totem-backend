## api gateway local
Objetivo: imitar las rutas del apigateway

Tendrías una sola pieza central:

ApiRouter
recibe: method, path, headers, queryParams, pathParams, body
devuelve: statusCode, headers, body

Y dos adapters de entrada:

AWS Lambda Adapter
APIGatewayV2HTTPEvent → ApiRequest → ApiRouter → ApiResponse → APIGatewayV2HTTPResponse

Local Controller Adapter
HttpServletRequest → ApiRequest → ApiRouter → ApiResponse → ResponseEntity

Así el router es compartido.

¿Tengo que replicar manualmente los paths del OpenAPI?

Mi respuesta práctica: al principio sí, manualmente, pero en un solo lugar.

No lo duplicaría en cada controller ni en cada function. Lo pondría en un registry único, tipo:

@Component
@RequiredArgsConstructor
public class ApiRoutes {

    private final GetPriceListHandler getPriceListHandler;
    private final GetCatalogVersionHandler getCatalogVersionHandler;

    public List<ApiRoute> routes() {
        return List.of(
            ApiRoute.get("/price-lists/{priceListId}", getPriceListHandler),
            ApiRoute.get("/branches/{branchId}/catalog/version", getCatalogVersionHandler)
        );
    }
}

Y el ApiRouter usa esa lista para resolver.

Eso te deja la duplicación en un solo punto:

openapi.yaml
ApiRoutes.java